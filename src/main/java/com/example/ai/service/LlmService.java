package com.example.ai.service;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

/**
 * LLM服务接口
 */
public interface LlmService {

    /**
     * 同步聊天
     *
     * @param messages 消息列表
     * @return AI回复内容
     */
    String chat(List<ChatMessage> messages);

    /**
     * 流式聊天
     *
     * @param messages 消息列表
     * @param handler 流式响应处理器
     */
    void chatStream(List<ChatMessage> messages, StreamHandler handler);
}
