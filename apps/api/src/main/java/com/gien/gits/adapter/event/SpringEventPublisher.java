package com.gien.gits.adapter.event;

import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.port.DomainEventPublisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adapter that bridges DomainEventPublisher to Spring's ApplicationEventPublisher.
 */
@Component
public class SpringEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(CloudEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
