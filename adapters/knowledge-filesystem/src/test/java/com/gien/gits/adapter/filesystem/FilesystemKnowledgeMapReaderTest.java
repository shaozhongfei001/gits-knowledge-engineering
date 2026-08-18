package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.KnowledgeMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemKnowledgeMapReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadRootParsesInlineFrontmatterKnowledgeMap() throws IOException {
        Path mapsDir = Files.createDirectory(tempDir.resolve("maps"));
        Files.writeString(mapsDir.resolve("ROOT_KNOWLEDGE_MAP.md"), ROOT_MAP, StandardCharsets.UTF_8);

        FilesystemKnowledgeMapReader reader = new FilesystemKnowledgeMapReader(mapsDir);
        KnowledgeMap map = reader.loadRoot().orElseThrow();

        assertEquals("KM-GITS-ROOT", map.mapId());
        assertEquals("ROOT", map.mapType());
        assertEquals("RP-CORP-RM-001", map.routePolicyRef());
        assertEquals("DENY", map.defaultPolicy());
        assertEquals(1, map.domains().size());
        assertEquals("KD-CORP-RM", map.domains().get(0).domainId());
        assertTrue(map.entrypoints().roles().contains("AGENT"));
    }

    @Test
    void loadRootReturnsEmptyWhenFileMissing() {
        FilesystemKnowledgeMapReader reader = new FilesystemKnowledgeMapReader(tempDir.resolve("nope"));
        assertTrue(reader.loadRoot().isEmpty());
    }

    @Test
    void loadRootFailsClosedOnMalformedJson() throws IOException {
        Path mapsDir = Files.createDirectory(tempDir.resolve("maps2"));
        Files.writeString(mapsDir.resolve("ROOT_KNOWLEDGE_MAP.md"), "---\n{not-valid-json}\n---", StandardCharsets.UTF_8);

        FilesystemKnowledgeMapReader reader = new FilesystemKnowledgeMapReader(mapsDir);
        assertTrue(reader.loadRoot().isEmpty());
    }

    @Test
    void loadRootFailsClosedWhenRequiredFieldMissing() throws IOException {
        Path mapsDir = Files.createDirectory(tempDir.resolve("maps3"));
        String invalid = "---\n{\"schemaVersion\":\"1.0.0\",\"mapId\":\"KM-X\"}\n---";
        Files.writeString(mapsDir.resolve("ROOT_KNOWLEDGE_MAP.md"), invalid, StandardCharsets.UTF_8);

        FilesystemKnowledgeMapReader reader = new FilesystemKnowledgeMapReader(mapsDir);
        assertTrue(reader.loadRoot().isEmpty());
    }

    @Test
    void loadByMapIdFailsClosedForBlank() {
        FilesystemKnowledgeMapReader reader = new FilesystemKnowledgeMapReader(tempDir);
        assertTrue(reader.load("  ").isEmpty());
        assertTrue(reader.load(null).isEmpty());
    }

    private static final String ROOT_MAP = """
            ---{"schemaVersion":"1.0.0","mapId":"KM-GITS-ROOT","name":"根地图","version":"0.1.0","status":"VALIDATION","mapType":"ROOT","entrypoints":{"roles":["RELATIONSHIP_MANAGER","AGENT"],"tasks":["PRE_VISIT_PREPARATION"]},"domains":[{"domainId":"KD-CORP-RM","name":"对公","purpose":"p","mapRef":"maps/x.md"}],"assetRefs":[],"skillRefs":[],"activationContractRefs":[],"routePolicyRef":"RP-CORP-RM-001","defaultPolicy":"DENY","maxInitialTokens":1200}
            ---
            """;
}
