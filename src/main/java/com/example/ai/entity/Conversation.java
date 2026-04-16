package com.example.ai.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话实体类
 */
@Data
public class Conversation {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
