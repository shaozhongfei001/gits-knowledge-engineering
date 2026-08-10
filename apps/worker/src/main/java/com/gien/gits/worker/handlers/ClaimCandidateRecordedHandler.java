package com.gien.gits.worker.handlers;

import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.ClaimType;
import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.event.DomainEventType;
import com.gien.gits.ontology.port.ClaimReconciliationPort;
import com.gien.gits.ontology.port.ClaimReconciliationPort.ReconciliationResult;
import com.gien.gits.ontology.port.ClaimReconciliationPort.ReconciliationStatus;
import com.gien.gits.ontology.port.DomainEventPublisher;
import com.gien.gits.ontology.port.WritableClaimRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles the {@link DomainEventType#CLAIM_CANDIDATE_RECORDED} domain event.
 *
 * <p>Business flow:
 * <ol>
 *   <li>Extract claim candidate data from event.data() — claimId, caseId, claimType, statement, etc.</li>
 *   <li>Persist the claim candidate via WritableClaimRepository</li>
 *   <li>Execute fact reconciliation via ClaimReconciliationPort (DMN decision engine)</li>
 *   <li>Update claim status based on reconciliation result:
 *       <ul>
 *         <li>VERIFIED_FACT → mark claim as VERIFIED_FACT</li>
 *         <li>CONFLICT_REQUIRES_HUMAN_REVIEW → mark claim as CONFLICT</li>
 *         <li>CANDIDATE_CLAIM → keep as CANDIDATE</li>
 *       </ul>
 *   </li>
 *   <li>Publish reconciliation-completed domain event</li>
 * </ol>
 *
 * <p>Rule #6: Claim≠Fact — a claim only becomes a fact after reconciliation confirms it.
 */
@Component
public class ClaimCandidateRecordedHandler {

    private static final Logger log = LoggerFactory.getLogger(ClaimCandidateRecordedHandler.class);

    private final WritableClaimRepository claimRepository;
    private final ClaimReconciliationPort claimReconciliationPort;
    private final DomainEventPublisher domainEventPublisher;

    public ClaimCandidateRecordedHandler(WritableClaimRepository claimRepository,
                                         ClaimReconciliationPort claimReconciliationPort,
                                         DomainEventPublisher domainEventPublisher) {
        this.claimRepository = Objects.requireNonNull(claimRepository, "claimRepository");
        this.claimReconciliationPort = Objects.requireNonNull(claimReconciliationPort, "claimReconciliationPort");
        this.domainEventPublisher = Objects.requireNonNull(domainEventPublisher, "domainEventPublisher");
    }

    @EventListener(condition = "#event.type() == T(com.gien.gits.ontology.event.DomainEventType).CLAIM_CANDIDATE_RECORDED")
    public void handle(CloudEvent event) {
        log.info("[CLAIM-CANDIDATE] Processing event: id={}, subject={}", event.id(), event.subject());

        Map<String, Object> data = event.data();
        if (data == null || data.isEmpty()) {
            log.warn("[CLAIM-CANDIDATE] Event data is empty, skipping: id={}", event.id());
            return;
        }

        // Step 1: Extract claim candidate data from event
        Claim claim = extractClaim(data);
        log.info("[CLAIM-CANDIDATE] Extracted claim: claimId={}, caseId={}, type={}, status={}",
                 claim.claimId(), claim.caseId(), claim.claimType(), claim.status());

        // Step 2: Persist the claim candidate
        claimRepository.save(claim);
        log.info("[CLAIM-CANDIDATE] Claim persisted: claimId={}", claim.claimId());

        // Step 3: Execute fact reconciliation via DMN decision engine
        boolean conflictDetected = parseBoolean(data, "conflictDetected", false);
        boolean authoritativeMatch = parseBoolean(data, "authoritativeMatch", false);
        boolean evidenceComplete = parseBoolean(data, "evidenceComplete", false);

        ReconciliationResult result = claimReconciliationPort.reconcile(
                conflictDetected, authoritativeMatch, evidenceComplete);
        log.info("[CLAIM-CANDIDATE] Reconciliation result: claimId={}, status={}, reasoning={}",
                 claim.claimId(), result.status(), result.reasoning());

        // Step 4: Update claim status based on reconciliation result
        ClaimStatus resolvedStatus = mapReconciliationToClaimStatus(result.status());
        if (resolvedStatus != claim.status()) {
            claimRepository.updateStatus(claim.claimId(), resolvedStatus);
            log.info("[CLAIM-CANDIDATE] Claim status updated: claimId={}, {} → {}",
                     claim.claimId(), claim.status(), resolvedStatus);
        }

        // Step 5: If conflict detected, publish CONTROLLED_ACTION_REQUESTED for human review
        if (result.status() == ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW) {
            CloudEvent actionRequestEvent = CloudEvent.builder()
                    .id("action-req-" + UUID.randomUUID().toString().substring(0, 8))
                    .source("gits-kno-worker")
                    .type(DomainEventType.CONTROLLED_ACTION_REQUESTED)
                    .time(Instant.now().toString())
                    .subject("claim:" + claim.claimId())
                    .data(Map.of(
                            "proposalId", claim.caseId().toString(),
                            "targetSystem", "GITS",
                            "targetObjectType", "CLAIM",
                            "targetObjectId", claim.claimId().toString(),
                            "operation", "UPDATE_WHITELISTED_FIELDS",
                            "conflictReasoning", result.reasoning()))
                    .build();
            domainEventPublisher.publish(actionRequestEvent);
            log.info("[CLAIM-CANDIDATE] Conflict detected → published CONTROLLED_ACTION_REQUESTED: claimId={}",
                     claim.claimId());
        }
    }

    /**
     * Extract a Claim from the event data map.
     *
     * <p>Expected data keys:
     * <ul>
     *   <li>claimId (String/UUID) — claim unique identifier</li>
     *   <li>caseId (String/UUID) — operating case identifier</li>
     *   <li>claimType (String) — CUSTOMER_JOURNEY, OPPORTUNITY, etc.</li>
     *   <li>statement (String) — the claim statement</li>
     *   <li>validFrom (String/Instant) — validity start</li>
     *   <li>validTo (String/Instant) — validity end</li>
     *   <li>supersedesClaimId (String/UUID) — previous claim superseded</li>
     * </ul>
     */
    private Claim extractClaim(Map<String, Object> data) {
        UUID claimId = parseUUID(data, "claimId");
        UUID caseId = parseUUID(data, "caseId");
        ClaimType claimType = parseClaimType(data);
        String statement = getString(data, "statement", "Unspecified claim");
        Instant validFrom = parseInstant(data, "validFrom");
        Instant validTo = parseInstant(data, "validTo");
        Instant recordedAt = Instant.now();
        UUID supersedesClaimId = data.containsKey("supersedesClaimId")
                ? parseUUID(data, "supersedesClaimId") : null;

        return new Claim(claimId, caseId, claimType, ClaimStatus.CANDIDATE,
                         statement, validFrom, validTo, recordedAt, supersedesClaimId);
    }

    /**
     * Map DMN reconciliation status to domain ClaimStatus.
     *
     * <p>Rule #6: Claim≠Fact — only VERIFIED_FACT from DMN promotes a claim to fact.
     */
    private ClaimStatus mapReconciliationToClaimStatus(ReconciliationStatus reconciliationStatus) {
        return switch (reconciliationStatus) {
            case VERIFIED_FACT -> ClaimStatus.VERIFIED_FACT;
            case CONFLICT_REQUIRES_HUMAN_REVIEW -> ClaimStatus.CONFLICT;
            case CANDIDATE_CLAIM -> ClaimStatus.CANDIDATE;
        };
    }

    private UUID parseUUID(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return UUID.randomUUID();
        }
        if (value instanceof UUID u) {
            return u;
        }
        try {
            return UUID.fromString(value.toString());
        } catch (IllegalArgumentException e) {
            log.warn("[CLAIM-CANDIDATE] Invalid UUID for key '{}': {}, generating random", key, value);
            return UUID.randomUUID();
        }
    }

    private ClaimType parseClaimType(Map<String, Object> data) {
        String typeStr = getString(data, "claimType", "CUSTOMER_STATEMENT");
        try {
            return ClaimType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            log.warn("[CLAIM-CANDIDATE] Unknown claimType '{}', defaulting to CUSTOMER_STATEMENT", typeStr);
            return ClaimType.CUSTOMER_STATEMENT;
        }
    }

    private Instant parseInstant(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Instant i) {
            return i;
        }
        try {
            return Instant.parse(value.toString());
        } catch (Exception e) {
            log.warn("[CLAIM-CANDIDATE] Invalid instant for key '{}': {}", key, value);
            return null;
        }
    }

    private boolean parseBoolean(Map<String, Object> data, String key, boolean defaultValue) {
        Object value = data.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private String getString(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
