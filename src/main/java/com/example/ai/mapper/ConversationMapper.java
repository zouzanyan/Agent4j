package com.example.ai.mapper;

import com.example.ai.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话Mapper接口
 */
@Mapper
public interface ConversationMapper {

    /**
     * 插入会话
     *
     * @param conversation 会话实体
     * @return 影响行数
     */
    int insert(Conversation conversation);

    /**
     * 根据用户ID查询会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Conversation> listByUserId(@Param("userId") String userId);

    /**
     * 根据ID查询会话
     *
     * @param id 会话ID
     * @return 会话实体
     */
    Conversation selectById(@Param("id") Long id);
}
