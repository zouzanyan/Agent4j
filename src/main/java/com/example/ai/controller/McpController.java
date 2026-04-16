package com.example.ai.controller;

import com.example.ai.service.McpService;
import dev.langchain4j.agent.tool.ToolSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP (Model Context Protocol) 工具管理接口
 * 支持多服务器配置
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpService mcpService;

    /**
     * 获取所有可用的 MCP 工具列表（聚合所有服务器）
     */
    @GetMapping("/tools")
    public Map<String, Object> listAllTools() {
        List<ToolSpecification> tools = mcpService.listAllTools();

        List<Map<String, Object>> toolList = tools.stream()
                .map(tool -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", tool.name());
                    map.put("description", tool.description());
                    if (tool.parameters() != null) {
                        map.put("parameters", tool.parameters().properties());
                    }
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("count", tools.size());
        result.put("tools", toolList);

        return result;
    }

    /**
     * 获取特定服务器的 MCP 工具列表
     */
    @GetMapping("/tools/{serverName}")
    public Map<String, Object> listToolsByServer(@PathVariable String serverName) {
        var client = mcpService.getMcpClient(serverName);
        Map<String, Object> result = new HashMap<>();

        if (client == null) {
            result.put("error", "Server not found: " + serverName);
            return result;
        }

        try {
            List<ToolSpecification> tools = client.listTools();
            List<Map<String, Object>> toolList = tools.stream()
                    .map(tool -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", tool.name());
                        map.put("description", tool.description());
                        if (tool.parameters() != null) {
                            map.put("parameters", tool.parameters().properties());
                        }
                        return map;
                    })
                    .collect(Collectors.toList());

            result.put("serverName", serverName);
            result.put("count", tools.size());
            result.put("tools", toolList);
        } catch (Exception e) {
            log.error("Failed to list tools from server: {}", serverName, e);
            result.put("error", "Failed to list tools: " + e.getMessage());
        }

        return result;
    }

    /**
     * 检查 MCP 服务状态（包含所有服务器）
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return mcpService.getStatus();
    }

    /**
     * 获取所有配置的 MCP 服务器列表
     */
    @GetMapping("/servers")
    public Map<String, Object> listServers() {
        Map<String, Object> result = new HashMap<>();
        result.put("servers", mcpService.getMcpClients().keySet());
        result.put("count", mcpService.getMcpClients().size());
        return result;
    }
}
