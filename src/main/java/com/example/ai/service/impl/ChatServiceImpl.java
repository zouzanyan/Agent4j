package com.example.ai.service.impl;

import com.example.ai.agent.AiAgent;
import com.example.ai.dto.ChatRequest;
import com.example.ai.dto.ChatResponse;
import com.example.ai.dto.StreamChatResponse;
import com.example.ai.entity.Conversation;
import com.example.ai.entity.Message;
import com.example.ai.mapper.ConversationMapper;
import com.example.ai.mapper.MessageMapper;
import com.example.ai.service.ChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 聊天服务实现类
 * 使用 LangChain4j AiServices 自动处理工具调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final String SYSTEM_PROMPT = "你是企业AI助手，请基于上下文回答问题，不要编造内容，不确定时请说明。";
    private static final int HISTORY_LIMIT = 10;

    private final AiAgent aiAgent;
    private final ChatMemory chatMemory;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public ChatResponse chat(String userId, ChatRequest request) {
        Long conversationId = request.getConversationId();
        String userMessage = request.getMessage();

        conversationId = getOrCreateConversation(userId, conversationId, userMessage);
        validateConversationOwnership(userId, conversationId);

        // 加载历史消息到 ChatMemory
        loadHistoryToMemory(conversationId);

        // 使用 AiAgent 进行对话，自动处理工具调用
        String answer = aiAgent.chat(userMessage);

        // 保存消息到数据库
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

        SseEmitter emitter = new SseEmitter(300000L);
        StringBuilder full = new StringBuilder();

        saveUserMessage(finalConversationId, userMessage);
        loadHistoryToMemory(finalConversationId);

        aiAgent.chatStream(userMessage)
                .onPartialThinking(thinking -> {
                    try {
                        emitter.send(StreamChatResponse.builder()
                                .type("reasoning")
                                .content(thinking.text())
                                .conversationId(finalConversationId)
                                .build());
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onPartialToolCall(partialToolCall -> {
                    try {
                        String toolName = partialToolCall.name();
                        String toolArgs = partialToolCall.partialArguments();
                        emitter.send(StreamChatResponse.builder()
                                .type("tool_call")
                                .toolName(toolName)
                                .toolArgs(toolArgs)
                                .conversationId(finalConversationId)
                                .build());
                        log.debug("Tool calling: {}", toolName);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onPartialResponse(token -> {
                    try {
                        // 过滤纯空白 token（模型常在工具调用后输出 \n\n）
                        if (token.isBlank()) {
                            return;
                        }
                        full.append(token);
                        emitter.send(StreamChatResponse.builder()
                                .type("token")
                                .content(token)
                                .conversationId(finalConversationId)
                                .build());
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(response -> {
                    try {
                        String answer = full.toString();
                        saveAssistantMessage(finalConversationId, answer);

                        emitter.send(StreamChatResponse.builder()
                                .type("complete")
                                .content(answer)
                                .conversationId(finalConversationId)
                                .build());
                        emitter.complete();
                        log.info("Stream chat completed. conversationId={}", finalConversationId);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .onError(error -> {
                    try {
                        log.error("Error in stream chat", error);
                        emitter.send(StreamChatResponse.builder()
                                .type("error")
                                .content(error.getMessage())
                                .conversationId(finalConversationId)
                                .build());
                        emitter.complete();
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .start();

        return emitter;
    }

    /**
     * 加载历史消息到 ChatMemory
     */
    private void loadHistoryToMemory(Long conversationId) {
        // 清空当前记忆
        chatMemory.clear();

        // 添加系统提示
        chatMemory.add(new SystemMessage(SYSTEM_PROMPT));

        // 加载历史消息
        List<Message> recentMessages = messageMapper.listRecentMessages(conversationId, HISTORY_LIMIT);
        Collections.reverse(recentMessages);

        for (Message msg : recentMessages) {
            if ("user".equals(msg.getRole())) {
                chatMemory.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                chatMemory.add(new AiMessage(msg.getContent()));
            }
        }

        log.debug("Loaded {} messages to ChatMemory for conversation {}", recentMessages.size(), conversationId);
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
        if (message == null || message.isEmpty()) {
            return "新对话";
        }
        return message.length() > 20 ? message.substring(0, 20) + "..." : message;
    }
}
