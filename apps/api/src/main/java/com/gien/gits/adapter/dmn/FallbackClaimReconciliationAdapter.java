package com.gien.gits.adapter.dmn;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.ontology.port.ClaimReconciliationPort;

/**
 * 手写实现DMN决策表逻辑 — 与claim-reconciliation.dmn完全一致
 * 3条规则，FIRST命中策略:
 *   Rule 1: conflictDetected=true  → CONFLICT_REQUIRES_HUMAN_REVIEW
 *   Rule 2: conflictDetected=false AND authoritativeMatch=true AND evidenceComplete=true → VERIFIED_FACT
 *   Rule 3: conflictDetected=false AND (其余情况) → CANDIDATE_CLAIM
 */
public class FallbackClaimReconciliationAdapter implements ClaimReconciliationPort {

    private final BusinessMetrics businessMetrics;

    public FallbackClaimReconciliationAdapter(BusinessMetrics businessMetrics) {
        this.businessMetrics = businessMetrics;
    }

    @Override
    public ReconciliationResult reconcile(boolean conflictDetected, boolean authoritativeMatch, boolean evidenceComplete) {
        // Rule 1: conflictDetected=true → CONFLICT_REQUIRES_HUMAN_REVIEW (其他输入无关)
        if (conflictDetected) {
            ReconciliationResult result = new ReconciliationResult(
                ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW,
                "Rule-1: conflictDetected=true → CONFLICT_REQUIRES_HUMAN_REVIEW");
            businessMetrics.recordDmnDecision(result.status().toString());
            return result;
        }

        // Rule 2: conflictDetected=false AND authoritativeMatch=true AND evidenceComplete=true → VERIFIED_FACT
        if (authoritativeMatch && evidenceComplete) {
            ReconciliationResult result = new ReconciliationResult(
                ReconciliationStatus.VERIFIED_FACT,
                "Rule-2: conflictDetected=false, authoritativeMatch=true, evidenceComplete=true → VERIFIED_FACT");
            businessMetrics.recordDmnDecision(result.status().toString());
            return result;
        }

        // Rule 3: conflictDetected=false AND (其余情况) → CANDIDATE_CLAIM
        ReconciliationResult result = new ReconciliationResult(
            ReconciliationStatus.CANDIDATE_CLAIM,
            "Rule-3: conflictDetected=false, fallback → CANDIDATE_CLAIM");
        businessMetrics.recordDmnDecision(result.status().toString());
        return result;
    }
}
