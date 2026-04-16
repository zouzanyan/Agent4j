package com.example.ai.controller;

import com.example.ai.dto.ApiResponse;
import com.example.ai.dto.FeedbackRequest;
import com.example.ai.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 反馈控制器
 */
@Slf4j
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 提交反馈（点赞/点踩）
     *
     * @param request 反馈请求
     * @return 响应
     */
    @PostMapping
    public ApiResponse<Void> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        log.info("Feedback request. messageId: {}, type: {}", request.getMessageId(), request.getType());

        feedbackService.submitFeedback(request);
        return ApiResponse.success();
    }
}
