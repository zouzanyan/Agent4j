package com.example.ai.mapper;

import com.example.ai.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper {

    /**
     * 插入消息
     *
     * @param message 消息实体
     * @return 影响行数
     */
    int insert(Message message);

    /**
     * 根据会话ID查询消息列表（按ID正序）
     *
     * @param conversationId 会话ID
     * @return 消息列表
     */
    List<Message> listByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 查询最近的消息（按ID倒序 + limit）
     *
     * @param conversationId 会话ID
     * @param limit 限制条数
     * @return 消息列表
     */
    List<Message> listRecentMessages(@Param("conversationId") Long conversationId, @Param("limit") Integer limit);
}
