package com.example.ai.service.impl;

import com.example.ai.dto.ChatRequest;
import com.example.ai.dto.ChatResponse;
import com.example.ai.dto.StreamChatResponse;
import com.example.ai.entity.Conversation;
import com.example.ai.entity.Message;
import com.example.ai.mapper.ConversationMapper;
import com.example.ai.mapper.MessageMapper;
import com.example.ai.service.ChatService;
import com.example.ai.service.LlmService;
import com.example.ai.service.StreamHandler;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聊天服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final String SYSTEM_PROMPT = "你是企业AI助手，请基于上下文回答问题，不要编造内容，不确定时请说明。";
    private static final int HISTORY_LIMIT = 10;

    private final LlmService llmService;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public ChatResponse chat(String userId, ChatRequest request) {
        Long conversationId = request.getConversationId();
        String userMessage = request.getMessage();

        conversationId = getOrCreateConversation(userId, conversationId, userMessage);

        validateConversationOwnership(userId, conversationId);

        List<Message> recentMessages = messageMapper.listRecentMessages(conversationId, HISTORY_LIMIT);
        Collections.reverse(recentMessages);

        List<ChatMessage> chatMessages = buildChatMessages(recentMessages, userMessage);

        String answer = llmService.chat(chatMessages);

        saveUserMessage(conversationId, userMessage);
        Long assistantMessageId = saveAssistantMessage(conversationId, answer);

        log.info("Chat completed. conversationId={}, messageId={}", conversationId, assistantMessageId);

        return ChatResponse.builder()
                .conversationId(conversationId)
                .answer(answer)
                .build();
    }

    @Override
    public SseEmitter chatStream(String userId, ChatRequest request) {
        Long conversationId = request.getConversationId();
        String userMessage = request.getMessage();

        Long finalConversationId = getOrCreateConversation(userId, conversationId, userMessage);

        validateConversationOwnership(userId, finalConversationId);

        List<Message> recentMessages = messageMapper.listRecentMessages(finalConversationId, HISTORY_LIMIT);
        Collections.reverse(recentMessages);

        List<ChatMessage> chatMessages = buildChatMessages(recentMessages, userMessage);

        SseEmitter emitter = new SseEmitter(300000L);

        saveUserMessage(finalConversationId, userMessage);

        llmService.chatStream(chatMessages, new StreamHandler() {
            private final StringBuilder fullAnswer = new StringBuilder();

            @Override
            public void onToken(String token) {
                try {
                    fullAnswer.append(token);
                    StreamChatResponse response = StreamChatResponse.builder()
                            .type("token")
                            .content(token)
                            .conversationId(finalConversationId)
                            .build();
                    emitter.send(response);
                } catch (IOException e) {
                    log.error("Error sending SSE token", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(String fullResponse) {
                try {
                    saveAssistantMessage(finalConversationId, fullResponse);

                    StreamChatResponse response = StreamChatResponse.builder()
                            .type("complete")
                            .content(fullResponse)
                            .conversationId(finalConversationId)
                            .build();
                    emitter.send(response);
                    emitter.complete();

                    log.info("Stream chat completed. conversationId={}", finalConversationId);
                } catch (IOException e) {
                    log.error("Error completing SSE", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("Error in stream chat", error);
                try {
                    StreamChatResponse response = StreamChatResponse.builder()
                            .type("error")
                            .content(error.getMessage())
                            .conversationId(finalConversationId)
                            .build();
                    emitter.send(response);
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        });

        return emitter;
    }

    private Long getOrCreateConversation(String userId, Long conversationId, String userMessage) {
        if (conversationId == null) {
            Conversation conversation = new Conversation();
            conversation.setUserId(userId);
            conversation.setTitle(generateTitle(userMessage));
            conversationMapper.insert(conversation);
            return conversation.getId();
        }
        return conversationId;
    }

    private void validateConversationOwnership(String userId, Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new IllegalArgumentException("会话不存在: " + conversationId);
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权访问该会话: " + conversationId);
        }
    }

    private List<ChatMessage> buildChatMessages(List<Message> historyMessages, String currentMessage) {
        List<ChatMessage> chatMessages = new ArrayList<>();

        chatMessages.add(new SystemMessage(SYSTEM_PROMPT));

        for (Message msg : historyMessages) {
            if ("user".equals(msg.getRole())) {
                chatMessages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                chatMessages.add(new AiMessage(msg.getContent()));
            }
        }

        chatMessages.add(new UserMessage(currentMessage));

        return chatMessages;
    }

    private void saveUserMessage(Long conversationId, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole("user");
        message.setContent(content);
        messageMapper.insert(message);
    }

    private Long saveAssistantMessage(Long conversationId, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole("assistant");
        message.setContent(content);
        messageMapper.insert(message);
        return message.getId();
    }

    private String generateTitle(String message) {
        if (message.length() <= 20) {
            return message;
        }
        return message.substring(0, 20) + "...";
    }
}
