package com.example.ai.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈实体类
 */
@Data
public class Feedback {

    /**
     * 反馈ID
     */
    private Long id;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 反馈类型：like/dislike
     */
    private String type;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
