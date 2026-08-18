package com.gien.gits.ontology.port;

import com.gien.gits.ontology.AuditTraceEntry;

import java.time.Instant;
import java.util.List;

/**
 * 审计追踪 Port 接口
 */
public interface AuditTraceRepository {
    AuditTraceEntry save(AuditTraceEntry entry);
    List<AuditTraceEntry> findByEntityTypeAndEntityId(String entityType, String entityId);
    List<AuditTraceEntry> findByActorId(String actorId);
    List<AuditTraceEntry> findByTimeRange(Instant from, Instant to);
    List<AuditTraceEntry> findAll();
}
