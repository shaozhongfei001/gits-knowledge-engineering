package com.gien.gits.api.service;

import com.gien.gits.ontology.ExternalEvent;
import com.gien.gits.ontology.port.WritableExternalEventRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 外部事件服务 — 监管变化、市场事件管理
 */
public class ExternalEventService {

    private final WritableExternalEventRepository externalEventRepo;

    public ExternalEventService(WritableExternalEventRepository externalEventRepo) {
        this.externalEventRepo = Objects.requireNonNull(externalEventRepo);
    }

    public Optional<ExternalEvent> findByEventId(String eventId) {
        return externalEventRepo.findByEventId(eventId);
    }

    public List<ExternalEvent> findByEventType(String eventType) {
        return externalEventRepo.findByEventType(eventType);
    }

    public List<ExternalEvent> findByAffectedCustomerId(String customerId) {
        return externalEventRepo.findByAffectedCustomerId(customerId);
    }

    public List<ExternalEvent> findByAffectedIndustry(String industry) {
        return externalEventRepo.findByAffectedIndustry(industry);
    }

    public List<ExternalEvent> findBySeverity(String severity) {
        return externalEventRepo.findBySeverity(severity);
    }

    public List<ExternalEvent> findRecent(int limit) {
        return externalEventRepo.findRecent(limit);
    }

    public List<ExternalEvent> findAll() {
        return externalEventRepo.findAll();
    }

    public ExternalEvent create(ExternalEvent event) {
        externalEventRepo.save(event);
        return event;
    }
}
