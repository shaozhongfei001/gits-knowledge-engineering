package com.gien.gits.ontology.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 商机 — 销售机会/交叉营销/追加销售的跟踪
 */
public record Opportunity(
    String opportunityId,
    String customerId,
    String interactionId,
    String operatingCaseId,
    String opportunityType,   // CROSS_SELL, UP_SELL, NEW_PRODUCT, RENEWAL, REFERRAL
    String productId,
    String productName,
    String description,
    String status,            // IDENTIFIED, QUALIFIED, PROPOSAL, NEGOTIATION, WON, LOST, STALE
    String estimatedAmount,
    String probability,       // HIGH, MEDIUM, LOW
    String assignedTo,
    String source,            // INTERACTION, EXTERNAL_EVENT, PROACTIVE, REFERRAL
    List<String> nextSteps,
    String expectedCloseDate,
    Instant createdAt,
    Instant updatedAt) {

    public Opportunity {
        Objects.requireNonNull(opportunityId, "opportunityId");
        Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(opportunityType, "opportunityType");
        Objects.requireNonNull(status, "status");
        nextSteps = List.copyOf(nextSteps != null ? nextSteps : List.of());
    }

    public Opportunity(String opportunityId, String customerId, String interactionId,
                       String operatingCaseId, String opportunityType, String productId,
                       String productName, String description, String status,
                       String estimatedAmount, String probability, String assignedTo,
                       String source, List<String> nextSteps, String expectedCloseDate) {
        this(opportunityId, customerId, interactionId, operatingCaseId, opportunityType,
             productId, productName, description, status, estimatedAmount, probability,
             assignedTo, source, nextSteps, expectedCloseDate, Instant.now(), Instant.now());
    }
}
