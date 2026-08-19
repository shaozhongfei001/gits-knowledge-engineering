package com.gien.gits.adapter.filesystem;

import com.gien.gits.knowledge.AssetManifest;
import com.gien.gits.knowledge.port.AssetCatalogPort;
import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 内存资产清单读取（P22 E1）：从不可变快照读内存。
 * fail-closed：未命中返回 {@link Optional#empty()} / 空数组。
 */
public final class InMemoryAssetCatalogReader implements AssetCatalogPort {

    private final InMemoryKnowledgeStore store;

    public InMemoryAssetCatalogReader(InMemoryKnowledgeStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public Optional<AssetManifest> find(String assetId) {
        if (assetId == null || assetId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.assets().get(assetId));
    }

    @Override
    public List<AssetManifest> listByDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return List.of();
        }
        return store.assets().values().stream()
                .filter(asset -> domain.equals(asset.domain()))
                .toList();
    }

    @Override
    public List<AssetManifest> listAll() {
        return List.copyOf(store.assets().values());
    }
}
