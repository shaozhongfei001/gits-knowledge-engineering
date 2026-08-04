package com.gien.gits.worker.handlers;

import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.event.DomainEventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Handles the {@link DomainEventType#CLAIM_CANDIDATE_RECORDED} domain event.
 *
 * <p>Business logic (future):
 * <ol>
 *   <li>Extract claim data from event.data()</li>
 *   <li>Validate and persist claim</li>
 *   <li>Trigger KYC insight update</li>
 * </ol>
 *
 * <p>For now: log and acknowledge.
 */
@Component
public class ClaimCandidateRecordedHandler {

    private static final Logger log = LoggerFactory.getLogger(ClaimCandidateRecordedHandler.class);

    @EventListener(condition = "#event.type() == T(com.gien.gits.ontology.event.DomainEventType).CLAIM_CANDIDATE_RECORDED")
    public void handle(CloudEvent event) {
        log.info("Processing claim-candidate-recorded event: id={}, subject={}", event.id(), event.subject());
        // Business logic placeholder:
        // 1. Extract claim data from event.data()
        // 2. Validate and persist claim
        // 3. Trigger KYC insight update
        log.info("Claim candidate recorded processed successfully: subject={}", event.subject());
    }
}
