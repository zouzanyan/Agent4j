package com.example.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {

    /**
     * 会话ID（可为空，表示新建会话）
     */
    private Long conversationId;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;
}
