package com.gien.gits.adapter.crm;

import com.gien.gits.action.port.CrmWritebackChannel;
import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.engagement.CrmWritebackCommand;
import com.gien.gits.engagement.CrmWritebackCommand.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * LoggingCrmWritebackChannel 行为测试
 * 
 * 验证:
 * - send始终返回成功（日志通道不会失败）
 * - 返回的messageId以"LOG-"前缀开头
 * - 每次send记录BusinessMetrics
 */
class LoggingCrmWritebackChannelTest {

    private BusinessMetrics businessMetrics;
    private LoggingCrmWritebackChannel channel;

    @BeforeEach
    void setUp() {
        businessMetrics = mock(BusinessMetrics.class);
        channel = new LoggingCrmWritebackChannel(businessMetrics);
    }

    @Test
    @DisplayName("send → 始终返回成功结果")
    void send_alwaysSucceeds() {
        CrmWritebackCommand cmd = new CrmWritebackCommand(
            "CMD-001", ObjectType.INTERACTION, Operation.CREATE,
            null, "value", RiskLevel.LOW,
            true, "review", "audit-001", "idempotency-key-12345678");

        CrmWritebackChannel.WritebackResult result = channel.send(cmd);

        assertThat(result.success()).isTrue();
    }

    @Test
    @DisplayName("send → 返回的messageId以LOG-前缀开头")
    void send_messageIdStartsWithLog() {
        CrmWritebackCommand cmd = new CrmWritebackCommand(
            "CMD-001", ObjectType.INTERACTION, Operation.CREATE,
            null, "value", RiskLevel.LOW,
            true, "review", "audit-001", "idempotency-key-12345678");

        CrmWritebackChannel.WritebackResult result = channel.send(cmd);

        assertThat(result.messageId()).startsWith("LOG-");
    }

    @Test
    @DisplayName("send → 记录BusinessMetrics")
    void send_recordsMetrics() {
        CrmWritebackCommand cmd = new CrmWritebackCommand(
            "CMD-001", ObjectType.INTERACTION, Operation.CREATE,
            null, "value", RiskLevel.LOW,
            true, "review", "audit-001", "idempotency-key-12345678");

        channel.send(cmd);

        verify(businessMetrics).recordCrmWriteback("logging", "success");
    }

    @Test
    @DisplayName("多次send → 每次都记录metrics且messageId不同（幂等性）")
    void multipleSends_eachRecordsMetricsAndDifferentMessageId() {
        CrmWritebackCommand cmd = new CrmWritebackCommand(
            "CMD-001", ObjectType.INTERACTION, Operation.CREATE,
            null, "value", RiskLevel.LOW,
            true, "review", "audit-001", "idempotency-key-12345678");

        CrmWritebackChannel.WritebackResult r1 = channel.send(cmd);
        CrmWritebackChannel.WritebackResult r2 = channel.send(cmd);

        assertThat(r1.messageId()).isNotEqualTo(r2.messageId());
        verify(businessMetrics, times(2)).recordCrmWriteback("logging", "success");
    }
}
