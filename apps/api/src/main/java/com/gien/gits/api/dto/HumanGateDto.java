package com.gien.gits.api.dto;

import com.gien.gits.ontology.GateDecision;
import com.gien.gits.ontology.GateType;
import com.gien.gits.ontology.HumanGate;
import com.gien.gits.ontology.HumanGateStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * HumanGate API DTO
 */
public record HumanGateDto(
        String gateId,
        GateType gateType,
        String journeyId,
        String customerId,
        String operatingCaseId,
        HumanGateStatus status,
        String subject,
        Map<String, Object> proposal,
        List<String> evidenceRefs,
        GateDecision decision,
        Map<String, Object> modification,
        String decisionReason,
        String actorId,
        Instant createdAt,
        Instant decidedAt
) {
    public static HumanGateDto from(HumanGate gate) {
        return new HumanGateDto(
                gate.gateId(), gate.gateType(), gate.journeyId(), gate.customerId(),
                gate.operatingCaseId(), gate.status(), gate.subject(), gate.proposal(),
                gate.evidenceRefs(), gate.decision(), gate.modification(),
                gate.decisionReason(), gate.actorId(), gate.createdAt(), gate.decidedAt()
        );
    }
}
