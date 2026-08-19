package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.KnowledgeMap;
import com.gien.gits.knowledge.port.KnowledgeMapPort;
import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import java.util.Objects;
import java.util.Optional;

/**
 * 内存知识地图读取（P22 E1）：从不可变快照读内存，替代每请求扫盘（GAP-2）。
 * fail-closed：未命中返回 {@link Optional#empty()}。
 */
public final class InMemoryKnowledgeMapReader implements KnowledgeMapPort {

    private final InMemoryKnowledgeStore store;

    public InMemoryKnowledgeMapReader(InMemoryKnowledgeStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public Optional<KnowledgeMap> loadRoot() {
        return Optional.ofNullable(store.rootMap());
    }

    @Override
    public Optional<KnowledgeMap> load(String mapId) {
        if (mapId == null || mapId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.maps().get(mapId));
    }
}
