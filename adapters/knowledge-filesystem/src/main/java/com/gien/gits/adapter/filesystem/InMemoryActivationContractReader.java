package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.ActivationContract;
import com.gien.gits.knowledge.port.ActivationContractPort;
import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import java.util.Objects;
import java.util.Optional;

/**
 * 内存激活合同读取（P22 E1）：从不可变快照读内存。fail-closed。
 */
public final class InMemoryActivationContractReader implements ActivationContractPort {

    private final InMemoryKnowledgeStore store;

    public InMemoryActivationContractReader(InMemoryKnowledgeStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public Optional<ActivationContract> find(String contractId) {
        if (contractId == null || contractId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.contracts().get(contractId));
    }
}
