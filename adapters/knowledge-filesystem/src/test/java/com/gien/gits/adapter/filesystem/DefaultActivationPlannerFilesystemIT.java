package com.gien.gits.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.ActivationPlan;
import com.gien.gits.knowledge.plan.ActivationPlanRequest;
import com.gien.gits.knowledge.plan.DefaultActivationPlanner;
import com.gien.gits.knowledge.plan.PlanDecision;
import com.gien.gits.knowledge.port.ActivationContractPort;
import com.gien.gits.knowledge.port.AssetCatalogPort;
import com.gien.gits.knowledge.port.KnowledgeMapPort;
import com.gien.gits.knowledge.port.RoutePolicyPort;
import com.gien.gits.knowledge.port.RoutePolicyEvaluatorPort;
import com.gien.gits.knowledge.route.DefaultRoutePolicyEvaluator;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 端到端集成测试：用真实 filesystem readers 加载 P20 合同数据，验证
 * ActivationPlanner 产出与黄金计划的确定性字段一致。
 *
 * <p>数据源为仓库 {@code specs/knowledge-architecture/}。</p>
 */
class DefaultActivationPlannerFilesystemIT {

    private static Path KA;

    @BeforeAll
    static void resolveKnowledgeArchitectureDir() {
        KA = Path.of(".").toAbsolutePath().normalize();
        // 从模块工作目录逐级向上定位 specs/knowledge-architecture
        while (KA != null && !KA.resolve("specs/knowledge-architecture").toFile().isDirectory()) {
            KA = KA.getParent();
        }
        assertTrue(KA != null && KA.resolve("specs/knowledge-architecture").toFile().isDirectory(),
                "must locate specs/knowledge-architecture from workspace root");
        KA = KA.resolve("specs/knowledge-architecture");
    }

    private static DefaultActivationPlanner planner() {
        RoutePolicyPort route = new FilesystemRoutePolicyReader(KA.resolve("routes"));
        RoutePolicyEvaluatorPort evaluator =
                new DefaultRoutePolicyEvaluator(route, "RP-CORP-RM-001");
        ActivationContractPort contract = new FilesystemActivationContractReader(KA.resolve("activations"));
        AssetCatalogPort assets = new FilesystemAssetCatalogReader(KA.resolve("assets"));
        KnowledgeMapPort map = new FilesystemKnowledgeMapReader(KA.resolve("maps"));
        return new DefaultActivationPlanner(evaluator, contract, assets, map, () -> Instant.parse("2026-08-18T09:00:00Z"));
    }

    @Test
    void preVisitPlanMatchesGoldenDeterministicFields() {
        DefaultActivationPlanner planner = planner();
        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "PRE_VISIT_PREPARATION", "CUST-001", "RELATIONSHIP_MANAGER",
                "PD-ALLOW", Instant.parse("2026-08-18T09:00:00Z"), null));

        assertInstanceOf(PlanDecision.AllowPlan.class, decision);
        ActivationPlan plan = decision.planOpt().orElseThrow();

        assertEquals("ONTOLOGY_THEN_MAP", plan.routeMode());
        assertEquals("PRE_VISIT_PREPARATION", plan.taskType());
        // 黄金 AP-PREVISIT 的确定性字段
        assertEquals(Set.of("SQ-CUSTOMER-RELATIONSHIP", "SQ-RELATED-LEGAL-ENTITIES", "SQ-KYC-GAPS",
                        "SQ-OPEN-COMMITMENTS", "SQ-ACTIVE-PRODUCT-VERSIONS"), Set.copyOf(plan.semanticQueries()));
        assertEquals(Set.of("CALLER_SCOPE_ALLOWED", "CLAIM_NOT_FACT", "SIGNAL_NOT_OPPORTUNITY",
                        "PRODUCT_VERSION_ACTIVE"), Set.copyOf(plan.ruleChecks()));
        assertEquals(Set.of("SP-02", "SP-05", "SP-15", "SP-10"), Set.copyOf(plan.skills()));
        assertEquals(12000, plan.context().maxTokens());
        assertEquals("CONTRACT_PRIORITY", plan.context().trimPolicy());
        // 9 个资产按 sequence 排序
        assertEquals(9, plan.selectedAssets().size());
        assertEquals(1, plan.selectedAssets().get(0).sequence());
    }

    @Test
    void factReconciliationPlanMatchesGoldenDeterministicFields() {
        DefaultActivationPlanner planner = planner();
        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "FACT_RECONCILIATION_30M", "CUST-001", "RELATIONSHIP_MANAGER",
                "PD-ALLOW", Instant.parse("2026-08-18T09:05:00Z"), "CLM-3000W"));

        assertInstanceOf(PlanDecision.AllowPlan.class, decision);
        ActivationPlan plan = decision.planOpt().orElseThrow();

        assertEquals("ONTOLOGY_FIRST", plan.routeMode());
        assertEquals(Set.of("SQ-CLAIM-SUBJECT-RELATIONS", "SQ-CREDIT-AND-PROJECT-AMOUNTS",
                "SQ-PROJECT-AND-BORROWER-ENTITY"), Set.copyOf(plan.semanticQueries()));
        assertEquals(Set.of("CLAIM_NOT_FACT", "AVAILABLE_CREDIT_NOT_NEW_NEED", "PROJECT_NOT_BORROWER",
                "SIGNAL_NOT_OPPORTUNITY", "BANKABILITY_NOT_APPROVAL"), Set.copyOf(plan.ruleChecks()));
        assertEquals(Set.of("SP-02", "SP-07"), Set.copyOf(plan.skills()));
        assertEquals(8000, plan.context().maxTokens());
        assertEquals("FAIL_ON_OVERFLOW", plan.context().trimPolicy());
        // 6 个资产全部登记并按 sequence 排序
        assertEquals(6, plan.selectedAssets().size());
        assertEquals("ASSET-DATA-CUSTOMER-PROFILE", plan.selectedAssets().get(0).assetId());
        assertEquals("ASSET-KNOW-CLAIM-RECONCILIATION", plan.selectedAssets().get(5).assetId());
    }

    @Test
    void unknownTaskDenied() {
        DefaultActivationPlanner planner = planner();
        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "UNKNOWN_TASK", "CUST-001", "AGENT", "PD-ALLOW",
                Instant.parse("2026-08-18T09:00:00Z"), null));

        assertInstanceOf(PlanDecision.Deny.class, decision);
        assertEquals("DENY_UNMAPPED_TASK", ((PlanDecision.Deny) decision).decisionCode());
    }
}
