package com.gien.gits.adapter.crm;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.engagement.CrmWritebackCommand;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HttpCrmWritebackChannel WireMock测试 — 验证CRM HTTP回写行为。
 */
class HttpCrmWritebackChannelTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private HttpCrmWritebackChannel channel;
    private BusinessMetrics businessMetrics;

    @BeforeEach
    void setUp() {
        businessMetrics = mock(BusinessMetrics.class);
    }

    private CrmWritebackCommand testCommand() {
        return new CrmWritebackCommand(
                "cmd-001",
                CrmWritebackCommand.ObjectType.INTERACTION,
                CrmWritebackCommand.Operation.UPDATE,
                "before-val",
                "proposed-val",
                CrmWritebackCommand.RiskLevel.LOW,
                true,
                "RM-ACTION-001",
                "audit-ref-001",
                "idempotency-key-001"
        );
    }

    private String getWritebackUrl() {
        return "http://localhost:" + wireMock.getPort() + "/api/crm/writeback";
    }

    @Test
    @DisplayName("成功回写返回success结果")
    void send_success_returnsSuccess() {
        channel = new HttpCrmWritebackChannel(
                org.springframework.web.client.RestClient.builder(), getWritebackUrl(), "", 30, businessMetrics);

        wireMock.stubFor(post("/api/crm/writeback")
                .willReturn(okJson("{\"status\":\"accepted\"}")));

        var result = channel.send(testCommand());
        assertTrue(result.success());
        assertNotNull(result.messageId());
        verify(businessMetrics).recordCrmWriteback("http", "success");
    }

    @Test
    @DisplayName("500错误返回failed结果")
    void send_serverError_returnsFailed() {
        channel = new HttpCrmWritebackChannel(
                org.springframework.web.client.RestClient.builder(), getWritebackUrl(), "", 30, businessMetrics);

        wireMock.stubFor(post("/api/crm/writeback")
                .willReturn(serverError()));

        var result = channel.send(testCommand());
        assertFalse(result.success());
        assertTrue(result.detail().contains("Server error"));
        verify(businessMetrics).recordCrmWriteback("http", "failed");
    }

    @Test
    @DisplayName("401错误返回failed结果")
    void send_unauthorized_returnsFailed() {
        channel = new HttpCrmWritebackChannel(
                org.springframework.web.client.RestClient.builder(), getWritebackUrl(), "", 30, businessMetrics);

        wireMock.stubFor(post("/api/crm/writeback")
                .willReturn(unauthorized()));

        var result = channel.send(testCommand());
        assertFalse(result.success());
        assertTrue(result.detail().contains("Client error"));
        verify(businessMetrics).recordCrmWriteback("http", "failed");
    }

    @Test
    @DisplayName("认证令牌在请求头中发送")
    void send_withAuthToken_sendsAuthorizationHeader() {
        channel = new HttpCrmWritebackChannel(
                org.springframework.web.client.RestClient.builder(), getWritebackUrl(), "my-secret-token", 30, businessMetrics);

        wireMock.stubFor(post("/api/crm/writeback")
                .willReturn(okJson("{\"status\":\"accepted\"}")));

        channel.send(testCommand());

        wireMock.verify(postRequestedFor(urlEqualTo("/api/crm/writeback"))
                .withHeader("Authorization", equalTo("Bearer my-secret-token")));
    }

    @Test
    @DisplayName("幂等键在请求头中发送")
    void send_sendsIdempotencyKeyHeader() {
        channel = new HttpCrmWritebackChannel(
                org.springframework.web.client.RestClient.builder(), getWritebackUrl(), "", 30, businessMetrics);

        wireMock.stubFor(post("/api/crm/writeback")
                .willReturn(okJson("{\"status\":\"accepted\"}")));

        channel.send(testCommand());

        wireMock.verify(postRequestedFor(urlEqualTo("/api/crm/writeback"))
                .withHeader("X-Idempotency-Key", equalTo("idempotency-key-001")));
    }

    @Test
    @DisplayName("无认证令牌时不发送Authorization头")
    void send_noAuthToken_noAuthorizationHeader() {
        channel = new HttpCrmWritebackChannel(
                org.springframework.web.client.RestClient.builder(), getWritebackUrl(), "", 30, businessMetrics);

        wireMock.stubFor(post("/api/crm/writeback")
                .willReturn(okJson("{\"status\":\"accepted\"}")));

        channel.send(testCommand());

        wireMock.verify(postRequestedFor(urlEqualTo("/api/crm/writeback"))
                .withoutHeader("Authorization"));
    }
}
