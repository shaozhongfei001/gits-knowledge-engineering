package com.gien.gits.worker.events;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * In-process mechanism handler for the two domain-event channels declared in
 * specs/events/domain-events.asyncapi.json:
 * <ul>
 *   <li>{@link DomainEventType#CLAIM_CANDIDATE_RECORDED}</li>
 *   <li>{@link DomainEventType#CONTROLLED_ACTION_REQUESTED}</li>
 * </ul>
 *
 * <p>This is engineering mechanism only — it does NOT perform real business
 * processing and does NOT talk to any external broker. It records each received
 * CloudEvent keyed by its {@code id} in an in-memory map so that callers and
 * tests can assert delivery. {@code gits.worker.enabled=false} (no external
 * integration) is preserved: this handler stays purely in-process.
 */
@Component
public class WorkerEventHandler {

    private static final Logger log = LoggerFactory.getLogger(WorkerEventHandler.class);

    private final Map<String, CloudEvent> received = new ConcurrentHashMap<>();

    @EventListener
    public void on(CloudEvent event) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(event.id(), "event.id");
        log.debug("Received domain event id={} type={} source={}", event.id(), event.type(), event.source());
        received.put(event.id(), event);
    }

    /** Find a received event by CloudEvent id. */
    public Optional<CloudEvent> find(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(received.get(id));
    }

    /** All received events, in insertion order of the underlying map. */
    public Collection<CloudEvent> all() {
        return received.values();
    }

    public int count() {
        return received.size();
    }

    public void clear() {
        received.clear();
    }
}
