package com.example.ai.service.impl;

import com.example.ai.dto.FeedbackRequest;
import com.example.ai.entity.Feedback;
import com.example.ai.mapper.FeedbackMapper;
import com.example.ai.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 反馈服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    @Override
    public void submitFeedback(FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setMessageId(request.getMessageId());
        feedback.setType(request.getType());
        feedbackMapper.insert(feedback);

        log.info("Feedback submitted. messageId={}, type={}", request.getMessageId(), request.getType());
    }
}
