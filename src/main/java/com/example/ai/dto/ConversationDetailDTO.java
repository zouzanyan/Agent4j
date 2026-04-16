package com.example.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话详情DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDetailDTO {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 消息列表
     */
    private List<MessageDTO> messages;
}
