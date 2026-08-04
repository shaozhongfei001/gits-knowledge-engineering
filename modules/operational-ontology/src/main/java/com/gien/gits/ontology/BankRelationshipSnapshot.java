package com.gien.gits.ontology;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 银行关系快照 — 月度客户关系指标汇总
 */
public record BankRelationshipSnapshot(
        UUID id,
        String customerId,
        String snapshotMonth,
        long avgDailyDepositCny,
        long monthlySettlementCny,
        long loanBalanceCny,
        long creditTotalCny,
        long usedCreditCny,
        long availableCreditCny,
        long bankAcceptanceBillBalanceCny,
        long guaranteeBalanceCny,
        int payrollEmployees,
        boolean cashManagementOpened,
        boolean supplyChainFinanceOpened,
        long crossBorderSettlementCny,
        int productCount,
        String customerContributionLevel,
        String anomalyFlags,
        Instant createdAt) {

    public BankRelationshipSnapshot {
        Objects.requireNonNull(id, "id");
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (snapshotMonth == null || snapshotMonth.isBlank()) {
            throw new IllegalArgumentException("snapshotMonth is required");
        }
        if (createdAt == null) createdAt = Instant.now();
    }

    /** 兼容旧构造器（无审计字段） */
    public BankRelationshipSnapshot(UUID id, String customerId, String snapshotMonth,
                                    long avgDailyDepositCny, long monthlySettlementCny,
                                    long loanBalanceCny, long creditTotalCny, long usedCreditCny,
                                    long availableCreditCny, long bankAcceptanceBillBalanceCny,
                                    long guaranteeBalanceCny, int payrollEmployees,
                                    boolean cashManagementOpened, boolean supplyChainFinanceOpened,
                                    long crossBorderSettlementCny, int productCount,
                                    String customerContributionLevel, String anomalyFlags) {
        this(id, customerId, snapshotMonth, avgDailyDepositCny, monthlySettlementCny,
             loanBalanceCny, creditTotalCny, usedCreditCny, availableCreditCny,
             bankAcceptanceBillBalanceCny, guaranteeBalanceCny, payrollEmployees,
             cashManagementOpened, supplyChainFinanceOpened, crossBorderSettlementCny,
             productCount, customerContributionLevel, anomalyFlags, Instant.now());
    }
}
