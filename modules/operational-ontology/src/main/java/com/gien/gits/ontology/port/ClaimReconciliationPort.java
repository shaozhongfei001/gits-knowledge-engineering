package com.gien.gits.ontology.port;

/**
 * 事实对账决策端口 — DMN决策表的抽象
 * 实现可基于KIE DMN运行时或手写决策表逻辑
 */
public interface ClaimReconciliationPort {

    ReconciliationResult reconcile(boolean conflictDetected, boolean authoritativeMatch, boolean evidenceComplete);

    enum ReconciliationStatus {
        CONFLICT_REQUIRES_HUMAN_REVIEW,
        VERIFIED_FACT,
        CANDIDATE_CLAIM
    }

    record ReconciliationResult(ReconciliationStatus status, String reasoning) {}
}
