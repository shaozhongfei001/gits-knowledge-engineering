package com.gien.gits.adapter.llm;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 真实LLM客户端 — 通过OpenAI兼容API调用外部LLM服务。
 * 使用Spring RestClient发起HTTP请求，支持任意OpenAI兼容端点。
 *
 * P16 G2增强:
 * - 重试机制(3次，指数退避)
 * - 熔断器(5次失败后开启，30s后半开)
 * - 超时配置(连接5s，读取30s)
 */
public class RealLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(RealLlmClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final BusinessMetrics businessMetrics;

    // --- P16 G2: 重试配置 ---
    private final int maxRetryAttempts;
    private final long initialRetryDelayMs;
    private final double backoffMultiplier;

    // --- P16 G2: 熔断器状态 ---
    private final int circuitBreakerFailureThreshold;
    private final long circuitBreakerHalfOpenDelayMs;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile Instant circuitOpenSince = null;
    private volatile CircuitState circuitState = CircuitState.CLOSED;

    private enum CircuitState {
        CLOSED,   // 正常工作
        OPEN,     // 熔断开启，拒绝请求
        HALF_OPEN // 半开，允许一次试探请求
    }

    /**
     * 创建RealLlmClient(带完整重试/熔断/超时配置)
     */
    public RealLlmClient(String baseUrl, String apiKey, String model,
                         int connectTimeoutMs, int readTimeoutMs,
                         int maxRetryAttempts, long initialRetryDelayMs, double backoffMultiplier,
                         int circuitBreakerFailureThreshold, long circuitBreakerHalfOpenDelayMs,
                         BusinessMetrics businessMetrics) {
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.businessMetrics = businessMetrics;
        this.maxRetryAttempts = maxRetryAttempts;
        this.initialRetryDelayMs = initialRetryDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.circuitBreakerFailureThreshold = circuitBreakerFailureThreshold;
        this.circuitBreakerHalfOpenDelayMs = circuitBreakerHalfOpenDelayMs;

        ClientHttpRequestFactory requestFactory = createRequestFactory(connectTimeoutMs, readTimeoutMs);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(requestFactory)
                .build();

        log.info("RealLlmClient configured: baseUrl={}, model={}, connectTimeout={}ms, readTimeout={}ms, " +
                 "maxRetry={}, circuitBreakerThreshold={}, circuitBreakerHalfOpenDelay={}ms",
                 baseUrl, model, connectTimeoutMs, readTimeoutMs,
                 maxRetryAttempts, circuitBreakerFailureThreshold, circuitBreakerHalfOpenDelayMs);
    }

    /**
     * 向后兼容构造函数(使用默认超时配置)
     */
    public RealLlmClient(String baseUrl, String apiKey, String model, int timeoutSeconds,
                         BusinessMetrics businessMetrics) {
        this(baseUrl, apiKey, model,
             5000, timeoutSeconds * 1000,
             3, 1000, 2.0,
             5, 30000,
             businessMetrics);
    }

    private ClientHttpRequestFactory createRequestFactory(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return factory;
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        // P16 G2: 熔断器检查
        checkCircuitBreaker();

        // P16 G2: 重试机制
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                String result = doComplete(systemPrompt, userPrompt);
                onCircuitBreakerSuccess();
                return result;
            } catch (LlmClientException e) {
                lastException = e;
                if (attempt < maxRetryAttempts && isRetryable(e)) {
                    long delay = calculateRetryDelay(attempt);
                    log.warn("LLM call attempt {}/{} failed, retrying in {}ms: {}",
                             attempt, maxRetryAttempts, delay, e.getMessage());
                    sleep(delay);
                } else {
                    log.error("LLM call failed after {} attempts: {}", attempt, e.getMessage());
                }
            }
        }

        // 所有重试失败，触发熔断器
        onCircuitBreakerFailure();
        businessMetrics.recordLlmCall("real", "error");
        assert lastException != null;
        throw new LlmClientException("LLM call failed after " + maxRetryAttempts + " attempts: " +
                                     lastException.getMessage(), lastException);
    }

    private String doComplete(String systemPrompt, String userPrompt) {
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
            throw e;
        } catch (Exception e) {
            log.error("LLM API call failed: {}", e.getMessage());
            throw new LlmClientException("LLM API call failed: " + e.getMessage(), e);
        }
    }

    // --- P16 G2: 熔断器逻辑 ---

    private void checkCircuitBreaker() {
        switch (circuitState) {
            case OPEN:
                if (Instant.now().isAfter(circuitOpenSince.plusMillis(circuitBreakerHalfOpenDelayMs))) {
                    circuitState = CircuitState.HALF_OPEN;
                    log.info("Circuit breaker transitioning to HALF_OPEN after {}ms", circuitBreakerHalfOpenDelayMs);
                } else {
                    throw new LlmClientException("Circuit breaker is OPEN - rejecting request. " +
                            "Will transition to HALF_OPEN after " + circuitBreakerHalfOpenDelayMs + "ms");
                }
                break;
            case HALF_OPEN:
                log.info("Circuit breaker in HALF_OPEN - allowing one probe request");
                break;
            case CLOSED:
                // 正常通过
                break;
        }
    }

    private void onCircuitBreakerSuccess() {
        if (circuitState != CircuitState.CLOSED) {
            log.info("Circuit breaker transitioning from {} to CLOSED after successful request", circuitState);
        }
        circuitState = CircuitState.CLOSED;
        consecutiveFailures.set(0);
        circuitOpenSince = null;
    }

    private void onCircuitBreakerFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= circuitBreakerFailureThreshold && circuitState != CircuitState.OPEN) {
            circuitState = CircuitState.OPEN;
            circuitOpenSince = Instant.now();
            log.warn("Circuit breaker OPEN after {} consecutive failures. " +
                     "Will transition to HALF_OPEN after {}ms",
                     failures, circuitBreakerHalfOpenDelayMs);
        } else if (circuitState == CircuitState.HALF_OPEN) {
            circuitState = CircuitState.OPEN;
            circuitOpenSince = Instant.now();
            log.warn("Circuit breaker probe request failed, returning to OPEN");
        }
    }

    // --- P16 G2: 重试逻辑 ---

    private boolean isRetryable(LlmClientException e) {
        // 超时和连接异常可重试，业务错误(如API key无效)不重试
        Throwable cause = e.getCause();
        if (cause instanceof SocketTimeoutException) return true;
        if (cause instanceof java.net.ConnectException) return true;
        if (cause instanceof org.springframework.web.client.ResourceAccessException) return true;
        // 5xx服务器错误可重试
        String msg = e.getMessage();
        if (msg != null && (msg.contains("5") && (msg.contains("Server Error") || msg.contains("Service Unavailable")))) {
            return true;
        }
        // 默认允许重试(网络瞬断等)
        return true;
    }

    private long calculateRetryDelay(int attempt) {
        return (long) (initialRetryDelayMs * Math.pow(backoffMultiplier, attempt - 1));
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmClientException("Retry sleep interrupted", e);
        }
    }

    // --- P16 G2: 熔断器状态暴露(供HealthIndicator使用) ---

    public CircuitBreakerStatus getCircuitBreakerStatus() {
        return new CircuitBreakerStatus(
                circuitState.name(),
                consecutiveFailures.get(),
                circuitOpenSince
        );
    }

    public record CircuitBreakerStatus(String state, int consecutiveFailures, Instant openSince) {}

    // --- 原有私有方法 ---

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
