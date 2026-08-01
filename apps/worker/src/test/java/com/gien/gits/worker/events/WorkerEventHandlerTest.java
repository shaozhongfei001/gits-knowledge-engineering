package com.gien.gits.worker.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Unit test for CTR-EVENT-001: publishes each of the two domain-event types
 * from specs/events/domain-events.asyncapi.json through Spring's in-process
 * event bus and asserts the handler received them with the correct type, id,
 * and source. No real broker is used.
 */
@SpringBootTest
class WorkerEventHandlerTest {

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    WorkerEventHandler handler;

    private CloudEvent sample(String type, String id) {
        return CloudEvent.builder()
                .specversion("1.0")
                .id(id)
                .source("/hzb/kno/worker/test")
                .type(type)
                .time(Instant.now().toString())
                .subject("claim-1")
                .datacontenttype("application/json")
                .data(Map.of("note", "in-process mechanism"))
                .build();
    }

    @Test
    void claimCandidateRecordedIsReceived() {
        handler.clear();
        CloudEvent event = sample(DomainEventType.CLAIM_CANDIDATE_RECORDED, "evt-claim-001");

        publisher.publishEvent(event);

        assertThat(handler.all()).hasSize(1);
        CloudEvent received = handler.find("evt-claim-001").orElseThrow();
        assertThat(received.type()).isEqualTo(DomainEventType.CLAIM_CANDIDATE_RECORDED);
        assertThat(received.id()).isEqualTo("evt-claim-001");
        assertThat(received.source()).isEqualTo("/hzb/kno/worker/test");
        assertThat(received.specversion()).isEqualTo("1.0");
        assertThat(received.datacontenttype()).isEqualTo("application/json");
        assertThat(received.data()).containsEntry("note", "in-process mechanism");
    }

    @Test
    void controlledActionRequestedIsReceived() {
        handler.clear();
        CloudEvent event = sample(DomainEventType.CONTROLLED_ACTION_REQUESTED, "evt-action-001");

        publisher.publishEvent(event);

        assertThat(handler.all()).hasSize(1);
        CloudEvent received = handler.find("evt-action-001").orElseThrow();
        assertThat(received.type()).isEqualTo(DomainEventType.CONTROLLED_ACTION_REQUESTED);
        assertThat(received.id()).isEqualTo("evt-action-001");
        assertThat(received.source()).isEqualTo("/hzb/kno/worker/test");
    }

    @Test
    void bothEventTypesAreReceivedIndependently() {
        handler.clear();
        publisher.publishEvent(sample(DomainEventType.CLAIM_CANDIDATE_RECORDED, "evt-both-1"));
        publisher.publishEvent(sample(DomainEventType.CONTROLLED_ACTION_REQUESTED, "evt-both-2"));

        assertThat(handler.count()).isEqualTo(2);
        assertThat(handler.find("evt-both-1")).isPresent()
                .get().extracting(CloudEvent::type)
                .isEqualTo(DomainEventType.CLAIM_CANDIDATE_RECORDED);
        assertThat(handler.find("evt-both-2")).isPresent()
                .get().extracting(CloudEvent::type)
                .isEqualTo(DomainEventType.CONTROLLED_ACTION_REQUESTED);
    }
}
