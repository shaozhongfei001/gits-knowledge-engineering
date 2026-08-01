package com.gien.gits.worker.events;

/**
 * Channel addresses from specs/events/domain-events.asyncapi.json, used as the
 * CloudEvent {@code type} value for each in-process domain event.
 */
public final class DomainEventType {

    /** Address of the claimCandidateRecorded channel. */
    public static final String CLAIM_CANDIDATE_RECORDED = "gits.kno.claim-candidate-recorded.v1";

    /** Address of the controlledActionRequested channel. */
    public static final String CONTROLLED_ACTION_REQUESTED = "gits.kno.controlled-action-requested.v1";

    private DomainEventType() {
    }
}
