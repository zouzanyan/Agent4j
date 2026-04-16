package com.example.ai.service;

import com.example.ai.dto.FeedbackRequest;

/**
 * 反馈服务接口
 */
public interface FeedbackService {

    /**
     * 提交反馈
     *
     * @param request 反馈请求
     */
    void submitFeedback(FeedbackRequest request);
}
