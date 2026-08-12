package com.gien.gits.adapter.persistence.foundation.ontology.dto;

import com.gien.gits.ontology.CreditFacility;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Flat DTO for MyBatis row mapping of credit_facility table.
 * Uses wrapper types (Long) to match MyBatis constructor lookup.
 */
public record CreditFacilityRow(
        String facilityId,
        String customerId,
        String borrowerEntity,
        LocalDate approvalDate,
        LocalDate maturityDate,
        Long creditTotalCny,
        Long usedCreditCny,
        Long availableCreditCny,
        Long currentLoanBalanceCny,
        Long bankAcceptanceBillBalanceCny,
        Long guaranteeBalanceCny,
        String collateral,
        List<String> purposeAllowed,
        List<String> purposeRestrictions,
        List<String> covenants,
        String reconciliationNote,
        String evidenceRef,
        Instant createdAt,
        Instant updatedAt) {

    public CreditFacility toCreditFacility() {
        return new CreditFacility(
                facilityId, customerId, borrowerEntity, approvalDate, maturityDate,
                creditTotalCny != null ? creditTotalCny : 0L,
                usedCreditCny != null ? usedCreditCny : 0L,
                availableCreditCny != null ? availableCreditCny : 0L,
                currentLoanBalanceCny != null ? currentLoanBalanceCny : 0L,
                bankAcceptanceBillBalanceCny != null ? bankAcceptanceBillBalanceCny : 0L,
                guaranteeBalanceCny != null ? guaranteeBalanceCny : 0L,
                collateral, purposeAllowed, purposeRestrictions, covenants,
                reconciliationNote, evidenceRef, createdAt, updatedAt);
    }
}
