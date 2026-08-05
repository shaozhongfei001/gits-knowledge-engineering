package com.gien.gits.adapter.crm;

import com.gien.gits.action.port.CrmWritebackChannel;
import com.gien.gits.action.port.CrmWritebackChannel.WritebackResult;
import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.engagement.CrmWritebackCommand;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HttpCrmWritebackChannel集成测试 — 验证超时、重试、HTTP状态码处理和fallback。
 * 使用WireMock模拟CRM HTTP端点。
 */
@WireMockTest(httpPort = 9998)
@Tag("integration")
class HttpCrmWritebackChannelIntegrationTest {

    private static final String WRITEBACK_URL = "http://localhost:9998/api/crm/writeback";
    private static final String AUTH_TOKEN = "test-crm-token";

    private BusinessMetrics businessMetrics;

    @BeforeEach
    void setUp() {
        businessMetrics = new BusinessMetrics(new SimpleMeterRegistry());
    }

    private HttpCrmWritebackChannel createChannel(int connectTimeoutMs, int readTimeoutMs,
                                                   int maxRetryAttempts, long retryDelayMs) {
        return new HttpCrmWritebackChannel(
                org.springframework.web.client.RestClient.builder(),
                WRITEBACK_URL, AUTH_TOKEN,
                connectTimeoutMs, readTimeoutMs,
                maxRetryAttempts, retryDelayMs,
                businessMetrics);
    }

    private CrmWritebackCommand createTestCommand() {
        return new CrmWritebackCommand(
                "CMD-TEST-001",
                CrmWritebackCommand.ObjectType.CUSTOMER,
                CrmWritebackCommand.Operation.UPDATE,
                "old-value",
                "new-value",
                CrmWritebackCommand.RiskLevel.MEDIUM,
                true,  // requiresHumanConfirm must be true
                "RM-ACTION-001",
                "AUDIT-REF-001",
                "IDEMPOTENCY-KEY-12345678"  // >= 16 chars
        );
    }

    @Test
    @DisplayName("成功发送CRM回写应返回成功结果")
    void shouldReturnSuccessOnValidWriteback() {
        stubFor(post(urlEqualTo("/api/crm/writeback"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"accepted\"}")));

        HttpCrmWritebackChannel channel = createChannel(5000, 5000, 2, 100);
        WritebackResult result = channel.send(createTestCommand());

        assertThat(result.success()).isTrue();
        assertThat(result.messageId()).startsWith("HTTP-CMD-TEST-001");
    }

    @Test
    @DisplayName("4xx客户端错误不应重试")
    void shouldNotRetryOn4xxClientError() {
        stubFor(post(urlEqualTo("/api/crm/writeback"))
                .willReturn(aResponse().withStatus(400).withBody("Bad Request")));

        HttpCrmWritebackChannel channel = createChannel(5000, 5000, 3, 100);
        WritebackResult result = channel.send(createTestCommand());

        assertThat(result.success()).isFalse();
        assertThat(result.detail()).contains("Client error");

        // 验证只调用了一次(不重试)
        verify(1, postRequestedFor(urlEqualTo("/api/crm/writeback")));
    }

    @Test
    @DisplayName("401未授权不应重试")
    void shouldNotRetryOn401Unauthorized() {
        stubFor(post(urlEqualTo("/api/crm/writeback"))
                .willReturn(aResponse().withStatus(401).withBody("Unauthorized")));

        HttpCrmWritebackChannel channel = createChannel(5000, 5000, 3, 100);
        WritebackResult result = channel.send(createTestCommand());

        assertThat(result.success()).isFalse();
        assertThat(result.detail()).contains("Client error");
        verify(1, postRequestedFor(urlEqualTo("/api/crm/writeback")));
    }

    @Test
    @DisplayName("5xx服务器错误应重试")
    void shouldRetryOn5xxServerError() {
        // 第一次500，第二次200
        stubFor(post(urlEqualTo("/api/crm/writeback"))
                .inScenario("5xx-retry")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error"))
                .willSetStateTo("Retried"));

        stubFor(post(urlEqualTo("/api/crm/writeback"))
                .inScenario("5xx-retry")
                .whenScenarioStateIs("Retried")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"accepted\"}")));

        HttpCrmWritebackChannel channel = createChannel(5000, 5000, 3, 50);
        WritebackResult result = channel.send(createTestCommand());

        assertThat(result.success()).isTrue();
        verify(2, postRequestedFor(urlEqualTo("/api/crm/writeback")));
    }

    @Test
    @DisplayName("503服务不可用应重试")
    void shouldRetryOn503ServiceUnavailable() {
        stubFor(post(urlEqualTo("/api/crm/writeback"))
                .inScenario("503-retry")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(503).withBody("Service Unavailable"))
                .willSetStateTo("Retried"));

        stubFor(post(urlEqualTo("/api/crm/writeback"))
                .inScenario("503-retry")
                .whenScenarioStateIs("Retried")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"accepted\"}")));

        HttpCrmWritebackChannel channel = createChannel(5000, 5000, 2, 50);
        WritebackResult result = channel.send(createTestCommand());

        assertThat(result.success()).isTrue();
    }

    @Test
    @DisplayName("重试耗尽后应返回失败结果")
    void shouldReturnFailedAfterRetriesExhausted() {
        stubFor(post(urlEqualTo("/api/crm/writeback"))
                .willReturn(aResponse().withStatus(500).withBody("Server Error")));

        HttpCrmWritebackChannel channel = createChannel(5000, 5000, 2, 50);
        WritebackResult result = channel.send(createTestCommand());

        assertThat(result.success()).isFalse();
        assertThat(result.detail()).contains("Failed after 2 attempts");
    }

    @Test
    @DisplayName("读取超时应触发重试")
    void shouldRetryOnReadTimeout() {
        // 第一次延迟超过读取超时
        stubFor(post(urlEqualTo("/api/crm/writeback"))
                .inScenario("timeout")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withFixedDelay(2000))
                .willSetStateTo("Timed Out"));

        stubFor(post(urlEqualTo("/api/crm/writeback"))
                .inScenario("timeout")
                .whenScenarioStateIs("Timed Out")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"accepted\"}")));

        // 读取超时500ms
        HttpCrmWritebackChannel channel = createChannel(2000, 500, 3, 50);
        WritebackResult result = channel.send(createTestCommand());

        assertThat(result.success()).isTrue();
    }

    @Test
    @DisplayName("Fallback: LoggingCrmWritebackChannel应正常工作")
    void loggingChannelShouldWorkAsFallback() {
        LoggingCrmWritebackChannel loggingChannel = new LoggingCrmWritebackChannel(businessMetrics);
        WritebackResult result = loggingChannel.send(createTestCommand());

        assertThat(result.success()).isTrue();
        assertThat(result.messageId()).startsWith("LOG-");
    }

    @Test
    @DisplayName("应携带认证令牌和幂等键")
    void shouldIncludeAuthTokenAndIdempotencyKey() {
        stubFor(post(urlEqualTo("/api/crm/writeback"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"accepted\"}")));

        HttpCrmWritebackChannel channel = createChannel(5000, 5000, 1, 100);
        channel.send(createTestCommand());

        verify(postRequestedFor(urlEqualTo("/api/crm/writeback"))
                .withHeader("Authorization", equalTo("Bearer " + AUTH_TOKEN))
                .withHeader("X-Idempotency-Key", equalTo("IDEMPOTENCY-KEY-12345678")));
    }
}
