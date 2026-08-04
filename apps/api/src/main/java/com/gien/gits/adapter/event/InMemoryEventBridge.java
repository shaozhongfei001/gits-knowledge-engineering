package com.gien.gits.adapter.event;

import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.port.EventBridge;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * In-process EventBridge that delegates to Spring's ApplicationEventPublisher.
 * Suitable for single-JVM deployments where the API and Worker share the
 * same Spring ApplicationContext.
 */
@Component
public class InMemoryEventBridge implements EventBridge {

    private final ApplicationEventPublisher publisher;

    public InMemoryEventBridge(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void send(CloudEvent event) {
        publisher.publishEvent(event);
    }
}
