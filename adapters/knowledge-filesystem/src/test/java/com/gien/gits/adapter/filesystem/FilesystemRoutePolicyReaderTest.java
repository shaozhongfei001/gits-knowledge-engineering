package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.RoutePolicy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemRoutePolicyReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void findParsesValidRoutePolicy() throws IOException {
        Path dir = Files.createDirectory(tempDir.resolve("routes"));
        Files.writeString(dir.resolve("RP-CORP-RM-001.json"), POLICY, StandardCharsets.UTF_8);

        FilesystemRoutePolicyReader reader = new FilesystemRoutePolicyReader(dir);
        RoutePolicy policy = reader.find("RP-CORP-RM-001").orElseThrow();

        assertEquals("RP-CORP-RM-001", policy.policyId());
        assertEquals("MAP_FIRST", policy.defaultMode());
        assertEquals("DENY_UNMAPPED_TASK", policy.defaultDecision());
        assertEquals(4, policy.rules().size());
        assertTrue(policy.findRule("FACT_RECONCILIATION_30M").isPresent());
    }

    @Test
    void findReturnsEmptyForMissingFile() {
        FilesystemRoutePolicyReader reader = new FilesystemRoutePolicyReader(tempDir.resolve("routes"));
        assertTrue(reader.find("RP-NOPE").isEmpty());
    }

    @Test
    void findFailsClosedOnMalformedJson() throws IOException {
        Path dir = Files.createDirectory(tempDir.resolve("routes2"));
        Files.writeString(dir.resolve("RP-BAD.json"), "{not-json}", StandardCharsets.UTF_8);

        FilesystemRoutePolicyReader reader = new FilesystemRoutePolicyReader(dir);
        assertTrue(reader.find("RP-BAD").isEmpty());
    }

    @Test
    void findFailsClosedWhenRequiredFieldMissing() throws IOException {
        Path dir = Files.createDirectory(tempDir.resolve("routes3"));
        Files.writeString(dir.resolve("RP-BAD.json"), "{\"policyId\":\"RP-BAD\"}", StandardCharsets.UTF_8);

        FilesystemRoutePolicyReader reader = new FilesystemRoutePolicyReader(dir);
        assertTrue(reader.find("RP-BAD").isEmpty());
    }

    @Test
    void findFailsClosedForBlank() {
        FilesystemRoutePolicyReader reader = new FilesystemRoutePolicyReader(tempDir.resolve("routes"));
        assertTrue(reader.find(null).isEmpty());
        assertTrue(reader.find("  ").isEmpty());
    }

    private static final String POLICY = """
            {"schemaVersion":"1.0.0","policyId":"RP-CORP-RM-001","version":"0.1.0","defaultMode":"MAP_FIRST","defaultDecision":"DENY_UNMAPPED_TASK","rules":[{"priority":10,"taskType":"FACT_RECONCILIATION_30M","mode":"ONTOLOGY_FIRST","activationContractRef":"AC-FACT-RECONCILIATION-001","reason":"r"},{"priority":20,"taskType":"PRE_VISIT_PREPARATION","mode":"ONTOLOGY_THEN_MAP","activationContractRef":"AC-PREVISIT-001","reason":"r"},{"priority":30,"taskType":"MARKET_SIGNAL_DISCOVERY","mode":"MAP_THEN_ONTOLOGY","activationContractRef":"AC-NOT-IN-P20","reason":"r"},{"priority":40,"taskType":"REPORT_GENERATION","mode":"MAP_FIRST","activationContractRef":"AC-NOT-IN-P20","reason":"r"}]}
            """;
}
