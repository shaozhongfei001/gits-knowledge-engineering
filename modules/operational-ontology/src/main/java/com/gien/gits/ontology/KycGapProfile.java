package com.gien.gits.ontology;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * KYC缺口画像 — 客户认知完整度评估
 */
public record KycGapProfile(
        String profileId,
        String customerId,
        LocalDate asOf,
        List<String> knownItems,
        List<String> partialKnownItems,
        List<String> staleItems,
        List<String> conflictingOrAmbiguousItems,
        List<String> unknownItems,
        List<String> priorityQuestions,
        Instant createdAt,
        Instant updatedAt) {

    public KycGapProfile {
        if (profileId == null || profileId.isBlank()) {
            throw new IllegalArgumentException("profileId is required");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        Objects.requireNonNull(asOf, "asOf");
        knownItems = List.copyOf(knownItems != null ? knownItems : List.of());
        partialKnownItems = List.copyOf(partialKnownItems != null ? partialKnownItems : List.of());
        staleItems = List.copyOf(staleItems != null ? staleItems : List.of());
        conflictingOrAmbiguousItems = List.copyOf(conflictingOrAmbiguousItems != null ? conflictingOrAmbiguousItems : List.of());
        unknownItems = List.copyOf(unknownItems != null ? unknownItems : List.of());
        priorityQuestions = List.copyOf(priorityQuestions != null ? priorityQuestions : List.of());
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    /** 兼容旧构造器（无审计字段） */
    public KycGapProfile(String profileId, String customerId, LocalDate asOf,
                         List<String> knownItems, List<String> partialKnownItems,
                         List<String> staleItems, List<String> conflictingOrAmbiguousItems,
                         List<String> unknownItems, List<String> priorityQuestions) {
        this(profileId, customerId, asOf, knownItems, partialKnownItems, staleItems,
             conflictingOrAmbiguousItems, unknownItems, priorityQuestions, Instant.now(), Instant.now());
    }
}
