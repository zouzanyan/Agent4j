package com.example.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI Agent 接口
 * 使用 LangChain4j AiServices 自动处理工具调用
 */
public interface AiAgent {

    /**
     * 与用户进行对话
     * 自动处理工具调用，无需手动干预
     */
    @SystemMessage("""
            你是一个智能AI助手，可以帮助用户解答问题。
            当需要获取实时信息或执行特定操作时，请使用可用的工具。
            请用中文回复用户。
            """)
    String chat(@UserMessage String message);

    /**
     * 带上下文的对话
     * @param conversationHistory 对话历史（格式化的上下文）
     * @param message 当前用户消息
     */
    @SystemMessage("""
            你是一个智能AI助手，可以帮助用户解答问题。
            请参考对话历史来理解上下文。
            当需要获取实时信息或执行特定操作时，请使用可用的工具。
            请用中文回复用户。
            """)
    String chatWithContext(@V("history") String conversationHistory, @UserMessage String message);

    // 流式（核心）- 返回 TokenStream，由调用方设置回调
    TokenStream chatStream(@UserMessage String message);
}
