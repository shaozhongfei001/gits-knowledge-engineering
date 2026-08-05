package com.gien.gits.adapter.llm;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.engagement.port.LlmClientException;

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
 * RealLlmClient集成测试 — 验证重试、熔断器、超时和fallback逻辑。
 * 使用WireMock模拟外部LLM API。
 */
@WireMockTest(httpPort = 9999)
@Tag("integration")
class RealLlmClientIntegrationTest {

    private static final String BASE_URL = "http://localhost:9999";
    private static final String API_KEY = "test-api-key";
    private static final String MODEL = "gpt-4o-mini";

    private BusinessMetrics businessMetrics;

    @BeforeEach
    void setUp() {
        businessMetrics = new BusinessMetrics(new SimpleMeterRegistry());
    }

    private RealLlmClient createClient(int connectTimeoutMs, int readTimeoutMs,
                                        int maxRetryAttempts, long initialRetryDelayMs,
                                        int cbFailureThreshold, long cbHalfOpenDelayMs) {
        return new RealLlmClient(BASE_URL, API_KEY, MODEL,
                connectTimeoutMs, readTimeoutMs,
                maxRetryAttempts, initialRetryDelayMs, 2.0,
                cbFailureThreshold, cbHalfOpenDelayMs,
                businessMetrics);
    }

    @Test
    @DisplayName("成功调用LLM API应返回内容")
    void shouldReturnContentOnSuccess() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "choices": [{
                                "message": {"content": "Hello from LLM"}
                              }]
                            }
                            """)));

        RealLlmClient client = createClient(5000, 5000, 3, 100, 5, 30000);
        String result = client.complete("system", "user");
        assertThat(result).isEqualTo("Hello from LLM");
    }

    @Test
    @DisplayName("重试机制：首次失败后重试成功")
    void shouldRetryOnTransientFailure() {
        // 第一次返回500，第二次返回成功
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .inScenario("retry")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Failed Once"));

        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .inScenario("retry")
                .whenScenarioStateIs("Failed Once")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "choices": [{
                                "message": {"content": "Retry success"}
                              }]
                            }
                            """)));

        RealLlmClient client = createClient(5000, 5000, 3, 50, 5, 30000);
        String result = client.complete("system", "user");
        assertThat(result).isEqualTo("Retry success");
    }

    @Test
    @DisplayName("重试耗尽后应抛出LlmClientException")
    void shouldThrowAfterRetriesExhausted() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse().withStatus(500).withBody("Server Error")));

        RealLlmClient client = createClient(5000, 5000, 2, 50, 5, 30000);
        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(LlmClientException.class)
                .hasMessageContaining("failed after 2 attempts");
    }

    @Test
    @DisplayName("熔断器：连续失败达到阈值后应开启")
    void shouldOpenCircuitBreakerAfterConsecutiveFailures() {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse().withStatus(500).withBody("Server Error")));

        // 阈值=2，快速触发
        RealLlmClient client = createClient(5000, 5000, 1, 10, 2, 60000);

        // 触发2次失败
        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(LlmClientException.class);
        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(LlmClientException.class);

        // 熔断器应已开启
        var cbStatus = client.getCircuitBreakerStatus();
        assertThat(cbStatus.state()).isEqualTo("OPEN");
        assertThat(cbStatus.consecutiveFailures()).isGreaterThanOrEqualTo(2);

        // 第3次调用应被熔断器拒绝
        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(LlmClientException.class)
                .hasMessageContaining("Circuit breaker is OPEN");
    }

    @Test
    @DisplayName("熔断器：半开状态下成功请求应关闭熔断器")
    void shouldCloseCircuitBreakerOnHalfOpenSuccess() throws InterruptedException {
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse().withStatus(500).withBody("Server Error")));

        // 阈值=2，半开延迟=100ms(测试用短时间)
        RealLlmClient client = createClient(5000, 5000, 1, 10, 2, 100);

        // 触发熔断
        assertThatThrownBy(() -> client.complete("system", "user")).isInstanceOf(LlmClientException.class);
        assertThatThrownBy(() -> client.complete("system", "user")).isInstanceOf(LlmClientException.class);
        assertThat(client.getCircuitBreakerStatus().state()).isEqualTo("OPEN");

        // 等待半开延迟
        Thread.sleep(150);

        // 模拟恢复
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "choices": [{
                                "message": {"content": "Recovered"}
                              }]
                            }
                            """)));

        String result = client.complete("system", "user");
        assertThat(result).isEqualTo("Recovered");
        assertThat(client.getCircuitBreakerStatus().state()).isEqualTo("CLOSED");
    }

    @Test
    @DisplayName("超时配置：读取超时应触发重试")
    void shouldRetryOnReadTimeout() {
        // 第一次延迟超过读取超时，第二次快速返回
        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .inScenario("timeout")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withFixedDelay(2000))  // 2s延迟 > 500ms超时
                .willSetStateTo("Timed Out"));

        stubFor(post(urlEqualTo("/v1/chat/completions"))
                .inScenario("timeout")
                .whenScenarioStateIs("Timed Out")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "choices": [{
                                "message": {"content": "After timeout retry"}
                              }]
                            }
                            """)));

        // 读取超时500ms，重试3次
        RealLlmClient client = createClient(2000, 500, 3, 50, 5, 30000);
        String result = client.complete("system", "user");
        assertThat(result).isEqualTo("After timeout retry");
    }

    @Test
    @DisplayName("Fallback：RealLlmClient失败时MockLlmClient应正常工作")
    void mockLlmClientShouldWorkAsFallback() {
        MockLlmClient mockClient = new MockLlmClient(businessMetrics);
        String result = mockClient.complete("system prompt", "user prompt");
        assertThat(result).isNotBlank();
        // MockLlmClient返回结构化JSON
        assertThat(result).contains("response");
    }
}
