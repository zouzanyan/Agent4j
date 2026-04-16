package com.example.ai.controller;

import com.example.ai.dto.ApiResponse;
import com.example.ai.dto.ConversationDTO;
import com.example.ai.dto.ConversationDetailDTO;
import com.example.ai.service.ConversationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会话控制器
 */
@Slf4j
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final ConversationService conversationService;

    /**
     * 查询当前用户的会话列表
     *
     * @param httpRequest HTTP请求
     * @return 会话列表
     */
    @GetMapping
    public ApiResponse<List<ConversationDTO>> listConversations(HttpServletRequest httpRequest) {
        String userId = getUserId(httpRequest);
        log.info("List conversations for user: {}", userId);

        List<ConversationDTO> conversations = conversationService.listConversations(userId);
        return ApiResponse.success(conversations);
    }

    /**
     * 查询会话详情
     *
     * @param id 会话ID
     * @param httpRequest HTTP请求
     * @return 会话详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ConversationDetailDTO> getConversationDetail(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        String userId = getUserId(httpRequest);
        log.info("Get conversation detail. user: {}, conversationId: {}", userId, id);

        ConversationDetailDTO detail = conversationService.getConversationDetail(userId, id);
        return ApiResponse.success(detail);
    }

    private String getUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("缺少请求头: " + USER_ID_HEADER);
        }
        return userId;
    }
}
