package com.example.ai.config;

import com.example.ai.agent.AiAgent;
import com.example.ai.agent.Tools;
import com.example.ai.service.McpService;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * AI Agent 配置类
 * 使用 LangChain4j AiServices 构建智能体
 */
@Slf4j
@Configuration
public class AiAgentConfig {

    /**
     * 创建 ChatMemory（对话记忆）
     * 使用 MessageWindowChatMemory 保留最近的消息
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(20);
    }

    /**
     * 创建 OpenAI Chat Model
     */
    @Bean
    public OpenAiChatModel chatModel(
            @Value("${langchain4j.open-ai.api-key}") String apiKey,
            @Value("${langchain4j.open-ai.model-name}") String modelName,
            @Value("${langchain4j.open-ai.base-url:}") String baseUrl) {

        // 使用 HTTP/1.1 以确保兼容性
        HttpClientBuilder httpClientBuilder = new JdkHttpClientBuilder()
                .httpClientBuilder(HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(60)));

        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .httpClientBuilder(httpClientBuilder);

        if (baseUrl != null && !baseUrl.isEmpty()) {
            builder.baseUrl(baseUrl);
        }

        log.info("OpenAiChatModel initialized with model: {}, baseUrl: {}", modelName, baseUrl);
        return builder.build();
    }

    /**
     * 创建 AI Agent
     * 使用 AiServices.builder 自动处理工具调用
     */
    @Bean
    public AiAgent aiAgent(OpenAiChatModel chatModel, 
                          ChatMemory chatMemory, 
                          Tools tools,
                          McpService mcpService) {
        
        log.info("Building AiAgent with AiServices...");

        // 构建 AiServices
        AiServices<AiAgent> builder = AiServices.builder(AiAgent.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory);

        // 注册本地工具（使用 @Tool 注解的方法）
        builder.tools(tools);

        // 如果启用了 MCP，注册 MCP 工具
        if (mcpService != null && !mcpService.getMcpClients().isEmpty()) {
            var mcpTools = mcpService.listAllTools();
            if (!mcpTools.isEmpty()) {
                // 使用 McpToolProvider 让 LangChain4j 自动处理 MCP 工具
                var toolProvider = dev.langchain4j.mcp.McpToolProvider.builder()
                        .mcpClients(mcpService.getMcpClients().values().toArray(new dev.langchain4j.mcp.client.McpClient[0]))
                        .build();
                builder.toolProvider(toolProvider);
                log.info("MCP tools registered: {} tools from {} servers", 
                        mcpTools.size(), mcpService.getMcpClients().size());
            }
        }

        AiAgent agent = builder.build();
        log.info("AiAgent built successfully");
        
        return agent;
    }
}
