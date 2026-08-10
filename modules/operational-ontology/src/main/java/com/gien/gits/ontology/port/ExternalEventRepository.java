package com.gien.gits.ontology.port;

import com.gien.gits.ontology.ExternalEvent;
import java.util.List;
import java.util.Optional;

/**
 * 外部事件仓储端口
 */
public interface ExternalEventRepository {
    Optional<ExternalEvent> findByEventId(String eventId);
    List<ExternalEvent> findByEventType(String eventType);
    List<ExternalEvent> findByAffectedCustomerId(String customerId);
    List<ExternalEvent> findByAffectedIndustry(String industry);
    List<ExternalEvent> findBySeverity(String severity);
    List<ExternalEvent> findByEntity(String entity);
    List<ExternalEvent> findRecent(int limit);
    List<ExternalEvent> findAll();
}
