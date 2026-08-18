package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.ActivationContract;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilesystemActivationContractReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void findParsesValidActivationContract() throws IOException {
        Path dir = Files.createDirectory(tempDir.resolve("activations"));
        Files.writeString(dir.resolve("AC-PREVISIT-001.json"), CONTRACT, StandardCharsets.UTF_8);

        FilesystemActivationContractReader reader = new FilesystemActivationContractReader(dir);
        ActivationContract contract = reader.find("AC-PREVISIT-001").orElseThrow();

        assertEquals("AC-PREVISIT-001", contract.contractId());
        assertEquals("PRE_VISIT_PREPARATION", contract.taskType());
        assertEquals("ONTOLOGY_THEN_MAP", contract.routeMode());
        assertEquals(1, contract.activations().size());
        assertEquals("ASSET-DATA-CUSTOMER-PROFILE", contract.activations().get(0).assetId());
        assertTrue(contract.preconditions().permissionDecisionRequired());
        assertEquals("FAIL_CLOSED", contract.failurePolicy());
    }

    @Test
    void findReturnsEmptyForMissingFile() {
        FilesystemActivationContractReader reader =
                new FilesystemActivationContractReader(tempDir.resolve("activations"));
        assertTrue(reader.find("AC-NOPE").isEmpty());
    }

    @Test
    void findFailsClosedOnMalformedJson() throws IOException {
        Path dir = Files.createDirectory(tempDir.resolve("activations2"));
        Files.writeString(dir.resolve("AC-BAD.json"), "{not-json}", StandardCharsets.UTF_8);

        FilesystemActivationContractReader reader = new FilesystemActivationContractReader(dir);
        assertTrue(reader.find("AC-BAD").isEmpty());
    }

    @Test
    void findFailsClosedWhenRequiredFieldMissing() throws IOException {
        Path dir = Files.createDirectory(tempDir.resolve("activations3"));
        Files.writeString(dir.resolve("AC-BAD.json"), "{\"contractId\":\"AC-BAD\"}", StandardCharsets.UTF_8);

        FilesystemActivationContractReader reader = new FilesystemActivationContractReader(dir);
        assertTrue(reader.find("AC-BAD").isEmpty());
    }

    @Test
    void findFailsClosedForBlank() {
        FilesystemActivationContractReader reader =
                new FilesystemActivationContractReader(tempDir.resolve("activations"));
        assertTrue(reader.find(null).isEmpty());
        assertTrue(reader.find("  ").isEmpty());
    }

    @Test
    void findFailsClosedOnPathTraversal() {
        FilesystemActivationContractReader reader =
                new FilesystemActivationContractReader(tempDir.resolve("activations"));
        assertTrue(reader.find("../../etc/passwd").isEmpty());
        assertTrue(reader.find("..").isEmpty());
        assertTrue(reader.find("/etc/passwd").isEmpty());
        assertTrue(reader.find("a/b").isEmpty());
    }

    private static final String CONTRACT = """
            {"schemaVersion":"1.0.0","contractId":"AC-PREVISIT-001","version":"0.1.0","taskType":"PRE_VISIT_PREPARATION","routeMode":"ONTOLOGY_THEN_MAP","preconditions":{"requiredInputs":["callerId","customerId"],"requiredRoles":["RELATIONSHIP_MANAGER"],"permissionDecisionRequired":true},"activations":[{"assetId":"ASSET-DATA-CUSTOMER-PROFILE","required":true,"purpose":"p","sequence":1}],"semanticQueries":["SQ-CUSTOMER-RELATIONSHIP"],"ruleChecks":["CLAIM_NOT_FACT"],"skills":["SP-02"],"context":{"maxTokens":12000,"priorityOrder":["VERIFIED_FACT"],"trimPolicy":"CONTRACT_PRIORITY"},"humanGates":["HG-B01"],"failurePolicy":"FAIL_CLOSED"}
            """;
}
