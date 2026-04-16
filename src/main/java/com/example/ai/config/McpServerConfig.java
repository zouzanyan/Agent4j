package com.example.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP 服务器配置类
 * 支持配置多个 MCP 服务器
 */
@Data
@Component
@ConfigurationProperties(prefix = "mcp")
public class McpServerConfig {

    private boolean enabled = false;
    private List<McpServer> servers = new ArrayList<>();

    /**
     * 单个 MCP 服务器配置
     */
    @Data
    public static class McpServer {
        private String name;
        private String url;
    }
}
