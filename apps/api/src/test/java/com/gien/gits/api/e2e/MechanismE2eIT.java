package com.gien.gits.api.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gien.gits.action.ActionDispatchPort;
import com.gien.gits.action.ControlledActionService;
import com.gien.gits.action.RecordingActionDispatcher;
import com.gien.gits.adapter.jena.JenaSemanticRepositoryAdapter;
import com.gien.gits.context.ContextAssemblyPort;
import com.gien.gits.context.DefaultContextAssembler;
import com.gien.gits.context.EvidenceBundle;
import com.gien.gits.evaluation.DefaultEvaluator;
import com.gien.gits.evaluation.EvaluationPort;
import com.gien.gits.evaluation.RunManifest;
import com.gien.gits.ontology.ActionReceipt;
import com.gien.gits.ontology.ControlledAction;
import com.gien.gits.ontology.HumanConfirmation;
import com.gien.gits.semantic.SemanticPackage;
import com.gien.gits.semantic.SemanticRepositoryPort;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * MECHANISM-level agent E2E for the knowledge-engineering contract chain.
 *
 * <p>Scope: exercises the in-process contract chain (CTR-SKILL-001 context-assembly skill,
 * CTR-SEM-001 semantic/SHACL, CTR-ACTION-001 controlled action, CTR-EVIDENCE-001 evidence
 * bundle, CTR-EVAL-001 run-manifest/evaluation) with all externals mocked or replaced by
 * in-memory/recording adapters. No AIOS, CRM, IAM, Oracle or any real external interface
 * is contacted.
 *
 * <p>This is <strong>NOT</strong> a real-interface E2E and <strong>NOT</strong> a QA pass.
 * The {@code MECHANISM_READY} gate state emitted by {@link DefaultEvaluator} is a neutral
 * engineering marker that the mechanism ran end-to-end; it MUST NOT be interpreted as a
 * QA pass, business pass, or release decision. Independent QA and real-interface
 * verification are reserved for separate actors and waves.
 *
 * <p>Fail-closed: any step failure fails the test.
 */
class MechanismE2eIT {

    private static final String CASE_SET_VERSION = "case-set-v0.1";

