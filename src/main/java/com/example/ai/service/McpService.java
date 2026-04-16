package com.example.ai.service;

import com.example.ai.config.McpServerConfig;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP (Model Context Protocol) 服务
 * 支持配置多个 MCP 服务器
 */
@Slf4j
@Service
public class McpService {

    private final McpServerConfig mcpServerConfig;
    private final Map<String, McpClient> mcpClients = new HashMap<>();

    public McpService(McpServerConfig mcpServerConfig) {
        this.mcpServerConfig = mcpServerConfig;
    }

    /**
     * 初始化所有 MCP 客户端
     */
    @PostConstruct
    public void init() {
        if (!mcpServerConfig.isEnabled()) {
            log.info("MCP is disabled");
            return;
        }

        List<McpServerConfig.McpServer> servers = mcpServerConfig.getServers();
        if (servers == null || servers.isEmpty()) {
            log.warn("MCP is enabled but no servers configured");
            return;
        }

        for (McpServerConfig.McpServer server : servers) {
            if (server.getUrl() == null || server.getUrl().isEmpty()) {
                log.warn("MCP server '{}' has no URL configured, skipping", server.getName());
                continue;
            }

            try {
                // 创建 HTTP SSE 传输层
                HttpMcpTransport transport = new HttpMcpTransport.Builder()
                        .sseUrl(server.getUrl())
                        .timeout(Duration.ofSeconds(60))
                        .build();

                // 创建 MCP 客户端
                McpClient mcpClient = new DefaultMcpClient.Builder()
                        .transport(transport)
                        .build();

                mcpClients.put(server.getName(), mcpClient);
                log.info("MCP client '{}' initialized successfully with server: {}",
                        server.getName(), server.getUrl());

            } catch (Exception e) {
                log.error("Failed to initialize MCP client '{}' with URL: {}",
                        server.getName(), server.getUrl(), e);
            }
        }

        log.info("MCP Service initialized with {} clients", mcpClients.size());
    }

    /**
     * 获取所有 MCP 客户端
     */
    public Map<String, McpClient> getMcpClients() {
        return new HashMap<>(mcpClients);
    }

    /**
     * 获取单个 MCP 客户端
     */
    public McpClient getMcpClient(String name) {
        return mcpClients.get(name);
    }

    /**
     * 列出所有可用的 MCP 工具（聚合所有服务器的工具）
     */
    public List<ToolSpecification> listAllTools() {
        List<ToolSpecification> allTools = new ArrayList<>();

        for (Map.Entry<String, McpClient> entry : mcpClients.entrySet()) {
            String serverName = entry.getKey();
            McpClient client = entry.getValue();

            try {
                List<ToolSpecification> tools = client.listTools();
                // 为工具添加来源标记（可选）
                log.debug("MCP server '{}' provides {} tools", serverName, tools.size());
                allTools.addAll(tools);
            } catch (Exception e) {
                log.error("Failed to list tools from MCP server '{}'", serverName, e);
            }
        }

        return allTools;
    }

    /**
     * 根据工具名称查找对应的 MCP 客户端
     * 用于执行工具时找到正确的客户端
     */
    public McpClient findClientForTool(String toolName) {
        for (Map.Entry<String, McpClient> entry : mcpClients.entrySet()) {
            McpClient client = entry.getValue();
            try {
                List<ToolSpecification> tools = client.listTools();
                for (ToolSpecification tool : tools) {
                    if (tool.name().equals(toolName)) {
                        return client;
                    }
                }
            } catch (Exception e) {
                log.error("Failed to check tools from MCP server '{}'", entry.getKey(), e);
            }
        }
        return null;
    }

    /**
     * 获取 MCP 服务状态
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", mcpServerConfig.isEnabled());
        status.put("clientCount", mcpClients.size());

        Map<String, Object> clientsStatus = new HashMap<>();
        for (Map.Entry<String, McpClient> entry : mcpClients.entrySet()) {
            String serverName = entry.getKey();
            try {
                List<ToolSpecification> tools = entry.getValue().listTools();
                Map<String, Object> clientStatus = new HashMap<>();
                clientStatus.put("toolCount", tools.size());
                clientStatus.put("connected", true);
                clientsStatus.put(serverName, clientStatus);
            } catch (Exception e) {
                Map<String, Object> clientStatus = new HashMap<>();
                clientStatus.put("error", e.getMessage());
                clientStatus.put("connected", false);
                clientsStatus.put(serverName, clientStatus);
            }
        }
        status.put("clients", clientsStatus);

        return status;
    }

    /**
     * 关闭所有 MCP 客户端
     */
    @PreDestroy
    public void shutdown() {
        for (Map.Entry<String, McpClient> entry : mcpClients.entrySet()) {
            try {
                entry.getValue().close();
                log.info("MCP client '{}' closed", entry.getKey());
            } catch (Exception e) {
                log.error("Error closing MCP client '{}'", entry.getKey(), e);
            }
        }
        mcpClients.clear();
    }
}
