package com.gien.gits.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeMapTest {

    @Test
    void recordNormalizesNullCollectionsToEmpty() {
        KnowledgeMap map = new KnowledgeMap(
                "1.0.0", "KM-GITS-ROOT", "根地图", "0.1.0", "VALIDATION", "ROOT",
                new KnowledgeMap.Entrypoints(List.of("AGENT"), List.of("PRE_VISIT_PREPARATION")),
                List.of(new KnowledgeMap.Domain("KD-CORP-RM", "对公", "p", "maps/x.md")),
                null, null, null, "RP-CORP-RM-001", "DENY", 1200);

        assertEquals(List.of(), map.assetRefs());
        assertEquals(List.of(), map.skillRefs());
        assertEquals(List.of(), map.activationContractRefs());
    }

    @Test
    void findDomainReturnsPresentForKnownDomain() {
        KnowledgeMap map = new KnowledgeMap(
                "1.0.0", "KM-GITS-ROOT", "根地图", "0.1.0", "VALIDATION", "ROOT",
                new KnowledgeMap.Entrypoints(List.of(), List.of()),
                List.of(new KnowledgeMap.Domain("KD-CORP-RM", "对公", "p", "maps/x.md")),
                List.of(), List.of(), List.of(), "RP-CORP-RM-001", "DENY", 1200);

        assertTrue(map.findDomain("KD-CORP-RM").isPresent());
        assertTrue(map.findDomain("KD-UNKNOWN").isEmpty());
    }
}
