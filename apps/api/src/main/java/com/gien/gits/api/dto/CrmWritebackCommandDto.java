package com.gien.gits.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * CRM写回命令 API DTO
 */
public record CrmWritebackCommandDto(
        String commandId,
        String journeyId,
        String customerId,
        String operatingCaseId,
        String operation,
        String targetEntity,
        Map<String, Object> payload,
        String status,
        boolean humanConfirmationRequired,
        String decision,
        List<Map<String, Object>> modifications,
        String decisionReason,
        String actorId,
        Instant createdAt,
        Instant decidedAt,
        Instant sentAt
) {}
