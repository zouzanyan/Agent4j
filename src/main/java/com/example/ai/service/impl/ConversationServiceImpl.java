package com.example.ai.service.impl;

import com.example.ai.dto.ConversationDTO;
import com.example.ai.dto.ConversationDetailDTO;
import com.example.ai.dto.MessageDTO;
import com.example.ai.entity.Conversation;
import com.example.ai.entity.Message;
import com.example.ai.mapper.ConversationMapper;
import com.example.ai.mapper.MessageMapper;
import com.example.ai.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @Override
    public List<ConversationDTO> listConversations(String userId) {
        List<Conversation> conversations = conversationMapper.listByUserId(userId);
        return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConversationDetailDTO getConversationDetail(String userId, Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new IllegalArgumentException("会话不存在: " + conversationId);
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权访问该会话: " + conversationId);
        }

        List<Message> messages = messageMapper.listByConversationId(conversationId);
        List<MessageDTO> messageDTOs = messages.stream()
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());

        return ConversationDetailDTO.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .messages(messageDTOs)
                .build();
    }

    private ConversationDTO convertToDTO(Conversation conversation) {
        return ConversationDTO.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    private MessageDTO convertToMessageDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
