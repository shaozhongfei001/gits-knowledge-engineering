package com.gien.gits.adapter.persistence.entity;

import com.gien.gits.ontology.GateDecision;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * CRM写回命令数据库实体 — 对应 crm_writeback_command 表
 */
public record CrmWritebackCommandEntity(
        String commandId,
        String journeyId,
        String customerId,
        String operatingCaseId,
        String operation,
        String targetEntity,
        Map<String, Object> payload,
        CrmWritebackStatus status,
        boolean humanConfirmationRequired,
        GateDecision decision,
        List<Map<String, Object>> modifications,
        String decisionReason,
        String actorId,
        Instant createdAt,
        Instant decidedAt,
        Instant sentAt,
        String errorMessage
) {
    public enum CrmWritebackStatus {
        PENDING, APPROVED, REJECTED, SENT, FAILED
    }

    public CrmWritebackCommandEntity withDecision(GateDecision decision,
                                                    List<Map<String, Object>> modifications,
                                                    String reason, String actorId) {
        var newStatus = mapDecisionToStatus(decision);
        return new CrmWritebackCommandEntity(
                commandId, journeyId, customerId, operatingCaseId,
                operation, targetEntity, payload, newStatus,
                humanConfirmationRequired, decision, modifications,
                reason, actorId, createdAt, Instant.now(), sentAt, errorMessage
        );
    }

    private static CrmWritebackStatus mapDecisionToStatus(GateDecision decision) {
        return switch (decision) {
            case APPROVE -> CrmWritebackStatus.APPROVED;
            case REJECT, DECLINE -> CrmWritebackStatus.REJECTED;
            case MODIFY -> CrmWritebackStatus.APPROVED;
            case HOLD -> CrmWritebackStatus.PENDING;
        };
    }
}