    @Test
    void mechanismChainEndToEndEmitsRunManifestAndMechanismReadyGate() throws Exception {
        // --- (a) Semantic / SHACL: load minimal ontology + shapes, validate conforming candidate ---
        byte[] ontology = turtle(
                "@prefix owl: <http://www.w3.org/2002/07/owl#> .",
                "@prefix gits: <https://gientech.com/gits/kno/> .",
                "gits:CoreOntology a owl:Ontology .",
                "gits:OperatingCase a owl:Class .");
        byte[] shapes = turtle(
                "@prefix sh: <http://www.w3.org/ns/shacl#> .",
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
                "@prefix gits: <https://gientech.com/gits/kno/> .",
                "gits:OperatingCaseShape a sh:NodeShape ;",
                "    sh:targetClass gits:OperatingCase ;",
                "    sh:property [",
                "        sh:path gits:caseType ;",
                "        sh:minCount 1 ;",
                "        sh:datatype xsd:string ;",
                "    ] .");
        JenaSemanticRepositoryAdapter semanticRepository = new JenaSemanticRepositoryAdapter();
        semanticRepository.load(new SemanticPackage("gits-core", "0.1.0", ontology, shapes));

        byte[] conformingCandidate = turtle(
                "@prefix gits: <https://gientech.com/gits/kno/> .",
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
                "gits:case-1 a gits:OperatingCase ;",
                "    gits:caseType \"claim-reconciliation\" .");
        SemanticRepositoryPort.ValidationResult validationResult =
                semanticRepository.validate(conformingCandidate);
        assertTrue(validationResult.conforms(),
                () -> "expected conforms=true, report=" + validationResult.reportText());
        assertEquals("0.1.0", validationResult.semanticPackageVersion());

        // --- (b) Context-assembly skill: assemble an EvidenceBundle via DefaultContextAssembler ---
        UUID caseId = UUID.randomUUID();
        Instant asOf = Instant.parse("2026-08-02T00:00:00Z");
        String permissionDecisionId = "DEC-e2e-0001";
        Map<String, Object> permissionContext = Map.of("permissionDecisionId", permissionDecisionId);
        ContextAssemblyPort assembler = new DefaultContextAssembler();
        ContextAssemblyPort.Request request = new ContextAssemblyPort.Request(
                caseId, "claim-reconciliation", "identity-token-ref-e2e", permissionContext, asOf);
        EvidenceBundle bundle = assembler.assemble(request);
        assertNotNull(bundle.bundleId());
        assertEquals(caseId, bundle.caseId());
        assertEquals("claim-reconciliation", bundle.purpose());
        assertEquals(permissionDecisionId, bundle.permissionDecisionId());
        assertEquals(asOf, bundle.assembledAt());

        // --- (c) Controlled action: AUTHORIZED confirmation dispatched via RecordingActionDispatcher ---
        HumanConfirmation confirmation = new HumanConfirmation(
                UUID.randomUUID(), UUID.randomUUID(),
                HumanConfirmation.Decision.APPROVED, "reviewer-e2e", Instant.now());
        ControlledAction.Target target = new ControlledAction.Target(
                "CRM", "TASK", "T-e2e-1", "v1",
                ControlledAction.Target.Operation.CREATE_TASK,
                Map.of("title", "follow-up-e2e"));
        ControlledAction action = new ControlledAction(
                UUID.randomUUID(), UUID.randomUUID(), confirmation, target,
                "e2e-idempotency-key-0001", Instant.now(), ControlledAction.Status.REQUESTED);
        ActionDispatchPort dispatcher = new RecordingActionDispatcher();
        ControlledActionService actionService = new ControlledActionService(dispatcher);
        ActionReceipt receipt = actionService.dispatch(action);
        assertNotNull(receipt.receiptId());
        assertEquals(action.actionId(), receipt.actionId());
        assertEquals(ActionReceipt.Status.SUCCEEDED, receipt.status());
        assertEquals("v1", receipt.targetVersionAfter());

        // --- (d) Run-manifest + evaluation: realistic non-blank versions, MECHANISM_READY gate ---
        RunManifest manifest = new RunManifest(
                UUID.randomUUID(),
                Instant.parse("2026-08-02T00:00:00Z"),
                "gits-core@0.1.0",
                "gits.kno.context-assembly@1.0.0",
                "prompt@0.1.0",
                new RunManifest.ModelVersion("openai", "gpt-4o", "parametersHash-e2e-0001"),
                List.of("claim-reconciliation@1.0.0"),
                "snapshot-2026-08-02T00:00:00Z",
                permissionDecisionId,
                "trace-e2e-0001");
        EvaluationPort evaluator = new DefaultEvaluator();
        EvaluationPort.Result result = evaluator.evaluate(manifest, CASE_SET_VERSION);
        assertEquals(DefaultEvaluator.GATE_MECHANISM_READY, result.gateState());
        assertNotNull(result.metrics());
        assertTrue(!result.metrics().isEmpty(), "metrics must be non-empty");
        // gateState must never self-claim QA or business pass
        assertTrue(!result.gateState().contains("QA_PASS"));
        assertTrue(!result.gateState().contains("BUSINESS_PASS"));

        // --- (e) Serialize RunManifest + Result to JSON artifact on disk ---
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ObjectNode artifact = mapper.createObjectNode();
        artifact.set("runManifest", mapper.valueToTree(manifest));
        ObjectNode resultNode = mapper.createObjectNode();
        resultNode.put("gateState", result.gateState());
        resultNode.set("metrics", mapper.valueToTree(result.metrics()));
        resultNode.put("caseSetVersion", CASE_SET_VERSION);
        resultNode.put("mechanismE2e", true);
        resultNode.put("realInterface", false);
        resultNode.put("qaPass", false);
        artifact.set("result", resultNode);

        // --- (f) Mapping locator evidence (CTR-MAP-001 spike, ADR-0009): hash + locator only ---
        Path r2rml = Paths.get("specs/data/customer-source-mapping.r2rml.ttl");
        if (!r2rml.isAbsolute()) {
            Path fromModule = Paths.get("../../specs/data/customer-source-mapping.r2rml.ttl");
            if (Files.exists(fromModule)) {
                r2rml = fromModule;
            }
        }
        assertTrue(Files.exists(r2rml), "CTR-MAP-001 R2RML source must exist for locator evidence");
        String r2rmlText = Files.readString(r2rml, StandardCharsets.UTF_8);
        assertTrue(r2rmlText.contains("A_ZHCX_CUST_BASE"), "spike source table locator");
        assertTrue(r2rmlText.contains("{CUSTID}"), "spike subject column locator");
        assertTrue(r2rmlText.contains("SPIKE_ONLY"), "must remain spike_only");
        String r2rmlSha = sha256Hex(r2rmlText.getBytes(StandardCharsets.UTF_8));
        ObjectNode mapping = mapper.createObjectNode();
        mapping.put("contractId", "CTR-MAP-001");
        mapping.put("adr", "ADR-0009");
        mapping.put("status", "SPIKE_ONLY");
        mapping.put("logicalTable", "A_ZHCX_CUST_BASE");
        mapping.put("subjectColumn", "CUSTID");
        mapping.put("sourceFile", "specs/data/customer-source-mapping.r2rml.ttl");
        mapping.put("sourceSha256", r2rmlSha);
        mapping.put("customerClassInCoreOntology", false);
        mapping.put("rowDataRead", false);
        artifact.set("mappingLocatorEvidence", mapping);

        String outPath = System.getProperty("e2e.manifest.out", "target/e2e-mechanism-run-manifest.json");
        Path out = Paths.get(outPath);
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        Files.writeString(out, mapper.writeValueAsString(artifact), StandardCharsets.UTF_8);
        assertTrue(Files.exists(out), "run-manifest artifact must be written to disk");
        assertTrue(Files.size(out) > 0, "run-manifest artifact must be non-empty");
        String written = Files.readString(out, StandardCharsets.UTF_8);
        assertTrue(written.contains("A_ZHCX_CUST_BASE"));
        assertTrue(written.contains("mappingLocatorEvidence"));
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] turtle(String... lines) {
        return String.join("\n", lines).getBytes(StandardCharsets.UTF_8);
    }
}
