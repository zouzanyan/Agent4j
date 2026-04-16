package com.example.ai.config;

import com.example.ai.service.McpService;
import com.example.ai.service.impl.LangChain4jService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LLM服务配置
 * 使用 LangChain4j 原生 OpenAI 方式调用，支持 MCP 工具（多服务器）
 */
@Slf4j
@Configuration
public class LlmConfig {

    /**
     * LangChain4j原生OpenAI实现，集成 MCP 工具（支持多服务器）
     */
    @Bean
    public LangChain4jService langChain4jService(
            @Value("${langchain4j.open-ai.api-key}") String apiKey,
            @Value("${langchain4j.open-ai.model-name}") String modelName,
            @Value("${langchain4j.open-ai.base-url:}") String baseUrl,
            McpService mcpService) {
        log.info("Using LangChain4j native OpenAI implementation with HTTP/1.1 and MCP multi-server support");
        LangChain4jService service = new LangChain4jService(apiKey, modelName, baseUrl);

        // 注入 MCP 服务（支持多服务器）
        if (mcpService != null && !mcpService.getMcpClients().isEmpty()) {
            service.setMcpService(mcpService);
            log.info("MCP Service injected with {} clients", mcpService.getMcpClients().size());
        } else {
            log.info("MCP Service not available or no clients configured");
        }

        return service;
    }
}
