package com.gien.gits.engagement.port;

/**
 * LLM调用抽象端口，支持mock/真实切换。
 * 调用失败时应抛出LlmClientException，由调用方决定fallback策略。
 */
public interface LlmClient {

    /**
     * 调用LLM完成文本生成
     *
     * @param systemPrompt 系统提示词，定义LLM角色和行为
     * @param userPrompt   用户提示词，包含具体请求内容
     * @return LLM生成的文本响应
     * @throws LlmClientException 调用失败时抛出
     */
    String complete(String systemPrompt, String userPrompt);
}
