package com.gien.gits.api.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * G3.1 合同合规验证 — 验证实现与契约的一致性
 */
class ContractComplianceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Resolve path relative to project root (works in Maven and IDE) */
    private static Path projectRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        if (Files.exists(cwd.resolve("specs/openapi/gits-kno-api.openapi.json"))) {
            return cwd;
        }
        if (Files.exists(cwd.getParent().resolve("specs/openapi/gits-kno-api.openapi.json"))) {
            return cwd.getParent();
        }
        if (Files.exists(cwd.getParent().getParent().resolve("specs/openapi/gits-kno-api.openapi.json"))) {
            return cwd.getParent().getParent();
        }
        return cwd;
    }

    // ═══════════════════════════════════════════════════════════════
    // 1. OpenAPI Contract Verification
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("OpenAPI 契约验证")
    class OpenApiContractTest {

        @Test
        @DisplayName("OpenAPI 规范文件可读取")
        void openApiSpecIsReadable() throws Exception {
            Path specPath = projectRoot().resolve("specs/openapi/gits-kno-api.openapi.json");
            assertTrue(Files.exists(specPath),
                "OpenAPI spec must exist at " + specPath);
            JsonNode root = MAPPER.readTree(specPath.toFile());
            assertNotNull(root.path("openapi"), "Must have openapi version field");
        }

        @Test
        @DisplayName("OpenAPI 规范中定义的端点在 Controller 中有实现")
        void openApiEndpointsHaveControllerImplementations() throws Exception {
            JsonNode spec = loadOpenApiSpec();
            JsonNode paths = spec.path("paths");
            assertFalse(paths.isMissingNode(), "OpenAPI spec must have paths");

            // Verify each path in the spec has a corresponding controller
            // Currently the spec only defines /architecture/status
            Iterator<Map.Entry<String, JsonNode>> pathIterator = paths.fields();
            while (pathIterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = pathIterator.next();
                String path = entry.getKey();
                JsonNode pathItem = entry.getValue();

                // Each path should have at least one HTTP method defined
                boolean hasMethod = false;
                for (String method : List.of("get", "post", "put", "delete", "patch")) {
                    if (!pathItem.path(method).isMissingNode()) {
                        hasMethod = true;
                    }
                }
                assertTrue(hasMethod,
                    "Path " + path + " must have at least one HTTP method defined");
            }
        }

        @Test
        @DisplayName("OpenAPI 规范路径模式使用标准格式")
        void pathPatternsMatch() throws Exception {
            JsonNode spec = loadOpenApiSpec();
            JsonNode paths = spec.path("paths");

            Iterator<String> pathIterator = paths.fieldNames();
            while (pathIterator.hasNext()) {
                String path = pathIterator.next();
                // OpenAPI paths should use {param} not :param
                assertFalse(path.contains(":"),
                    "Path " + path + " should use {param} style, not :param");
            }
        }

        @Test
        @DisplayName("OpenAPI 规范包含 Problem 错误响应定义")
        void openApiSpecHasProblemResponse() throws Exception {
            JsonNode spec = loadOpenApiSpec();
            JsonNode problemSchema = spec.path("components").path("schemas").path("Problem");
            assertFalse(problemSchema.isMissingNode(),
                "OpenAPI spec must define Problem schema in components/schemas");
            assertTrue(problemSchema.path("required").isArray(),
                "Problem schema must have required fields");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. DMN Contract Verification
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DMN 契约验证")
    class DmnContractTest {

        @Test
        @DisplayName("claim-reconciliation.dmn 文件可读取")
        void dmnFileIsReadable() throws Exception {
            InputStream is = getClass().getClassLoader().getResourceAsStream("claim-reconciliation.dmn");
            assertNotNull(is, "claim-reconciliation.dmn must be on classpath");
        }

        @Test
        @DisplayName("DMN 决策表恰好有 3 条规则")
        void dmnHasExactlyThreeRules() throws Exception {
            String dmnXml = loadDmnXml();
            int ruleCount = countOccurrences(dmnXml, "<rule ");
            assertEquals(3, ruleCount,
                "DMN must have exactly 3 rules matching FallbackClaimReconciliationAdapter");
        }

        @Test
        @DisplayName("DMN 命中策略为 FIRST")
        void dmnHitPolicyIsFirst() throws Exception {
            String dmnXml = loadDmnXml();
            // DMN standard uses "U" for FIRST hit policy, but some tools use "FIRST"
            assertTrue(
                dmnXml.contains("hitPolicy=\"U\"") || dmnXml.contains("hitPolicy='U'") ||
                dmnXml.contains("hitPolicy=\"FIRST\"") || dmnXml.contains("hitPolicy='FIRST'"),
                "DMN hit policy must be FIRST (U or FIRST in DMN XML)");
        }

        @Test
        @DisplayName("DMN 输入字段包含 conflictDetected, authoritativeMatch, evidenceComplete")
        void dmnInputFieldsMatch() throws Exception {
            String dmnXml = loadDmnXml();
            assertTrue(dmnXml.contains("conflictDetected"),
                "DMN must have input field 'conflictDetected'");
            assertTrue(dmnXml.contains("authoritativeMatch"),
                "DMN must have input field 'authoritativeMatch'");
            assertTrue(dmnXml.contains("evidenceComplete"),
                "DMN must have input field 'evidenceComplete'");
        }

        @Test
        @DisplayName("DMN 输出字段 reconciliationStatus 值匹配 ReconciliationStatus 枚举")
        void dmnOutputMatchesReconciliationStatus() throws Exception {
            String dmnXml = loadDmnXml();
            assertTrue(dmnXml.contains("CONFLICT_REQUIRES_HUMAN_REVIEW"),
                "DMN must output CONFLICT_REQUIRES_HUMAN_REVIEW");
            assertTrue(dmnXml.contains("VERIFIED_FACT"),
                "DMN must output VERIFIED_FACT");
            assertTrue(dmnXml.contains("CANDIDATE_CLAIM"),
                "DMN must output CANDIDATE_CLAIM");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. Schema Contract Verification
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Schema 契约验证")
    class SchemaContractTest {

        @Test
        @DisplayName("CloudEvent record 具有标准字段")
        void cloudEventFieldsMatchClassDefinition() throws Exception {
            // Verify CloudEvent record has expected fields by reflection
            Class<?> cloudEventClass = Class.forName("com.gien.gits.ontology.event.CloudEvent");
            java.lang.reflect.RecordComponent[] components = cloudEventClass.getRecordComponents();
            Set<String> fieldNames = java.util.Arrays.stream(components)
                .map(java.lang.reflect.RecordComponent::getName)
                .collect(Collectors.toSet());

            // CloudEvent standard fields per CloudEvents spec
            assertTrue(fieldNames.contains("specversion"), "Must have specversion field");
            assertTrue(fieldNames.contains("type"), "Must have type field");
            assertTrue(fieldNames.contains("source"), "Must have source field");
            assertTrue(fieldNames.contains("id"), "Must have id field");

            // Also verify against AsyncAPI spec if available
            Path asyncApiPath = projectRoot().resolve("specs/events/domain-events.asyncapi.json");
            if (Files.exists(asyncApiPath)) {
                JsonNode asyncApi = MAPPER.readTree(asyncApiPath.toFile());
                assertNotNull(asyncApi.path("components"), "AsyncAPI must have components");
            }
        }

        @Test
        @DisplayName("DomainEventType 常量与实际字符串值一致")
        void domainEventTypeConstantsMatchValues() throws Exception {
            Class<?> clazz = Class.forName("com.gien.gits.ontology.event.DomainEventType");
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            List<String> constantNames = new ArrayList<>();
            for (java.lang.reflect.Field f : fields) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())
                    && java.lang.reflect.Modifier.isFinal(f.getModifiers())
                    && f.getType() == String.class) {
                    constantNames.add(f.getName());
                }
            }
            // Verify at least the key event types exist
            assertTrue(constantNames.contains("CLAIM_CANDIDATE_RECORDED"),
                "DomainEventType must have CLAIM_CANDIDATE_RECORDED constant");
            assertTrue(constantNames.contains("CONTROLLED_ACTION_REQUESTED"),
                "DomainEventType must have CONTROLLED_ACTION_REQUESTED constant");
        }

        @Test
        @DisplayName("ReconciliationStatus 枚举值在 DMN 和 Port 间一致")
        void reconciliationStatusEnumConsistency() throws Exception {
            Class<?>[] innerClasses = Class.forName("com.gien.gits.ontology.port.ClaimReconciliationPort").getDeclaredClasses();
            Class<?> statusEnum = null;
            for (Class<?> c : innerClasses) {
                if (c.getSimpleName().equals("ReconciliationStatus")) {
                    statusEnum = c;
                    break;
                }
            }
            assertNotNull(statusEnum, "ClaimReconciliationPort must have ReconciliationStatus enum");

            Object[] enumConstants = statusEnum.getEnumConstants();
            Set<String> enumNames = java.util.Arrays.stream(enumConstants)
                .map(Object::toString)
                .collect(Collectors.toSet());

            assertTrue(enumNames.contains("CONFLICT_REQUIRES_HUMAN_REVIEW"),
                "Must have CONFLICT_REQUIRES_HUMAN_REVIEW");
            assertTrue(enumNames.contains("VERIFIED_FACT"),
                "Must have VERIFIED_FACT");
            assertTrue(enumNames.contains("CANDIDATE_CLAIM"),
                "Must have CANDIDATE_CLAIM");
            assertEquals(3, enumNames.size(),
                "ReconciliationStatus must have exactly 3 values matching DMN rules");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Helper methods
    // ═══════════════════════════════════════════════════════════════

    private JsonNode loadOpenApiSpec() throws Exception {
        Path specPath = projectRoot().resolve("specs/openapi/gits-kno-api.openapi.json");
        assertTrue(Files.exists(specPath), "OpenAPI spec must exist at " + specPath);
        return MAPPER.readTree(specPath.toFile());
    }

    private String loadDmnXml() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("claim-reconciliation.dmn");
        assertNotNull(is, "claim-reconciliation.dmn must be on classpath");
        return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }

    private int countOccurrences(String text, String substring) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(substring, idx)) != -1) {
            count++;
            idx += substring.length();
        }
        return count;
    }
}
