package com.gien.gits.api.controller;

import com.gien.gits.api.service.ExternalEventService;
import com.gien.gits.ontology.ExternalEvent;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 外部事件控制器 — 监管变化、市场事件管理
 */
@RestController
@RequestMapping("/api/v1/external-events")
public class ExternalEventController {

    private final ExternalEventService externalEventService;

    public ExternalEventController(ExternalEventService externalEventService) {
        this.externalEventService = Objects.requireNonNull(externalEventService);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ExternalEvent> getById(@PathVariable String eventId) {
        return externalEventService.findByEventId(eventId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(params = "eventType")
    public ResponseEntity<List<ExternalEvent>> getByEventType(@RequestParam String eventType) {
        return ResponseEntity.ok(externalEventService.findByEventType(eventType));
    }

    @GetMapping(params = "customerId")
    public ResponseEntity<List<ExternalEvent>> getByCustomerId(@RequestParam String customerId) {
        return ResponseEntity.ok(externalEventService.findByAffectedCustomerId(customerId));
    }

    @GetMapping(params = "industry")
    public ResponseEntity<List<ExternalEvent>> getByIndustry(@RequestParam String industry) {
        return ResponseEntity.ok(externalEventService.findByAffectedIndustry(industry));
    }

    @GetMapping(params = "severity")
    public ResponseEntity<List<ExternalEvent>> getBySeverity(@RequestParam String severity) {
        return ResponseEntity.ok(externalEventService.findBySeverity(severity));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ExternalEvent>> getRecent(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(externalEventService.findRecent(limit));
    }

    @GetMapping
    public ResponseEntity<List<ExternalEvent>> listAll() {
        return ResponseEntity.ok(externalEventService.findAll());
    }

    @PostMapping
    public ResponseEntity<ExternalEvent> create(@RequestBody ExternalEvent event) {
        return ResponseEntity.status(HttpStatus.CREATED).body(externalEventService.create(event));
    }
}
