package com.example.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式聊天响应DTO
 *
 * type 取值：
 * - reasoning:  思维链片段（content 为思考内容）
 * - tool_call:  工具调用（toolName + toolArgs）
 * - token:      逐 token 输出（content 为文本片段）
 * - complete:   生成完成（content 为完整回复）
 * - error:      出错（content 为错误信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamChatResponse {

    private String type;

    private String content;

    private Long conversationId;

    private String toolName;

    private String toolArgs;
}
