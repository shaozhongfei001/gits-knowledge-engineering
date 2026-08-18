package com.gien.gits.knowledge;

import java.util.List;
import java.util.Optional;

/**
 * 知识地图（Knowledge Map）领域模型，对应合同 CTR-KMAP-001
 * (specs/knowledge-architecture/schemas/knowledge-map.schema.json)。
 *
 * <p>仅承载合同已定义的字段，不发明额外字段。</p>
 */
public record KnowledgeMap(
        String schemaVersion,
        String mapId,
        String name,
        String version,
        String status,
        String mapType,
        Entrypoints entrypoints,
        List<Domain> domains,
        List<String> assetRefs,
        List<String> skillRefs,
        List<String> activationContractRefs,
        String routePolicyRef,
        String defaultPolicy,
        Integer maxInitialTokens) {

    public KnowledgeMap {
        assetRefs = orEmpty(assetRefs);
        skillRefs = orEmpty(skillRefs);
        activationContractRefs = orEmpty(activationContractRefs);
    }

    private static List<String> orEmpty(List<String> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    public record Entrypoints(List<String> roles, List<String> tasks) {}

    public record Domain(String domainId, String name, String purpose, String mapRef) {}

    public Optional<Domain> findDomain(String domainId) {
        return domains.stream().filter(d -> d.domainId().equals(domainId)).findFirst();
    }
}
