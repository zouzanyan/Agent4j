package com.example.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 反馈请求DTO
 */
@Data
public class FeedbackRequest {

    /**
     * 消息ID
     */
    @NotNull(message = "消息ID不能为空")
    private Long messageId;

    /**
     * 反馈类型：like/dislike
     */
    @NotBlank(message = "反馈类型不能为空")
    @Pattern(regexp = "^(like|dislike)$", message = "反馈类型只能是like或dislike")
    private String type;
}
