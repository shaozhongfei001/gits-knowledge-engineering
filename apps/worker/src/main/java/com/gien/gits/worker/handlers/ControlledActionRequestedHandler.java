package com.gien.gits.worker.handlers;

import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.event.DomainEventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Handles the {@link DomainEventType#CONTROLLED_ACTION_REQUESTED} domain event.
 *
 * <p>Business logic (future):
 * <ol>
 *   <li>Extract action data from event.data()</li>
 *   <li>Create ControlledAction record</li>
 *   <li>Trigger CRM writeback</li>
 * </ol>
 *
 * <p>For now: log and acknowledge.
 */
@Component
public class ControlledActionRequestedHandler {

    private static final Logger log = LoggerFactory.getLogger(ControlledActionRequestedHandler.class);

    @EventListener(condition = "#event.type() == T(com.gien.gits.ontology.event.DomainEventType).CONTROLLED_ACTION_REQUESTED")
    public void handle(CloudEvent event) {
        log.info("Processing controlled-action-requested event: id={}, subject={}", event.id(), event.subject());
        // Business logic placeholder:
        // 1. Extract action data from event.data()
        // 2. Create ControlledAction record
        // 3. Trigger CRM writeback
        log.info("Controlled action requested processed successfully: subject={}", event.subject());
    }
}
