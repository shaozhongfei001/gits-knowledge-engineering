package com.gien.gits.adapter.persistence.foundation.ontology.dto;

import com.gien.gits.ontology.TransactionRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Flat DTO for MyBatis row mapping of transaction table.
 * Uses wrapper types (BigDecimal) to match DB DECIMAL column.
 */
public record TransactionRecordRow(
        UUID id,
        String customerId,
        LocalDate transactionDate,
        String transactionType,
        String counterparty,
        BigDecimal amount,
        String description,
        Instant createdAt) {

    public TransactionRecord toTransactionRecord() {
        return new TransactionRecord(
                id, customerId, transactionDate, transactionType, counterparty,
                amount != null ? amount.longValue() : 0L,
                description, null, createdAt);
    }
}
