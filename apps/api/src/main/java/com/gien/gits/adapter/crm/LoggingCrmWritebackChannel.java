package com.gien.gits.adapter.crm;

import com.gien.gits.action.port.CrmWritebackChannel;
import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.engagement.CrmWritebackCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 日志回写通道 — 仅记录CRM回写命令到日志，不实际发送。
 * 用于开发和测试环境。
 */
public class LoggingCrmWritebackChannel implements CrmWritebackChannel {

    private static final Logger log = LoggerFactory.getLogger(LoggingCrmWritebackChannel.class);

    private final BusinessMetrics businessMetrics;

    public LoggingCrmWritebackChannel(BusinessMetrics businessMetrics) {
        this.businessMetrics = businessMetrics;
    }

    @Override
    public WritebackResult send(CrmWritebackCommand command) {
        log.info("[CRM-WRITEBACK] commandId={}, objectType={}, operation={}, riskLevel={}, " +
                 "requiresHumanConfirm={}, rmAction={}, idempotencyKey={}",
                 command.commandId(), command.objectType(), command.operation(),
                 command.riskLevel(), command.requiresHumanConfirm(),
                 command.rmAction(), command.idempotencyKey());
        log.debug("[CRM-WRITEBACK] beforeValue={}, proposedValue={}, auditRef={}",
                  command.beforeValue(), command.proposedValue(), command.auditRef());

        String messageId = "LOG-" + UUID.randomUUID().toString().substring(0, 8);
        businessMetrics.recordCrmWriteback("logging", "success");
        return WritebackResult.success(messageId);
    }
}
