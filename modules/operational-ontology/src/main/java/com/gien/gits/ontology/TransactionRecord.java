package com.gien.gits.ontology;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * 交易记录 — 客户交易流水
 */
public record TransactionRecord(
        UUID id,
        String customerId,
        LocalDate transactionDate,
        String transactionType,
        String counterparty,
        long amountCny,
        String description,
        String evidenceRef,
        Instant createdAt) {

    public TransactionRecord {
        Objects.requireNonNull(id, "id");
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        Objects.requireNonNull(transactionDate, "transactionDate");
        if (createdAt == null) createdAt = Instant.now();
    }

    /** 兼容旧构造器（无审计字段） */
    public TransactionRecord(UUID id, String customerId, LocalDate transactionDate,
                             String transactionType, String counterparty, long amountCny,
                             String description, String evidenceRef) {
        this(id, customerId, transactionDate, transactionType, counterparty, amountCny,
             description, evidenceRef, Instant.now());
    }
}
