package com.gien.gits.api.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P13 G1: CrmHealthIndicator单元测试
 */
class CrmHealthIndicatorTest {

    private final RestClient.Builder restClientBuilder = RestClient.builder();

    @Test
    @DisplayName("logging模式返回UP状态")
    void healthCheck_loggingMode_returnsUp() {
        CrmHealthIndicator indicator = new CrmHealthIndicator("logging", "", restClientBuilder);

        var health = indicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals("logging", health.getDetails().get("mode"));
    }

    @Test
    @DisplayName("http模式URL未配置返回DOWN状态")
    void healthCheck_httpMode_noUrl_returnsDown() {
        CrmHealthIndicator indicator = new CrmHealthIndicator("http", "", restClientBuilder);

        var health = indicator.health();
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("http", health.getDetails().get("mode"));
        assertNotNull(health.getDetails().get("error"));
    }

    @Test
    @DisplayName("http模式URL为空白返回DOWN状态")
    void healthCheck_httpMode_blankUrl_returnsDown() {
        CrmHealthIndicator indicator = new CrmHealthIndicator("http", "   ", restClientBuilder);

        var health = indicator.health();
        assertEquals(Status.DOWN, health.getStatus());
    }
}
