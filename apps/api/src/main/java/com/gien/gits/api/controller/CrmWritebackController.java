package com.gien.gits.api.controller;

import com.gien.gits.adapter.persistence.JdbcCrmWritebackCommandRepository;
import com.gien.gits.adapter.persistence.entity.CrmWritebackCommandEntity;
import com.gien.gits.api.dto.CrmWritebackCommandDto;
import com.gien.gits.api.dto.CrmWritebackDecisionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm/writeback-commands")
public class CrmWritebackController {

    private static final Logger log = LoggerFactory.getLogger(CrmWritebackController.class);

    private final JdbcCrmWritebackCommandRepository repository;

    public CrmWritebackController(JdbcCrmWritebackCommandRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<CrmWritebackCommandDto>> listCrmWritebackCommands(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String journeyId,
            @RequestParam(required = false) String customerId) {

        log.info("Listing CRM writeback commands: status={}, journeyId={}, customerId={}",
                status, journeyId, customerId);

        List<CrmWritebackCommandEntity> commands;
        if (status != null) {
            commands = repository.findByStatus(status);
        } else if (journeyId != null) {
            commands = repository.findByJourneyId(journeyId);
        } else if (customerId != null) {
            commands = repository.findByCustomerId(customerId);
        } else {
            commands = repository.findAll();
        }

        var dtos = commands.stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{commandId}")
    public ResponseEntity<CrmWritebackCommandDto> getCrmWritebackCommand(@PathVariable String commandId) {
        log.info("Getting CRM writeback command: commandId={}", commandId);
        return repository.findById(commandId)
                .map(cmd -> ResponseEntity.ok(toDto(cmd)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{commandId}/decide")
    public ResponseEntity<CrmWritebackCommandDto> decideCrmWritebackCommand(
            @PathVariable String commandId,
            @RequestBody CrmWritebackDecisionRequest request) {

        log.info("Deciding CRM writeback command: commandId={}, decision={}, actor={}",
                commandId, request.decision(), request.actorId());

        var updated = repository.decide(
                commandId, request.decision(), request.modifications(),
                request.reason(), request.actorId());

        return ResponseEntity.ok(toDto(updated));
    }

    private CrmWritebackCommandDto toDto(CrmWritebackCommandEntity cmd) {
        return new CrmWritebackCommandDto(
                cmd.commandId(), cmd.journeyId(), cmd.customerId(), cmd.operatingCaseId(),
                cmd.operation(), cmd.targetEntity(), cmd.payload(), cmd.status().name(),
                cmd.humanConfirmationRequired(),
                cmd.decision() != null ? cmd.decision().name() : null,
                cmd.modifications(), cmd.decisionReason(), cmd.actorId(),
                cmd.createdAt(), cmd.decidedAt(), cmd.sentAt()
        );
    }
}
