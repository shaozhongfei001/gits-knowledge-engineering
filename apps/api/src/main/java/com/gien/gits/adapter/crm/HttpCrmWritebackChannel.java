package com.gien.gits.adapter.crm;

import com.gien.gits.action.port.CrmWritebackChannel;
import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.engagement.CrmWritebackCommand;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * HTTP回写通道 — 通过REST调用将回写命令发送到CRM系统。
 * 使用Spring 6.1+ RestClient，支持认证令牌和超时配置。
 *
 * P16 G3增强:
 * - 超时配置(5s连接，10s读取)
 * - 重试机制(2次)
 * - HTTP状态码精细处理(4xx不重试，5xx重试)
 */
public class HttpCrmWritebackChannel implements CrmWritebackChannel {

    private static final Logger log = LoggerFactory.getLogger(HttpCrmWritebackChannel.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String writebackUrl;
    private final String authToken;
    private final BusinessMetrics businessMetrics;

    // P16 G3: 重试配置
    private final int maxRetryAttempts;
    private final long retryDelayMs;

    public HttpCrmWritebackChannel(RestClient.Builder restClientBuilder,
                                   String writebackUrl,
                                   String authToken,
                                   int connectTimeoutMs,
                                   int readTimeoutMs,
                                   int maxRetryAttempts,
                                   long retryDelayMs,
                                   BusinessMetrics businessMetrics) {
        if (writebackUrl == null || writebackUrl.isBlank()) {
            throw new IllegalArgumentException(
                "engagement.crm.writeback-url must be configured for HTTP mode");
        }
        this.writebackUrl = writebackUrl;
        this.authToken = authToken;
        this.objectMapper = new ObjectMapper();
        this.maxRetryAttempts = maxRetryAttempts;
        this.retryDelayMs = retryDelayMs;
        this.businessMetrics = businessMetrics;

        ClientHttpRequestFactory requestFactory = createRequestFactory(connectTimeoutMs, readTimeoutMs);
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();

        log.info("HttpCrmWritebackChannel configured: url={}, authToken={}, connectTimeout={}ms, " +
                 "readTimeout={}ms, maxRetry={}, retryDelay={}ms",
                 writebackUrl,
                 authToken != null && !authToken.isBlank() ? "***configured***" : "none",
                 connectTimeoutMs, readTimeoutMs, maxRetryAttempts, retryDelayMs);
    }

    /**
     * 向后兼容构造函数(使用默认超时配置)
     */
    public HttpCrmWritebackChannel(RestClient.Builder restClientBuilder,
                                   String writebackUrl,
                                   String authToken,
                                   int timeoutSeconds,
                                   BusinessMetrics businessMetrics) {
        this(restClientBuilder, writebackUrl, authToken,
             5000, timeoutSeconds * 1000,
             2, 500,
             businessMetrics);
    }

    private ClientHttpRequestFactory createRequestFactory(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return factory;
    }

    @Override
    public WritebackResult send(CrmWritebackCommand command) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                MDC.put("crm.commandId", command.commandId());
                MDC.put("crm.idempotencyKey", command.idempotencyKey());
                MDC.put("crm.attempt", String.valueOf(attempt));

                WritebackResult result = doSend(command);
                if (result.success()) {
                    return result;
                }

                // P16 G3: 4xx客户端错误不重试
                if (lastException instanceof CrmHttpException crmEx && crmEx.isClientError()) {
                    log.warn("[CRM-WRITEBACK-HTTP] Client error ({}), not retrying: commandId={}",
                             crmEx.getStatusCode(), command.commandId());
                    return result;
                }

                lastException = new RuntimeException(result.detail());

            } catch (CrmHttpException e) {
                lastException = e;
                // P16 G3: 4xx客户端错误不重试
                if (e.isClientError()) {
                    log.warn("[CRM-WRITEBACK-HTTP] Client error ({}), not retrying: commandId={}",
                             e.getStatusCode(), command.commandId());
                    businessMetrics.recordCrmWriteback("http", "failed");
                    return WritebackResult.failed(e.getMessage());
                }

                // 5xx服务器错误可重试
                if (attempt < maxRetryAttempts) {
                    log.warn("[CRM-WRITEBACK-HTTP] Server error ({}), attempt {}/{}: commandId={}, retrying in {}ms",
                             e.getStatusCode(), attempt, maxRetryAttempts, command.commandId(), retryDelayMs);
                    sleep(retryDelayMs);
                }
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetryAttempts) {
                    log.warn("[CRM-WRITEBACK-HTTP] Attempt {}/{} failed: commandId={}, retrying in {}ms: {}",
                             attempt, maxRetryAttempts, command.commandId(), retryDelayMs, e.getMessage());
                    sleep(retryDelayMs);
                }
            } finally {
                MDC.remove("crm.commandId");
                MDC.remove("crm.idempotencyKey");
                MDC.remove("crm.attempt");
            }
        }

        // 所有重试耗尽
        log.error("[CRM-WRITEBACK-HTTP] All {} attempts failed for commandId={}",
                  maxRetryAttempts, command.commandId());
        businessMetrics.recordCrmWriteback("http", "failed");
        String detail = lastException != null ? lastException.getMessage() : "Unknown error";
        return WritebackResult.failed("Failed after " + maxRetryAttempts + " attempts: " + detail);
    }

    private WritebackResult doSend(CrmWritebackCommand command) throws Exception {
        log.info("[CRM-WRITEBACK-HTTP] Sending commandId={} objectType={} operation={} to {}",
                command.commandId(), command.objectType(), command.operation(), writebackUrl);

        String jsonBody = objectMapper.writeValueAsString(command);

        var requestSpec = restClient.post()
                .uri(writebackUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", command.idempotencyKey());

        if (authToken != null && !authToken.isBlank()) {
            requestSpec = requestSpec.header("Authorization", "Bearer " + authToken);
        }

        String response = requestSpec
                .body(jsonBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                    String msg = "Client error: " + resp.getStatusCode().value();
                    log.error("[CRM-WRITEBACK-HTTP] {} for commandId={}", msg, command.commandId());
                    throw new CrmHttpException(msg, resp.getStatusCode().value());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                    String msg = "Server error: " + resp.getStatusCode().value();
                    log.error("[CRM-WRITEBACK-HTTP] {} for commandId={}", msg, command.commandId());
                    throw new CrmHttpException(msg, resp.getStatusCode().value());
                })
                .body(String.class);

        log.info("[CRM-WRITEBACK-HTTP] Response for commandId={}: {}", command.commandId(), response);
        String messageId = "HTTP-" + command.commandId();
        businessMetrics.recordCrmWriteback("http", "success");
        return WritebackResult.success(messageId);
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * CRM HTTP异常 — 用于区分4xx/5xx错误
     * P16 G3: 增加isClientError/isServerError判断
     */
    static class CrmHttpException extends RuntimeException {
        private final int statusCode;

        CrmHttpException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        int getStatusCode() {
            return statusCode;
        }

        boolean isClientError() {
            return statusCode >= 400 && statusCode < 500;
        }

        boolean isServerError() {
            return statusCode >= 500 && statusCode < 600;
        }
    }
}
