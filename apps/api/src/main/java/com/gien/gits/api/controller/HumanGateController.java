package com.gien.gits.api.controller;

import com.gien.gits.api.dto.HumanGateDecisionRequest;
import com.gien.gits.api.dto.HumanGateDto;
import com.gien.gits.ontology.GateType;
import com.gien.gits.ontology.HumanGate;
import com.gien.gits.ontology.HumanGateStatus;
import com.gien.gits.ontology.port.HumanGateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/human-gates")
public class HumanGateController {

    private static final Logger log = LoggerFactory.getLogger(HumanGateController.class);

    private final HumanGateRepository humanGateRepository;

    public HumanGateController(HumanGateRepository humanGateRepository) {
        this.humanGateRepository = humanGateRepository;
    }

    @GetMapping
    public ResponseEntity<List<HumanGateDto>> listHumanGates(
            @RequestParam(required = false) HumanGateStatus status,
            @RequestParam(required = false) GateType gateType,
            @RequestParam(required = false) String journeyId,
            @RequestParam(required = false) String customerId) {

        log.info("Listing human gates: status={}, gateType={}, journeyId={}, customerId={}",
                status, gateType, journeyId, customerId);

        List<HumanGate> gates;
        if (status != null) {
            gates = humanGateRepository.findByStatus(status);
        } else if (gateType != null) {
            gates = humanGateRepository.findByGateType(gateType);
        } else if (journeyId != null) {
            gates = humanGateRepository.findByJourneyId(journeyId);
        } else if (customerId != null) {
            gates = humanGateRepository.findByCustomerId(customerId);
        } else {
            gates = humanGateRepository.findAll();
        }

        var dtos = gates.stream().map(HumanGateDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{gateId}")
    public ResponseEntity<HumanGateDto> getHumanGate(@PathVariable String gateId) {
        log.info("Getting human gate: gateId={}", gateId);
        return humanGateRepository.findById(gateId)
                .map(gate -> ResponseEntity.ok(HumanGateDto.from(gate)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{gateId}/decide")
    public ResponseEntity<HumanGateDto> decideHumanGate(
            @PathVariable String gateId,
            @RequestBody HumanGateDecisionRequest request) {

        log.info("Deciding human gate: gateId={}, decision={}, actor={}",
                gateId, request.decision(), request.actorId());

        var updated = humanGateRepository.decide(
                gateId, request.decision(), request.modification(),
                request.reason(), request.actorId());

        return ResponseEntity.ok(HumanGateDto.from(updated));
    }
}
