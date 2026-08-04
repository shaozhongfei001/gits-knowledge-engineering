package com.gien.gits.worker.events;

/**
 * @deprecated Use {@link com.gien.gits.ontology.event.DomainEventType} instead.
 * The canonical constants have been relocated to the operational-ontology module.
 */
@Deprecated(forRemoval = true)
public final class DomainEventType {

    /** @deprecated Use {@link com.gien.gits.ontology.event.DomainEventType#CLAIM_CANDIDATE_RECORDED} */
    @Deprecated(forRemoval = true)
    public static final String CLAIM_CANDIDATE_RECORDED =
            com.gien.gits.ontology.event.DomainEventType.CLAIM_CANDIDATE_RECORDED;

    /** @deprecated Use {@link com.gien.gits.ontology.event.DomainEventType#CONTROLLED_ACTION_REQUESTED} */
    @Deprecated(forRemoval = true)
    public static final String CONTROLLED_ACTION_REQUESTED =
            com.gien.gits.ontology.event.DomainEventType.CONTROLLED_ACTION_REQUESTED;

    private DomainEventType() {}
}
