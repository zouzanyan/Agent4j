package com.example.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式聊天响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamChatResponse {

    /**
     * 响应类型：token/complete/error
     */
    private String type;

    /**
     * 内容（token内容或错误信息）
     */
    private String content;

    /**
     * 会话ID
     */
    private Long conversationId;
}
