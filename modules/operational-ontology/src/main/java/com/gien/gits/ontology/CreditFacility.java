package com.gien.gits.ontology;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 授信额度 — 客户授信信息
 */
public record CreditFacility(
        String facilityId,
        String customerId,
        String borrowerEntity,
        LocalDate approvalDate,
        LocalDate maturityDate,
        long creditTotalCny,
        long usedCreditCny,
        long availableCreditCny,
        long currentLoanBalanceCny,
        long bankAcceptanceBillBalanceCny,
        long guaranteeBalanceCny,
        String collateral,
        List<String> purposeAllowed,
        List<String> purposeRestrictions,
        List<String> covenants,
        String reconciliationNote,
        String evidenceRef,
        Instant createdAt,
        Instant updatedAt) {

    public CreditFacility {
        if (facilityId == null || facilityId.isBlank()) {
            throw new IllegalArgumentException("facilityId is required");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        purposeAllowed = List.copyOf(purposeAllowed != null ? purposeAllowed : List.of());
        purposeRestrictions = List.copyOf(purposeRestrictions != null ? purposeRestrictions : List.of());
        covenants = List.copyOf(covenants != null ? covenants : List.of());
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    /** 兼容旧构造器（无审计字段） */
    public CreditFacility(String facilityId, String customerId, String borrowerEntity,
                          LocalDate approvalDate, LocalDate maturityDate, long creditTotalCny,
                          long usedCreditCny, long availableCreditCny, long currentLoanBalanceCny,
                          long bankAcceptanceBillBalanceCny, long guaranteeBalanceCny,
                          String collateral, List<String> purposeAllowed, List<String> purposeRestrictions,
                          List<String> covenants, String reconciliationNote, String evidenceRef) {
        this(facilityId, customerId, borrowerEntity, approvalDate, maturityDate, creditTotalCny,
             usedCreditCny, availableCreditCny, currentLoanBalanceCny, bankAcceptanceBillBalanceCny,
             guaranteeBalanceCny, collateral, purposeAllowed, purposeRestrictions, covenants,
             reconciliationNote, evidenceRef, Instant.now(), Instant.now());
    }
}
