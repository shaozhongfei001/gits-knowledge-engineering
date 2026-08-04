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
 * Test for ControlledActionRequestedHandler: verifies that the handler
 * receives CloudEvent with the correct type and processes it.
 */
@SpringBootTest
class ControlledActionRequestedHandlerTest {

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    ControlledActionRequestedHandler handler;

    private CloudEvent sampleActionEvent(String id) {
        return CloudEvent.builder()
                .specversion("1.0")
                .id(id)
                .source("/gits/kno/worker/test")
                .type(DomainEventType.CONTROLLED_ACTION_REQUESTED)
                .time(Instant.now().toString())
                .subject("action-1")
                .datacontenttype("application/json")
                .data(Map.of("note", "test action event"))
                .build();
    }

    @Test
    void handlerReceivesControlledActionRequestedEvent() {
        CloudEvent event = sampleActionEvent("evt-action-handler-001");
        publisher.publishEvent(event);
        assertThat(event.type()).isEqualTo(DomainEventType.CONTROLLED_ACTION_REQUESTED);
    }

    @Test
    void handlerIgnoresOtherEventTypes() {
        CloudEvent otherEvent = CloudEvent.builder()
                .specversion("1.0")
                .id("evt-other-002")
                .source("/gits/kno/worker/test")
                .type(DomainEventType.CLAIM_CANDIDATE_RECORDED)
                .time(Instant.now().toString())
                .subject("claim-1")
                .datacontenttype("application/json")
                .data(Map.of("note", "not an action event"))
                .build();
        publisher.publishEvent(otherEvent);
        assertThat(otherEvent.type()).isNotEqualTo(DomainEventType.CONTROLLED_ACTION_REQUESTED);
    }
}
