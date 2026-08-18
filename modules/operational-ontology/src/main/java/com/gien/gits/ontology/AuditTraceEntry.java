package com.gien.gits.ontology;

import java.time.Instant;
import java.util.Map;

/**
 * 审计追踪条目
 *
 * @param traceId        追踪ID
 * @param entityType     实体类型
 * @param entityId       实体ID
 * @param operation      操作类型
 * @param beforeSnapshot 操作前快照
 * @param afterSnapshot  操作后快照
 * @param actorId        操作人ID
 * @param actorRole      操作人角色
 * @param occurredAt     发生时间
 * @param correlationId  关联ID
 */
public record AuditTraceEntry(
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
) {}
