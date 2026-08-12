package com.gien.gits.api.dto;

import com.gien.gits.ontology.GateDecision;

import java.util.Map;

/**
 * HumanGate 决策请求 DTO
 */
public record HumanGateDecisionRequest(
        GateDecision decision,
        Map<String, Object> modification,
        String reason,
        String actorId
) {}
