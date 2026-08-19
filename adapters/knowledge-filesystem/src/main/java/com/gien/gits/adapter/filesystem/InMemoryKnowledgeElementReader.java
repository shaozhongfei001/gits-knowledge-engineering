package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.KnowledgeElement;
import com.gien.gits.knowledge.port.KnowledgeElementPort;
import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 内存知识要素读取（P22 E1）：从不可变快照读内存。fail-closed。
 */
public final class InMemoryKnowledgeElementReader implements KnowledgeElementPort {

    private final InMemoryKnowledgeStore store;

    public InMemoryKnowledgeElementReader(InMemoryKnowledgeStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public Optional<KnowledgeElement> find(String elementId) {
        if (elementId == null || elementId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.elements().get(elementId));
    }

    @Override
    public List<KnowledgeElement> listByKnowledgeItem(String knowledgeItemId) {
        if (knowledgeItemId == null || knowledgeItemId.isBlank()) {
            return List.of();
        }
        return store.elements().values().stream()
                .filter(element -> knowledgeItemId.equals(element.knowledgeItemId()))
                .toList();
    }
}
