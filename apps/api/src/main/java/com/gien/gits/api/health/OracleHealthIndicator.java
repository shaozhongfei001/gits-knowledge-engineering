package com.gien.gits.api.health;

import com.gien.gits.adapter.oracle.JdbcOracleSourceAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Oracle数据源健康指标 — 检查Oracle连接是否可用。
 * 在staging/production环境中，fail-closed守卫依赖此指标判断连接状态。
 * 仅在JdbcOracleSourceAdapter存在时注册。
 */
@Component
@ConditionalOnBean(JdbcOracleSourceAdapter.class)
public class OracleHealthIndicator extends AbstractHealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(OracleHealthIndicator.class);

    private final JdbcOracleSourceAdapter oracleAdapter;
    private final boolean oracleSourceEnabled;

    public OracleHealthIndicator(JdbcOracleSourceAdapter oracleAdapter,
                                 @Value("${oracle.source.enabled:false}") boolean oracleSourceEnabled) {
        super("Oracle health check failed");
        this.oracleAdapter = oracleAdapter;
        this.oracleSourceEnabled = oracleSourceEnabled;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if (!oracleSourceEnabled) {
            builder.up().withDetail("status", "DISABLED").withDetail("reason", "Oracle source is not enabled");
            return;
        }

        JdbcOracleSourceAdapter.HealthCheckResult result = oracleAdapter.checkHealth();
        if (result.healthy()) {
            builder.up().withDetail("status", "UP").withDetail("detail", result.detail());
        } else {
            log.warn("Oracle health check failed: {}", result.detail());
            builder.down().withDetail("status", "DOWN").withDetail("detail", result.detail());
        }
    }
}
