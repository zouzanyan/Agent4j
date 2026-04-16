package com.example.ai.service;

/**
 * 流式响应处理器接口
 */
public interface StreamHandler {

    /**
     * 接收到新token时的回调
     *
     * @param token token内容
     */
    void onToken(String token);

    /**
     * 流式响应完成时的回调
     *
     * @param fullResponse 完整响应内容
     */
    void onComplete(String fullResponse);

    /**
     * 发生错误时的回调
     *
     * @param error 错误信息
     */
    void onError(Throwable error);
}
