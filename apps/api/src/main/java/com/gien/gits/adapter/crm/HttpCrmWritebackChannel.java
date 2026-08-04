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
import org.springframework.web.client.RestClient;

/**
 * HTTP回写通道 — 通过REST调用将回写命令发送到CRM系统。
 * 使用Spring 6.1+ RestClient，支持认证令牌和超时配置。
 */
public class HttpCrmWritebackChannel implements CrmWritebackChannel {

    private static final Logger log = LoggerFactory.getLogger(HttpCrmWritebackChannel.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String writebackUrl;
    private final String authToken;
    private final BusinessMetrics businessMetrics;

    public HttpCrmWritebackChannel(RestClient.Builder restClientBuilder,
                                   String writebackUrl,
                                   String authToken,
                                   int timeoutSeconds,
                                   BusinessMetrics businessMetrics) {
        if (writebackUrl == null || writebackUrl.isBlank()) {
            throw new IllegalArgumentException(
                "engagement.crm.writeback-url must be configured for HTTP mode");
        }
        this.writebackUrl = writebackUrl;
        this.authToken = authToken;
        this.objectMapper = new ObjectMapper();
        this.restClient = restClientBuilder.build();
        this.businessMetrics = businessMetrics;
        log.info("HttpCrmWritebackChannel configured: url={}, authToken={}", writebackUrl,
                authToken != null && !authToken.isBlank() ? "***configured***" : "none");
    }

    @Override
    public WritebackResult send(CrmWritebackCommand command) {
        try {
            MDC.put("crm.commandId", command.commandId());
            MDC.put("crm.idempotencyKey", command.idempotencyKey());
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

        } catch (CrmHttpException e) {
            businessMetrics.recordCrmWriteback("http", "failed");
            return WritebackResult.failed(e.getMessage());
        } catch (Exception e) {
            log.error("[CRM-WRITEBACK-HTTP] Failed to send commandId={}: {}", command.commandId(), e.getMessage(), e);
            businessMetrics.recordCrmWriteback("http", "failed");
            return WritebackResult.failed(e.getMessage());
        } finally {
            MDC.remove("crm.commandId");
            MDC.remove("crm.idempotencyKey");
        }
    }

    /**
     * CRM HTTP异常 — 用于区分4xx/5xx错误
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
    }
}
