package com.gien.gits.api.dto;

import com.gien.gits.ontology.AuditTraceEntry;

import java.time.Instant;
import java.util.Map;

/**
 * 审计追踪条目 API DTO
 */
public record AuditTraceEntryDto(
        String traceId,
        String entityType,
        String entityId,
        String operation,
        Map<String, Object> beforeSnapshot,
        Map<String, Object> afterSnapshot,
        String actorId,
        String actorRole,
        Instant occurredAt,
        String correlationId
) {
    public static AuditTraceEntryDto from(AuditTraceEntry entry) {
        return new AuditTraceEntryDto(
                entry.traceId(), entry.entityType(), entry.entityId(), entry.operation(),
                entry.beforeSnapshot(), entry.afterSnapshot(), entry.actorId(),
                entry.actorRole(), entry.occurredAt(), entry.correlationId()
        );
    }
}
