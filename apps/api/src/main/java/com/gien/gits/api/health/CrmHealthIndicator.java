package com.gien.gits.api.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * P13 G1: CRM健康检查指标
 * - logging模式: UP with detail mode=logging
 * - http模式: 尝试HTTP健康检查，成功则UP，失败则DOWN
 */
@Component
public class CrmHealthIndicator extends AbstractHealthIndicator {

    private final String crmMode;
    private final String writebackUrl;
    private final RestClient restClient;

    public CrmHealthIndicator(@Value("${engagement.crm.mode:logging}") String crmMode,
                              @Value("${engagement.crm.writeback-url:}") String writebackUrl,
                              RestClient.Builder restClientBuilder) {
        super("CRM health check failed");
        this.crmMode = crmMode;
        this.writebackUrl = writebackUrl;
        this.restClient = restClientBuilder.build();
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if ("logging".equalsIgnoreCase(crmMode)) {
            builder.up().withDetail("mode", "logging");
        } else {
            if (writebackUrl == null || writebackUrl.isBlank()) {
                builder.down().withDetail("mode", "http").withDetail("error", "writeback-url not configured");
                return;
            }
            try {
                // 尝试对CRM根路径做简单GET健康检查
                String healthUrl = writebackUrl.replaceAll("/+$", "") + "/../health";
                restClient.get()
                        .uri(healthUrl)
                        .retrieve()
                        .toBodilessEntity();
                builder.up().withDetail("mode", "http").withDetail("url", writebackUrl);
            } catch (Exception e) {
                builder.down().withDetail("mode", "http").withDetail("error", e.getMessage());
            }
        }
    }
}
