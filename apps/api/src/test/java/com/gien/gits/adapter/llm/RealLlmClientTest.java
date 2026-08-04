package com.gien.gits.adapter.llm;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.engagement.port.LlmClientException;
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
 * RealLlmClient WireMock测试 — 验证OpenAI兼容API调用行为。
 */
class RealLlmClientTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private RealLlmClient client;
    private BusinessMetrics businessMetrics;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + wireMock.getPort();
        businessMetrics = mock(BusinessMetrics.class);
        client = new RealLlmClient(baseUrl, "test-api-key", "gpt-4o-mini", 30, businessMetrics);
    }

    @Test
    @DisplayName("成功调用LLM API返回内容")
    void complete_success_returnsContent() {
        wireMock.stubFor(post("/v1/chat/completions")
                .willReturn(okJson("""
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "Hello from LLM!"
                              }
                            }
                          ]
                        }
                        """)));

        String result = client.complete("You are a helper", "Say hello");
        assertEquals("Hello from LLM!", result);

        wireMock.verify(postRequestedFor(urlEqualTo("/v1/chat/completions"))
                .withHeader("Authorization", equalTo("Bearer test-api-key"))
                .withHeader("Content-Type", equalTo("application/json")));
        verify(businessMetrics).recordLlmCall("real", "success");
    }

    @Test
    @DisplayName("API返回500错误时抛出LlmClientException")
    void complete_serverError_throwsLlmClientException() {
        wireMock.stubFor(post("/v1/chat/completions")
                .willReturn(serverError()));

        assertThrows(LlmClientException.class, () ->
                client.complete("You are a helper", "Say hello"));
        verify(businessMetrics).recordLlmCall("real", "error");
    }

    @Test
    @DisplayName("API返回401错误时抛出LlmClientException")
    void complete_unauthorized_throwsLlmClientException() {
        wireMock.stubFor(post("/v1/chat/completions")
                .willReturn(unauthorized()));

        assertThrows(LlmClientException.class, () ->
                client.complete("You are a helper", "Say hello"));
        verify(businessMetrics).recordLlmCall("real", "error");
    }

    @Test
    @DisplayName("API返回空choices时抛出LlmClientException")
    void complete_emptyChoices_throwsLlmClientException() {
        wireMock.stubFor(post("/v1/chat/completions")
                .willReturn(okJson("""
                        {
                          "choices": []
                        }
                        """)));

        assertThrows(LlmClientException.class, () ->
                client.complete("You are a helper", "Say hello"));
        verify(businessMetrics).recordLlmCall("real", "error");
    }

    @Test
    @DisplayName("请求体包含正确的model和messages")
    void complete_requestBodyContainsModelAndMessages() {
        wireMock.stubFor(post("/v1/chat/completions")
                .willReturn(okJson("""
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "OK"
                              }
                            }
                          ]
                        }
                        """)));

        client.complete("system prompt", "user prompt");

        wireMock.verify(postRequestedFor(urlEqualTo("/v1/chat/completions"))
                .withRequestBody(containing("\"model\":\"gpt-4o-mini\""))
                .withRequestBody(containing("\"role\":\"system\""))
                .withRequestBody(containing("\"content\":\"system prompt\""))
                .withRequestBody(containing("\"role\":\"user\""))
                .withRequestBody(containing("\"content\":\"user prompt\"")));
    }
}
