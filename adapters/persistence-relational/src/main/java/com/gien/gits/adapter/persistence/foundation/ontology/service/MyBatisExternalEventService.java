package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.dto.ExternalEventRow;
import com.gien.gits.adapter.persistence.foundation.ontology.mapper.ExternalEventMapper;
import com.gien.gits.ontology.ExternalEvent;
import com.gien.gits.ontology.port.WritableExternalEventRepository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 外部事件仓储实现 — foundation/ontology 层
 */
public class MyBatisExternalEventService implements WritableExternalEventRepository {

    private final ExternalEventMapper mapper;

    public MyBatisExternalEventService(ExternalEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(ExternalEvent event) {
        mapper.insert(event);
    }

    @Override
    public Optional<ExternalEvent> findByEventId(String eventId) {
        return mapper.findRowByEventId(eventId)
                .map(ExternalEventRow::toExternalEvent);
    }

    @Override
    public List<ExternalEvent> findByEventType(String eventType) {
        return mapper.findRowsByEntity(eventType).stream()
                .map(ExternalEventRow::toExternalEvent)
                .toList();
    }

    @Override
    public List<ExternalEvent> findByAffectedCustomerId(String customerId) {
        return mapper.findAllRows().stream()
                .map(ExternalEventRow::toExternalEvent)
                .filter(e -> e.entity() != null && e.entity().contains(customerId))
                .toList();
    }

    @Override
    public List<ExternalEvent> findByAffectedIndustry(String industry) {
        return List.of();
    }

    @Override
    public List<ExternalEvent> findBySeverity(String severity) {
        return List.of();
    }

    @Override
    public List<ExternalEvent> findByEntity(String entity) {
        return mapper.findRowsByEntity(entity).stream()
                .map(ExternalEventRow::toExternalEvent)
                .toList();
    }

    @Override
    public List<ExternalEvent> findRecent(int limit) {
        return mapper.findRecentRows(limit).stream()
                .map(ExternalEventRow::toExternalEvent)
                .toList();
    }

    @Override
    public List<ExternalEvent> findAll() {
        return mapper.findAllRows().stream()
                .map(ExternalEventRow::toExternalEvent)
                .toList();
    }
}
