package com.gien.gits.api.dto;

import com.gien.gits.ontology.GateDecision;

import java.util.List;
import java.util.Map;

/**
 * CRM写回决策请求 DTO
 */
public record CrmWritebackDecisionRequest(
        GateDecision decision,
        List<Map<String, Object>> modifications,
        String reason,
        String actorId
) {}
