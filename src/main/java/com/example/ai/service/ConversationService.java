package com.example.ai.service;

import com.example.ai.dto.ConversationDTO;
import com.example.ai.dto.ConversationDetailDTO;

import java.util.List;

/**
 * 会话服务接口
 */
public interface ConversationService {

    /**
     * 查询用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ConversationDTO> listConversations(String userId);

    /**
     * 查询会话详情
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 会话详情
     */
    ConversationDetailDTO getConversationDetail(String userId, Long conversationId);
}
