package com.gien.gits.api.controller;

import com.gien.gits.api.dto.AuditTraceEntryDto;
import com.gien.gits.ontology.port.AuditTraceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-trace")
public class AuditTraceController {

    private static final Logger log = LoggerFactory.getLogger(AuditTraceController.class);

    private final AuditTraceRepository auditTraceRepository;

    public AuditTraceController(AuditTraceRepository auditTraceRepository) {
        this.auditTraceRepository = auditTraceRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditTraceEntryDto>> listAuditTrace(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {

        log.info("Listing audit trace: entityType={}, entityId={}, actorId={}", entityType, entityId, actorId);

        List<com.gien.gits.ontology.AuditTraceEntry> entries;
        if (entityType != null && entityId != null) {
            entries = auditTraceRepository.findByEntityTypeAndEntityId(entityType, entityId);
        } else if (actorId != null) {
            entries = auditTraceRepository.findByActorId(actorId);
        } else if (from != null && to != null) {
            entries = auditTraceRepository.findByTimeRange(from, to);
        } else {
            entries = auditTraceRepository.findAll();
        }

        var dtos = entries.stream().map(AuditTraceEntryDto::from).toList();
        return ResponseEntity.ok(dtos);
    }
}
