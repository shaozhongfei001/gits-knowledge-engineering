package com.gien.gits.ontology.port;

import com.gien.gits.ontology.event.CloudEvent;

/**
 * Abstracts the event transport layer.
 * In-process implementation uses Spring ApplicationEventPublisher.
 * Future implementations can use Kafka, RabbitMQ, etc.
 */
public interface EventBridge {
    void send(CloudEvent event);
}
