package com.gien.gits.adapter.persistence.foundation.ontology.dto;

import com.gien.gits.ontology.BankRelationshipSnapshot;

import java.time.Instant;
import java.util.UUID;

/**
 * Flat DTO for MyBatis row mapping of bank_relationship_snapshot table.
 * Uses wrapper types (Long, Integer, Boolean) to match MyBatis constructor lookup.
 */
public record BankRelationshipSnapshotRow(
        UUID id,
        String customerId,
        String snapshotMonth,
        Long avgDailyDepositCny,
        Long monthlySettlementCny,
        Long loanBalanceCny,
        Long creditTotalCny,
        Long usedCreditCny,
        Long availableCreditCny,
        Long bankAcceptanceBillBalanceCny,
        Long guaranteeBalanceCny,
        Integer payrollEmployees,
        Boolean cashManagementOpened,
        Boolean supplyChainFinanceOpened,
        Long crossBorderSettlementCny,
        Integer productCount,
        String customerContributionLevel,
        String anomalyFlags,
        Instant createdAt) {

    public BankRelationshipSnapshot toBankRelationshipSnapshot() {
        return new BankRelationshipSnapshot(
                id, customerId, snapshotMonth,
                avgDailyDepositCny != null ? avgDailyDepositCny : 0L,
                monthlySettlementCny != null ? monthlySettlementCny : 0L,
                loanBalanceCny != null ? loanBalanceCny : 0L,
                creditTotalCny != null ? creditTotalCny : 0L,
                usedCreditCny != null ? usedCreditCny : 0L,
                availableCreditCny != null ? availableCreditCny : 0L,
                bankAcceptanceBillBalanceCny != null ? bankAcceptanceBillBalanceCny : 0L,
                guaranteeBalanceCny != null ? guaranteeBalanceCny : 0L,
                payrollEmployees != null ? payrollEmployees : 0,
                cashManagementOpened != null && cashManagementOpened,
                supplyChainFinanceOpened != null && supplyChainFinanceOpened,
                crossBorderSettlementCny != null ? crossBorderSettlementCny : 0L,
                productCount != null ? productCount : 0,
                customerContributionLevel,
                anomalyFlags,
                createdAt);
    }
}
