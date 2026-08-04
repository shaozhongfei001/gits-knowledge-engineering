package com.gien.gits.adapter.llm;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

/**
 * 真实LLM客户端 — 通过OpenAI兼容API调用外部LLM服务。
 * 使用Spring RestClient发起HTTP请求，支持任意OpenAI兼容端点。
 */
public class RealLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(RealLlmClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final BusinessMetrics businessMetrics;

    public RealLlmClient(String baseUrl, String apiKey, String model, int timeoutSeconds,
                         BusinessMetrics businessMetrics) {
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.businessMetrics = businessMetrics;
        log.info("RealLlmClient configured: baseUrl={}, model={}", baseUrl, model);
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        try {
            String requestBody = buildRequestBody(systemPrompt, userPrompt);
            log.debug("LLM request body: {}", requestBody);

            String response = restClient.post()
                    .uri("/v1/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            String content = extractContent(response);
            log.debug("LLM response content length: {}", content != null ? content.length() : 0);
            businessMetrics.recordLlmCall("real", "success");
            return content;
        } catch (LlmClientException e) {
            businessMetrics.recordLlmCall("real", "error");
            throw e;
        } catch (Exception e) {
            log.error("LLM API call failed: {}", e.getMessage());
            businessMetrics.recordLlmCall("real", "error");
            throw new LlmClientException("LLM API call failed: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String systemPrompt, String userPrompt) throws Exception {
        var messages = objectMapper.createArrayNode();
        var systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        var userMsg = objectMapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        var body = objectMapper.createObjectNode();
        body.put("model", model);
        body.set("messages", messages);
        body.put("temperature", 0.7);

        return objectMapper.writeValueAsString(body);
    }

    private String extractContent(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode choices = root.path("choices");
        if (choices.isMissingNode() || choices.isEmpty()) {
            throw new LlmClientException("LLM response missing choices: " + response);
        }
        String content = choices.path(0).path("message").path("content").asText();
        if (content == null || content.isEmpty()) {
            throw new LlmClientException("LLM response missing content: " + response);
        }
        return content;
    }
}
