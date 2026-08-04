package com.gien.gits.ontology;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 事实对账案例 — 交互声明与外部事实的交叉验证
 */
public record FactReconciliationCase(
        String reconciliationId,
        String caseId,
        String topic,
        String structuredFact,
        String interactionClaim,
        String externalFact,
        List<String> ontologyDistinction,
        String correctJudgment,
        List<String> wrongOutputExamples,
        String nextAction,
        ReconciliationStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public FactReconciliationCase {
        if (reconciliationId == null || reconciliationId.isBlank()) {
            throw new IllegalArgumentException("reconciliationId is required");
        }
        if (caseId == null || caseId.isBlank()) {
            throw new IllegalArgumentException("caseId is required");
        }
        ontologyDistinction = List.copyOf(ontologyDistinction != null ? ontologyDistinction : List.of());
        wrongOutputExamples = List.copyOf(wrongOutputExamples != null ? wrongOutputExamples : List.of());
        Objects.requireNonNull(status, "status");
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    /** 兼容旧构造器（无审计字段） */
    public FactReconciliationCase(String reconciliationId, String caseId, String topic,
                                  String structuredFact, String interactionClaim, String externalFact,
                                  List<String> ontologyDistinction, String correctJudgment,
                                  List<String> wrongOutputExamples, String nextAction,
                                  ReconciliationStatus status) {
        this(reconciliationId, caseId, topic, structuredFact, interactionClaim, externalFact,
             ontologyDistinction, correctJudgment, wrongOutputExamples, nextAction, status,
             Instant.now(), Instant.now());
    }
}
