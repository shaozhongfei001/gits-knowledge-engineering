package com.gien.gits.worker;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;

import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.event.DomainEventType;
import com.gien.gits.worker.events.WorkerEventHandler;
import com.gien.gits.worker.handlers.ClaimCandidateRecordedHandler;
import com.gien.gits.worker.handlers.ControlledActionRequestedHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Integration test for the Worker application context.
 * Verifies that:
 * - Worker application context loads
 * - Event handlers are registered
 * - WorkerEventHandler stores events
 * - Custom handlers process events
 */
@SpringBootTest
class WorkerIntegrationTest {

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    WorkerEventHandler workerEventHandler;

    @Autowired
    ClaimCandidateRecordedHandler claimHandler;

    @Autowired
    ControlledActionRequestedHandler actionHandler;

    private CloudEvent buildEvent(String type, String id, String subject) {
        return CloudEvent.builder()
                .specversion("1.0")
                .id(id)
                .source("/gits/kno/worker/test")
                .type(type)
                .time(Instant.now().toString())
                .subject(subject)
                .datacontenttype("application/json")
                .data(Map.of("note", "integration test"))
                .build();
    }

    @Test
    void contextLoads() {
        // If this test passes, the Spring context loaded successfully
        assertThat(workerEventHandler).isNotNull();
        assertThat(claimHandler).isNotNull();
        assertThat(actionHandler).isNotNull();
    }

    @Test
    void workerEventHandlerStoresEvents() {
        workerEventHandler.clear();
        CloudEvent event = buildEvent(DomainEventType.CLAIM_CANDIDATE_RECORDED, "evt-int-001", "claim-int");

        publisher.publishEvent(event);

        assertThat(workerEventHandler.count()).isGreaterThanOrEqualTo(1);
        assertThat(workerEventHandler.find("evt-int-001")).isPresent();
    }

    @Test
    void bothHandlersProcessTheirEventTypes() {
        workerEventHandler.clear();
        CloudEvent claimEvent = buildEvent(DomainEventType.CLAIM_CANDIDATE_RECORDED, "evt-int-claim", "claim-int");
        CloudEvent actionEvent = buildEvent(DomainEventType.CONTROLLED_ACTION_REQUESTED, "evt-int-action", "action-int");

        publisher.publishEvent(claimEvent);
        publisher.publishEvent(actionEvent);

        // WorkerEventHandler should have received both
        assertThat(workerEventHandler.find("evt-int-claim")).isPresent();
        assertThat(workerEventHandler.find("evt-int-action")).isPresent();

        // Type-specific counters
        assertThat(workerEventHandler.getEventCount(DomainEventType.CLAIM_CANDIDATE_RECORDED)).isGreaterThanOrEqualTo(1);
        assertThat(workerEventHandler.getEventCount(DomainEventType.CONTROLLED_ACTION_REQUESTED)).isGreaterThanOrEqualTo(1);
    }
}
