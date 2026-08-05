package com.gien.gits.api.health;

import com.gien.gits.adapter.llm.RealLlmClient;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * P13 G1: LLM健康检查指标
 * - mock模式: UP with detail mode=mock
 * - real模式: 尝试简单调用，成功则UP，失败则DOWN
 * P16 G2: 增加熔断器状态报告
 */
@Component
public class LlmHealthIndicator extends AbstractHealthIndicator {

    private final LlmClient llmClient;
    private final String llmMode;

    public LlmHealthIndicator(LlmClient llmClient,
                              @Value("${engagement.llm.mode:mock}") String llmMode) {
        super("LLM health check failed");
        this.llmClient = llmClient;
        this.llmMode = llmMode;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if ("mock".equalsIgnoreCase(llmMode)) {
            builder.up().withDetail("mode", "mock");
        } else {
            // P16 G2: 报告熔断器状态
            if (llmClient instanceof RealLlmClient realClient) {
                RealLlmClient.CircuitBreakerStatus cbStatus = realClient.getCircuitBreakerStatus();
                builder.withDetail("circuitBreaker", cbStatus.state())
                       .withDetail("consecutiveFailures", cbStatus.consecutiveFailures());
            }
            try {
                llmClient.complete("health check", "ping");
                builder.up().withDetail("mode", "real");
            } catch (LlmClientException e) {
                builder.down().withDetail("mode", "real").withDetail("error", e.getMessage());
            }
        }
    }
}
