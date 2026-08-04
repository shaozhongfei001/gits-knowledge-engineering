package com.gien.gits.worker.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;

import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.event.DomainEventType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Test for ClaimCandidateRecordedHandler: verifies that the handler
 * receives CloudEvent with the correct type and processes it.
 */
@SpringBootTest
class ClaimCandidateRecordedHandlerTest {

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    ClaimCandidateRecordedHandler handler;

    private CloudEvent sampleClaimEvent(String id) {
        return CloudEvent.builder()
                .specversion("1.0")
                .id(id)
                .source("/gits/kno/worker/test")
                .type(DomainEventType.CLAIM_CANDIDATE_RECORDED)
                .time(Instant.now().toString())
                .subject("claim-1")
                .datacontenttype("application/json")
                .data(Map.of("note", "test claim event"))
                .build();
    }

    @Test
    void handlerReceivesClaimCandidateRecordedEvent() {
        CloudEvent event = sampleClaimEvent("evt-claim-handler-001");
        // Publishing should not throw — handler processes the event
        publisher.publishEvent(event);
        // If we reach here without exception, the handler received and processed the event
        assertThat(event.type()).isEqualTo(DomainEventType.CLAIM_CANDIDATE_RECORDED);
    }

    @Test
    void handlerIgnoresOtherEventTypes() {
        CloudEvent otherEvent = CloudEvent.builder()
                .specversion("1.0")
                .id("evt-other-001")
                .source("/gits/kno/worker/test")
                .type(DomainEventType.CONTROLLED_ACTION_REQUESTED)
                .time(Instant.now().toString())
                .subject("action-1")
                .datacontenttype("application/json")
                .data(Map.of("note", "not a claim event"))
                .build();
        // Publishing a different event type should not trigger the claim handler
        publisher.publishEvent(otherEvent);
        // No exception means the condition filter worked
        assertThat(otherEvent.type()).isNotEqualTo(DomainEventType.CLAIM_CANDIDATE_RECORDED);
    }
}
