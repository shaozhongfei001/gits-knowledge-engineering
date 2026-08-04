package com.gien.gits.ontology.event;

/**
 * Well-known domain-event type constants used across the GITS platform.
 * Each constant maps to a CloudEvent {@code type} value defined in
 * {@code specs/events/domain-events.asyncapi.json}.
 */
public final class DomainEventType {

    private DomainEventType() {}

    /** Address of the claimCandidateRecorded channel. */
    public static final String CLAIM_CANDIDATE_RECORDED =
            "gits.kno.claim-candidate-recorded.v1";

    /** Address of the controlledActionRequested channel. */
    public static final String CONTROLLED_ACTION_REQUESTED =
            "gits.kno.controlled-action-requested.v1";
}
