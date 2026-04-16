package com.example.ai.service.impl;

import com.example.ai.service.LlmService;
import com.example.ai.service.McpService;
import com.example.ai.service.StreamHandler;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * LangChain4j原生OpenAI方式调用 - 1.13.0版本
 * 使用 HTTP/1.1 版本以兼容部分模型服务器
 * 支持 MCP (Model Context Protocol) 工具调用（多服务器）
 */
@Slf4j
public class LangChain4jService implements LlmService {

    private final OpenAiChatModel chatLanguageModel;
    private final OpenAiStreamingChatModel streamingChatLanguageModel;
    private McpService mcpService;
    private List<ToolSpecification> mcpTools;

    public LangChain4jService(String apiKey, String modelName, String baseUrl) {
        // 重要：必须自定义 HttpClientBuilder 并设置 HTTP/1.1 版本
        // 原因：LangChain4j 默认使用 HTTP/2，但部分私有部署的模型服务器（如 Qwen）
        // 对 HTTP/2 的支持不完善，会导致同步请求返回 400 错误：
        // "Field required" 或 "Invalid request body"
        // 而流式请求通常不受影响（因为流式使用不同的传输机制）
        // 解决方案：强制使用 HTTP/1.1 以确保兼容性
        // 相关依赖：langchain4j-http-client-jdk
        HttpClientBuilder httpClientBuilder = new JdkHttpClientBuilder()
                .httpClientBuilder(HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(60)));

        OpenAiChatModel.OpenAiChatModelBuilder chatBuilder = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .httpClientBuilder(httpClientBuilder);

        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder streamingBuilder = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .httpClientBuilder(httpClientBuilder);

        if (baseUrl != null && !baseUrl.isEmpty()) {
            chatBuilder.baseUrl(baseUrl);
            streamingBuilder.baseUrl(baseUrl);
        }

        this.chatLanguageModel = chatBuilder.build();
        this.streamingChatLanguageModel = streamingBuilder.build();

        log.info("LangChain4jService initialized with HTTP/1.1. baseUrl={}, model={}", baseUrl, modelName);
    }

    /**
     * 设置 MCP 服务和工具列表（支持多服务器）
     */
    public void setMcpService(McpService mcpService) {
        this.mcpService = mcpService;
        this.mcpTools = mcpService.listAllTools();
        if (mcpTools != null && !mcpTools.isEmpty()) {
            log.info("MCP Service set with {} tools from multiple servers", mcpTools.size());
        }
    }

    @Override
    public String chat(java.util.List<ChatMessage> messages) {
        log.debug("Sending {} messages to LLM via LangChain4j", messages.size());

        // 如果有 MCP 工具，使用工具调用模式
        if (mcpService != null && mcpTools != null && !mcpTools.isEmpty()) {
            return chatWithTools(messages);
        }

        ChatResponse response = chatLanguageModel.chat(messages);
        return response.aiMessage().text();
    }

    /**
     * 使用 MCP 工具进行对话
     */
    private String chatWithTools(java.util.List<ChatMessage> messages) {
        // 创建带工具的聊天请求
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                .toolSpecifications(mcpTools)
                .build();

        ChatResponse response = chatLanguageModel.chat(chatRequest);

        // 检查是否需要调用工具
        List<ToolExecutionRequest> toolRequests = response.aiMessage().toolExecutionRequests();
        if (toolRequests != null && !toolRequests.isEmpty()) {

            log.info("AI requested {} tool executions", toolRequests.size());

            for (ToolExecutionRequest toolRequest : toolRequests) {
                log.info("Executing tool: {}", toolRequest.name());
                
                // 使用 McpClient 执行工具
                String result = executeMcpTool(toolRequest);
                log.debug("Tool result: {}", result);

                // 将工具结果添加到消息列表
                messages.add(response.aiMessage());
                messages.add(dev.langchain4j.data.message.ToolExecutionResultMessage.from(
                        toolRequest.id(), toolRequest.name(), result));
            }

            // 再次调用模型获取最终回复
            ChatRequest finalRequest = ChatRequest.builder()
                    .messages(messages)
                    .build();
            ChatResponse finalResponse = chatLanguageModel.chat(finalRequest);
            return finalResponse.aiMessage().text();
        }

        return response.aiMessage().text();
    }

    /**
     * 执行 MCP 工具调用（支持多服务器）
     */
    private String executeMcpTool(ToolExecutionRequest toolRequest) {
        try {
            // 根据工具名称查找对应的 MCP 客户端
            McpClient client = mcpService.findClientForTool(toolRequest.name());
            
            if (client == null) {
                log.error("No MCP client found for tool: {}", toolRequest.name());
                return "Error: No MCP client available for tool " + toolRequest.name();
            }
            
            // 通过 McpClient 执行工具调用
            // executeTool 方法返回 ToolExecutionResult 对象
            ToolExecutionResult result = client.executeTool(toolRequest);
            return result.resultText();
        } catch (Exception e) {
            log.error("Failed to execute MCP tool: {}", toolRequest.name(), e);
            return "Error executing tool: " + e.getMessage();
        }
    }

    @Override
    public void chatStream(java.util.List<ChatMessage> messages, StreamHandler handler) {
        log.debug("Sending {} messages to LLM via LangChain4j (streaming)", messages.size());

        streamingChatLanguageModel.chat(messages, new StreamingChatResponseHandler() {
            private final StringBuilder fullResponse = new StringBuilder();

            @Override
            public void onPartialResponse(String partialResponse) {
                if (partialResponse != null && !partialResponse.isEmpty()) {
                    fullResponse.append(partialResponse);
                    handler.onToken(partialResponse);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                handler.onComplete(fullResponse.toString());
            }

            @Override
            public void onError(Throwable error) {
                log.error("Error in streaming chat via LangChain4j", error);
                handler.onError(error);
            }
        });
    }
}
