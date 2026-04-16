package com.example.ai.controller;

import com.example.ai.dto.ApiResponse;
import com.example.ai.dto.ChatRequest;
import com.example.ai.dto.ChatResponse;
import com.example.ai.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final ChatService chatService;

    /**
     * 同步聊天接口
     *
     * @param request 聊天请求
     * @param httpRequest HTTP请求
     * @return 聊天响应
     */
    @PostMapping
    public ApiResponse<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            HttpServletRequest httpRequest) {
        String userId = getUserId(httpRequest);
        log.info("Chat request from user: {}, conversationId: {}", userId, request.getConversationId());

        ChatResponse response = chatService.chat(userId, request);
        return ApiResponse.success(response);
    }

    /**
     * 流式聊天接口（SSE）
     *
     * @param request 聊天请求
     * @param httpRequest HTTP请求
     * @return SSE发射器
     */
    @PostMapping("/stream")
    public SseEmitter chatStream(
            @Valid @RequestBody ChatRequest request,
            HttpServletRequest httpRequest) {
        String userId = getUserId(httpRequest);
        log.info("Stream chat request from user: {}, conversationId: {}", userId, request.getConversationId());

        return chatService.chatStream(userId, request);
    }

    private String getUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("缺少请求头: " + USER_ID_HEADER);
        }
        return userId;
    }
}
