package com.example.ai.service;

import com.example.ai.dto.ChatRequest;
import com.example.ai.dto.ChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 同步聊天
     *
     * @param userId 用户ID
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(String userId, ChatRequest request);

    /**
     * 流式聊天
     *
     * @param userId 用户ID
     * @param request 聊天请求
     * @return SSE发射器
     */
    SseEmitter chatStream(String userId, ChatRequest request);
}
