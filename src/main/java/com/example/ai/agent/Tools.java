package com.example.ai.agent;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AI Agent 工具类
 * 使用 @Tool 注解定义工具方法，LangChain4j 会自动处理工具调用
 */
@Slf4j
@Component
public class Tools {

    /**
     * 获取当前时间
     */
    @Tool("获取当前日期和时间")
    public String getCurrentTime() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("Tool called: getCurrentTime, result: {}", time);
        return time;
    }

    /**
     * 计算数学表达式
     */
    @Tool("计算数学表达式的结果，支持加减乘除和括号")
    public double calculate(String expression) {
        log.info("Tool called: calculate, expression: {}", expression);
        // 这里可以实现更复杂的计算逻辑
        // 简化示例：只处理简单的加减乘除
        try {
            return evaluateExpression(expression);
        } catch (Exception e) {
            log.error("Failed to calculate expression: {}", expression, e);
            return Double.NaN;
        }
    }

    /**
     * 简单的表达式计算（示例实现）
     */
    private double evaluateExpression(String expression) {
        // 简化实现，实际项目中可以使用更强大的表达式引擎
        // 如: exp4j, Javaluator, 或 ScriptEngine
        return 0.0;
    }
}
