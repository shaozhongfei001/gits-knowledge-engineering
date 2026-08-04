package com.gien.gits.worker.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.event.DomainEventType;

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
 *
 * <p>P11 G2 enhancement: per-type event counters and recent-events query.
 */
@Component
public class WorkerEventHandler {

    private static final Logger log = LoggerFactory.getLogger(WorkerEventHandler.class);

    private final Map<String, CloudEvent> received = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> typeCounters = new ConcurrentHashMap<>();

    @EventListener
    public void on(CloudEvent event) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(event.id(), "event.id");
        log.debug("Received domain event id={} type={} source={}", event.id(), event.type(), event.source());
        received.put(event.id(), event);
        typeCounters.computeIfAbsent(event.type(), k -> new AtomicLong(0)).incrementAndGet();
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
        typeCounters.clear();
    }

    /**
     * Get the count of received events for a specific event type.
     * @param type the CloudEvent type string (see {@link DomainEventType})
     * @return number of events received for that type, or 0 if none
     */
    public long getEventCount(String type) {
        AtomicLong counter = typeCounters.get(type);
        return counter != null ? counter.get() : 0L;
    }

    /**
     * Get the most recent events, ordered by insertion (latest first).
     * @param limit maximum number of events to return
     * @return list of recent CloudEvents, newest first
     */
    public List<CloudEvent> getRecentEvents(int limit) {
        List<CloudEvent> events = new ArrayList<>(received.values());
        int size = events.size();
        if (size <= limit) {
            return events;
        }
        return events.subList(size - limit, size);
    }
}
