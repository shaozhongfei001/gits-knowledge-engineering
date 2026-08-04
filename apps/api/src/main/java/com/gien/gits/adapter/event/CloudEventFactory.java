package com.gien.gits.adapter.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.event.DomainEventType;

/**
 * Factory for creating standard CloudEvent instances with auto-filled
 * specversion, source, time, and datacontenttype.
 */
public final class CloudEventFactory {

    private static final String SOURCE = "/gits/kno/api";

    private CloudEventFactory() {}

    /**
     * Create a CloudEvent with all standard fields pre-populated.
     */
    public static CloudEvent create(String type, String subject, Map<String, Object> data) {
        return CloudEvent.builder()
                .specversion("1.0")
                .id(UUID.randomUUID().toString())
                .source(SOURCE)
                .type(type)
                .time(Instant.now().toString())
                .subject(subject)
                .datacontenttype("application/json")
                .data(data)
                .build();
    }

    /**
     * Convenience: create a claimCandidateRecorded event.
     */
    public static CloudEvent claimCandidateRecorded(String subject, Map<String, Object> data) {
        return create(DomainEventType.CLAIM_CANDIDATE_RECORDED, subject, data);
    }

    /**
     * Convenience: create a controlledActionRequested event.
     */
    public static CloudEvent controlledActionRequested(String subject, Map<String, Object> data) {
        return create(DomainEventType.CONTROLLED_ACTION_REQUESTED, subject, data);
    }
}
