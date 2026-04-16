package com.example.ai.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Data
public class Message {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 角色：user/assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
