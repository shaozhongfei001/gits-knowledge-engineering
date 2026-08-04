package com.gien.gits.ontology.port;

import com.gien.gits.ontology.event.CloudEvent;

/**
 * Outbound port for publishing domain events.
 * Implementations wrap an underlying event bus (e.g. Spring ApplicationEventPublisher).
 */
public interface DomainEventPublisher {
    void publish(CloudEvent event);
}
