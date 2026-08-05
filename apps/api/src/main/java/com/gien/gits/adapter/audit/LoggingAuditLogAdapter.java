package com.gien.gits.adapter.audit;

import com.gien.gits.action.port.AuditLogPort;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 审计日志适配器 — 基于SLF4J的日志记录实现。
 * 生产环境应替换为数据库持久化或外部审计系统推送。
 */
public class LoggingAuditLogAdapter implements AuditLogPort {

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");

    @Override
    public void log(String action, String actor, String resource, String outcome,
                    Map<String, Object> details, Instant timestamp) {
        AUDIT_LOGGER.info("[AUDIT] action={} actor={} resource={} outcome={} timestamp={} details={}",
                action, actor, resource, outcome, timestamp, details);
    }
}
