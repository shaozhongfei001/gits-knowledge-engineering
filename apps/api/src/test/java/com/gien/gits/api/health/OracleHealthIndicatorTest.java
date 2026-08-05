package com.gien.gits.api.health;

import com.gien.gits.adapter.oracle.JdbcOracleSourceAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * P17: OracleHealthIndicator单元测试
 */
class OracleHealthIndicatorTest {

    @Test
    @DisplayName("Oracle禁用时返回UP+DISABLED状态")
    void healthCheck_disabled_returnsUpWithDisabled() {
        JdbcOracleSourceAdapter adapter = mock(JdbcOracleSourceAdapter.class);
        OracleHealthIndicator indicator = new OracleHealthIndicator(adapter, false);

        var health = indicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals("DISABLED", health.getDetails().get("status"));

        verifyNoInteractions(adapter);
    }

    @Test
    @DisplayName("Oracle启用且健康时返回UP状态")
    void healthCheck_enabledAndHealthy_returnsUp() {
        JdbcOracleSourceAdapter adapter = mock(JdbcOracleSourceAdapter.class);
        when(adapter.checkHealth()).thenReturn(new JdbcOracleSourceAdapter.HealthCheckResult(true, "Oracle connection is healthy"));
        OracleHealthIndicator indicator = new OracleHealthIndicator(adapter, true);

        var health = indicator.health();
        assertEquals(Status.UP, health.getStatus());
        assertEquals("UP", health.getDetails().get("status"));
        assertEquals("Oracle connection is healthy", health.getDetails().get("detail"));
    }

    @Test
    @DisplayName("Oracle启用但不健康时返回DOWN状态")
    void healthCheck_enabledAndUnhealthy_returnsDown() {
        JdbcOracleSourceAdapter adapter = mock(JdbcOracleSourceAdapter.class);
        when(adapter.checkHealth()).thenReturn(new JdbcOracleSourceAdapter.HealthCheckResult(false, "Oracle connection failed"));
        OracleHealthIndicator indicator = new OracleHealthIndicator(adapter, true);

        var health = indicator.health();
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("DOWN", health.getDetails().get("status"));
        assertEquals("Oracle connection failed", health.getDetails().get("detail"));
    }
}
