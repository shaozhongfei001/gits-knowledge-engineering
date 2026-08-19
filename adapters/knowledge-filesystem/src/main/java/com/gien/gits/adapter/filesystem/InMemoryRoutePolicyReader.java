package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.RoutePolicy;
import com.gien.gits.knowledge.port.RoutePolicyPort;
import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import java.util.Objects;
import java.util.Optional;

/**
 * 内存路由策略读取（P22 E1）：从不可变快照读内存。fail-closed。
 */
public final class InMemoryRoutePolicyReader implements RoutePolicyPort {

    private final InMemoryKnowledgeStore store;

    public InMemoryRoutePolicyReader(InMemoryKnowledgeStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public Optional<RoutePolicy> find(String policyId) {
        if (policyId == null || policyId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.policies().get(policyId));
    }
}
