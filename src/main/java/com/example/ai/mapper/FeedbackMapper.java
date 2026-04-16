package com.example.ai.mapper;

import com.example.ai.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 反馈Mapper接口
 */
@Mapper
public interface FeedbackMapper {

    /**
     * 插入反馈
     *
     * @param feedback 反馈实体
     * @return 影响行数
     */
    int insert(Feedback feedback);
}
