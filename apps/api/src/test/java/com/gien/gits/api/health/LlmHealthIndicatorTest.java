package com.gien.gits.api.health;

import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * P13 G1: LlmHealthIndicator单元测试
 */
class LlmHealthIndicatorTest {

    @Test
    @DisplayName("mock模式返回UP状态")
    void healthCheck_mockMode_returnsUp() {
        LlmClient llmClient = mock(LlmClient.class);
        LlmHealthIndicator indicator = new LlmHealthIndicator(llmClient, "mock");

        var health = indicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals("mock", health.getDetails().get("mode"));

        // mock模式下不应调用LLM
        verifyNoInteractions(llmClient);
    }

    @Test
    @DisplayName("real模式调用成功返回UP状态")
    void healthCheck_realMode_success_returnsUp() {
        LlmClient llmClient = mock(LlmClient.class);
        when(llmClient.complete(anyString(), anyString())).thenReturn("pong");
        LlmHealthIndicator indicator = new LlmHealthIndicator(llmClient, "real");

        var health = indicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals("real", health.getDetails().get("mode"));
    }

    @Test
    @DisplayName("real模式调用失败返回DOWN状态")
    void healthCheck_realMode_failure_returnsDown() {
        LlmClient llmClient = mock(LlmClient.class);
        when(llmClient.complete(anyString(), anyString())).thenThrow(new LlmClientException("connection refused"));
        LlmHealthIndicator indicator = new LlmHealthIndicator(llmClient, "real");

        var health = indicator.health();
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("real", health.getDetails().get("mode"));
        assertNotNull(health.getDetails().get("error"));
    }
}
