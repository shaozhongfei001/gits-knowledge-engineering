package com.gien.gits.adapter.oracle;

import com.gien.gits.adapter.oracle.JdbcOracleSourceAdapter;
import com.gien.gits.adapter.oracle.JdbcOracleSourceAdapter.HealthCheckResult;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.gien.gits.api.health.OracleHealthIndicator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Oracle集成测试 — 使用staging profile验证真实Oracle连接。
 * 需要设置环境变量: ORACLE_JDBC_URL, ORACLE_USER, ORACLE_PASSWORD
 * 仅在ORACLE_INTEGRATION_TEST=true时执行。
 */
@SpringBootTest
@ActiveProfiles("staging")
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "ORACLE_INTEGRATION_TEST", matches = "true")
class OracleIntegrationTest {

    @Autowired(required = false)
    private JdbcOracleSourceAdapter oracleAdapter;

    @Autowired(required = false)
    private OracleHealthIndicator oracleHealthIndicator;

    @Test
    @DisplayName("Oracle适配器在staging环境下应被注入")
    void oracleAdapterShouldBeInjected() {
        assertThat(oracleAdapter).as("JdbcOracleSourceAdapter should be injected in staging profile")
                .isNotNull();
    }

    @Test
    @DisplayName("Oracle健康检查应返回成功")
    void healthCheckShouldReturnHealthy() {
        assertThat(oracleAdapter).isNotNull();
        HealthCheckResult result = oracleAdapter.checkHealth();
        assertThat(result.healthy())
                .as("Oracle health check should pass with valid connection")
                .isTrue();
        assertThat(result.detail()).isNotBlank();
    }

    @Test
    @DisplayName("OracleHealthIndicator应报告UP状态")
    void healthIndicatorShouldReportUp() {
        assertThat(oracleHealthIndicator).isNotNull();
        var health = oracleHealthIndicator.health();
        assertThat(health.getStatus())
                .as("Oracle health indicator should report UP when connection is valid")
                .isEqualTo(Status.UP);
    }

    @Test
    @DisplayName("Oracle适配器应能读取索赔数据")
    void shouldReadClaimsFromOracle() {
        assertThat(oracleAdapter).isNotNull();
        assertThat(oracleAdapter.isAvailable()).isTrue();

        // 读取最近7天的数据
        java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(7);
        var claims = oracleAdapter.readClaims(since);
        assertThat(claims).as("Should be able to read claims without exception").isNotNull();
    }
}
