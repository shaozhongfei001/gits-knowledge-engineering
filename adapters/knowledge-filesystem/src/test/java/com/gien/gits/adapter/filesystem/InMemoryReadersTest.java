package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.ActivationContract;
import com.gien.gits.knowledge.AssetManifest;
import com.gien.gits.knowledge.KnowledgeElement;
import com.gien.gits.knowledge.KnowledgeMap;
import com.gien.gits.knowledge.RoutePolicy;
import com.gien.gits.knowledge.repository.InMemoryKnowledgeStore;
import org.junit.jupiter.api.Test;

/**
 * 内存读取适配器单元测试：验证 5 类 Port 从不可变快照读内存的 fail-closed 行为。
 */
class InMemoryReadersTest {

    @Test
    void knowledgeMapReader() {
        InMemoryKnowledgeStore store = InMemoryKnowledgeStore.builder()
                .putMap(map("MAP-1"))
                .putMap(map("ROOT"))
                .build();
        InMemoryKnowledgeMapReader reader = new InMemoryKnowledgeMapReader(store);

        assertTrue(reader.loadRoot().isPresent());
        assertEquals("ROOT", reader.loadRoot().get().mapId());
        assertTrue(reader.load("MAP-1").isPresent());
        assertFalse(reader.load("NOPE").isPresent());
        assertFalse(reader.load(null).isPresent());
        assertFalse(reader.load("").isPresent());
    }

    @Test
    void assetCatalogReader() {
        InMemoryKnowledgeStore store = InMemoryKnowledgeStore.builder()
                .putAsset(asset("ASSET-A", "customer"))
                .putAsset(asset("ASSET-B", "product"))
                .putAsset(asset("ASSET-C", "customer"))
                .build();
        InMemoryAssetCatalogReader reader = new InMemoryAssetCatalogReader(store);

        assertTrue(reader.find("ASSET-A").isPresent());
        assertFalse(reader.find("ASSET-Z").isPresent());
        assertFalse(reader.find(null).isPresent());
        assertEquals(2, reader.listByDomain("customer").size());
        assertEquals(0, reader.listByDomain("nope").size());
        assertEquals(3, reader.listAll().size());
    }

    @Test
    void activationContractReader() {
        InMemoryKnowledgeStore store = InMemoryKnowledgeStore.builder()
                .putContract(contract("AC-1"))
                .build();
        InMemoryActivationContractReader reader = new InMemoryActivationContractReader(store);
        assertTrue(reader.find("AC-1").isPresent());
        assertFalse(reader.find("AC-Z").isPresent());
        assertFalse(reader.find(null).isPresent());
    }

    @Test
    void routePolicyReader() {
        InMemoryKnowledgeStore store = InMemoryKnowledgeStore.builder()
                .putPolicy(policy("RP-1"))
                .build();
        InMemoryRoutePolicyReader reader = new InMemoryRoutePolicyReader(store);
        assertTrue(reader.find("RP-1").isPresent());
        assertFalse(reader.find("RP-Z").isPresent());
    }

    @Test
    void knowledgeElementReader() {
        InMemoryKnowledgeStore store = InMemoryKnowledgeStore.builder()
                .putElement(element("KE-009-01", "KI-009"))
                .putElement(element("KE-009-02", "KI-009"))
                .putElement(element("KE-FRONT-001-01", "KI-FRONT-001"))
                .build();
        InMemoryKnowledgeElementReader reader = new InMemoryKnowledgeElementReader(store);

        assertTrue(reader.find("KE-009-01").isPresent());
        assertFalse(reader.find("KE-UNKNOWN").isPresent());
        assertEquals(2, reader.listByKnowledgeItem("KI-009").size());
        assertEquals(1, reader.listByKnowledgeItem("KI-FRONT-001").size());
        assertEquals(0, reader.listByKnowledgeItem("KI-NOPE").size());
    }

    @Test
    void emptyStoreBehavesFailClosed() {
        InMemoryKnowledgeStore store = InMemoryKnowledgeStore.builder().build();
        assertTrue(new InMemoryKnowledgeMapReader(store).loadRoot().isEmpty());
        assertTrue(new InMemoryAssetCatalogReader(store).find("A").isEmpty());
        assertTrue(new InMemoryActivationContractReader(store).find("AC").isEmpty());
        assertTrue(new InMemoryRoutePolicyReader(store).find("RP").isEmpty());
        assertTrue(new InMemoryKnowledgeElementReader(store).find("KE").isEmpty());
    }

    // ---- fixtures ----

    private static KnowledgeMap map(String id) {
        return new KnowledgeMap("1.0.0", id, id, "1", "active", "ROOT",
                new KnowledgeMap.Entrypoints(java.util.List.of(), java.util.List.of()),
                java.util.List.of(), java.util.List.of(), java.util.List.of(),
                java.util.List.of(), "RP-CORP-RM-001", "DENY", 0);
    }

    private static AssetManifest asset(String id, String domain) {
        return new AssetManifest("1.0.0", id, "DATA", id, domain, "1", "active",
                new AssetManifest.Source("type", "file://x", "AUTHORITATIVE", "INLINE_MD", "NONE"),
                new AssetManifest.Governance("owner", "INTERNAL", "CALLER", java.util.List.of()),
                java.util.List.of(),
                new AssetManifest.Activation("SHADOW", "adapter", java.util.List.of(), 100, "DENY"),
                new AssetManifest.Evidence(false, false, false),
                java.util.List.of(), java.util.List.of());
    }

    private static ActivationContract contract(String id) {
        return new ActivationContract("1.0.0", id, "1", "TASK", "SHADOW",
                new ActivationContract.Preconditions(java.util.List.of(), java.util.List.of(), false),
                java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of(),
                new ActivationContract.Context(100, java.util.List.of(), "TRIM"),
                java.util.List.of(), "DENY");
    }

    private static RoutePolicy policy(String id) {
        return new RoutePolicy("1.0.0", id, "1", "SHADOW", "ALLOW", java.util.List.of());
    }

    private static KnowledgeElement element(String id, String ki) {
        return new KnowledgeElement("1.0.0", id, id, "K-Type-F", ki, "content",
                new KnowledgeElement.Source("src", "AUTHORITATIVE"), java.util.List.of(), "ACTIVE");
    }
}
