package com.gien.gits.knowledge.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.ActivationContract;
import com.gien.gits.knowledge.ActivationPlan;
import com.gien.gits.knowledge.AssetManifest;
import com.gien.gits.knowledge.KnowledgeMap;
import com.gien.gits.knowledge.RoutePolicy;
import com.gien.gits.knowledge.port.ActivationContractPort;
import com.gien.gits.knowledge.port.AssetCatalogPort;
import com.gien.gits.knowledge.port.KnowledgeMapPort;
import com.gien.gits.knowledge.port.RoutePolicyEvaluatorPort;
import com.gien.gits.knowledge.port.RoutePolicyPort;
import com.gien.gits.knowledge.route.DefaultRoutePolicyEvaluator;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class DefaultActivationPlannerTest {

    private static final Instant NOW = Instant.parse("2026-08-18T09:00:00Z");

    private static RoutePolicy routePolicy() {
        return new RoutePolicy("1.0.0", "RP-CORP-RM-001", "0.1.0", "MAP_FIRST", "DENY_UNMAPPED_TASK",
                List.of(
                        new RoutePolicy.Rule(10, "FACT_RECONCILIATION_30M", "ONTOLOGY_FIRST", "AC-FACT-RECONCILIATION-001", "r"),
                        new RoutePolicy.Rule(20, "PRE_VISIT_PREPARATION", "ONTOLOGY_THEN_MAP", "AC-PREVISIT-001", "r"),
                        new RoutePolicy.Rule(30, "MARKET_SIGNAL_DISCOVERY", "MAP_THEN_ONTOLOGY", "AC-NOT-IN-P20", "r")));
    }

    private static ActivationContract previsitContract() {
        return new ActivationContract("1.0.0", "AC-PREVISIT-001", "0.1.0", "PRE_VISIT_PREPARATION", "ONTOLOGY_THEN_MAP",
                new ActivationContract.Preconditions(List.of("callerId", "customerId"), List.of("RELATIONSHIP_MANAGER"), true),
                List.of(new ActivationContract.Activation("ASSET-DATA-CUSTOMER-PROFILE", true, "p", 1),
                        new ActivationContract.Activation("ASSET-KNOW-CUSTOMER-ONTOLOGY", true, "p", 2)),
                List.of("SQ-CUSTOMER-RELATIONSHIP"), List.of("CLAIM_NOT_FACT"), List.of("SP-02"),
                new ActivationContract.Context(12000, List.of("VERIFIED_FACT"), "CONTRACT_PRIORITY"),
                List.of("HG-B01"), "FAIL_CLOSED");
    }

    private static AssetManifest asset(String id, String version) {
        return new AssetManifest("1.0.0", id, "FOUNDATIONAL_DATA", "n", "KD-CORP-RM", version, "VALIDATION",
                new AssetManifest.Source("FILESYSTEM_MOCK", "uri", "SYNTHETIC", "RUNTIME_FETCH", null),
                new AssetManifest.Governance("o", "SENSITIVE", "CALLER", List.of("READ")),
                List.of("c"),
                new AssetManifest.Activation("QUERY", "adapter", List.of("p"), 1800, "FAIL_CLOSED"),
                new AssetManifest.Evidence(true, true, true), List.of(), List.of());
    }

    private static KnowledgeMap knowledgeMap() {
        return new KnowledgeMap("1.0.0", "KM-GITS-ROOT", "root", "0.1.0", "VALIDATION", "ROOT",
                new KnowledgeMap.Entrypoints(List.of("AGENT"), List.of("PRE_VISIT_PREPARATION")),
                List.of(new KnowledgeMap.Domain("KD-CORP-RM", "d", "p", "maps/x.md")),
                List.of(), List.of(), List.of(), "RP-CORP-RM-001", "DENY", 1200);
    }

    private static DefaultActivationPlanner planner(Map<String, AssetManifest> assets) {
        return planner(assets, previsitContract());
    }

    private static DefaultActivationPlanner planner(
            Map<String, AssetManifest> assets, ActivationContract contract) {
        RoutePolicy policy = routePolicy();
        RoutePolicyEvaluatorPort evaluator =
                new DefaultRoutePolicyEvaluator(new FakeRoutePolicyPort(policy), "RP-CORP-RM-001");
        return new DefaultActivationPlanner(
                evaluator,
                new FakeActivationContractPort(Map.of(contract.contractId(), contract)),
                new FakeAssetCatalogPort(assets),
                new FakeKnowledgeMapPort(Optional.of(knowledgeMap())),
                fixedClock());
    }

    private static Supplier<Instant> fixedClock() {
        return () -> NOW;
    }

    @Test
    void plansPreVisitWithResolvedAssetsAndRouteMode() {
        DefaultActivationPlanner planner = planner(Map.of(
                "ASSET-DATA-CUSTOMER-PROFILE", asset("ASSET-DATA-CUSTOMER-PROFILE", "0.1.0"),
                "ASSET-KNOW-CUSTOMER-ONTOLOGY", asset("ASSET-KNOW-CUSTOMER-ONTOLOGY", "0.2.0")));

        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "PRE_VISIT_PREPARATION", "CUST-001", "RELATIONSHIP_MANAGER",
                "PD-ALLOW", NOW, null));

        assertTrue(decision.isAllowed(), () -> "expected allow, got=" + decision);
        ActivationPlan plan = decision.planOpt().orElseThrow();
        assertEquals("ONTOLOGY_THEN_MAP", plan.routeMode());
        assertEquals("PRE_VISIT_PREPARATION", plan.taskType());
        assertEquals("PD-ALLOW", plan.permissionDecisionId());
        assertEquals(2, plan.selectedAssets().size());
        assertEquals("ASSET-DATA-CUSTOMER-PROFILE", plan.selectedAssets().get(0).assetId());
        assertEquals("ASSET-KNOW-CUSTOMER-ONTOLOGY", plan.selectedAssets().get(1).assetId());
        assertNotNull(plan.trace().planHash());
        assertEquals(DefaultActivationPlanner.PLANNER_VERSION, plan.trace().plannerVersion());
        assertEquals(List.of("CLAIM_NOT_FACT"), plan.ruleChecks());
        assertEquals("12000", String.valueOf(plan.context().maxTokens()));
    }

    @Test
    void deniesUnmappedTask() {
        DefaultActivationPlanner planner = planner(Map.of());
        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "UNKNOWN_TASK", "CUST-001", "AGENT", "PD-ALLOW", NOW, null));

        assertInstanceOf(PlanDecision.Deny.class, decision);
        PlanDecision.Deny deny = (PlanDecision.Deny) decision;
        assertEquals("DENY_UNMAPPED_TASK", deny.decisionCode());
    }

    @Test
    void deniesNotInP20Task() {
        DefaultActivationPlanner planner = planner(Map.of());
        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "MARKET_SIGNAL_DISCOVERY", "CUST-001", "AGENT", "PD-ALLOW", NOW, null));

        PlanDecision.Deny deny = assertInstanceOf(PlanDecision.Deny.class, decision);
        assertEquals("DENY_NOT_IN_P20", deny.decisionCode());
    }

    @Test
    void deniesWhenPermissionPending() {
        DefaultActivationPlanner planner = planner(Map.of());
        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "PRE_VISIT_PREPARATION", "CUST-001", "RELATIONSHIP_MANAGER", "PENDING", NOW, null));

        PlanDecision.Deny deny = assertInstanceOf(PlanDecision.Deny.class, decision);
        assertEquals("DENY_PERMISSION_PENDING", deny.decisionCode());
    }

    @Test
    void deniesWhenPermissionMissing() {
        DefaultActivationPlanner planner = planner(Map.of());
        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "PRE_VISIT_PREPARATION", "CUST-001", "RELATIONSHIP_MANAGER", null, NOW, null));

        PlanDecision.Deny deny = assertInstanceOf(PlanDecision.Deny.class, decision);
        assertEquals("DENY_PERMISSION_PENDING", deny.decisionCode());
    }

    @Test
    void deniesWhenAssetNotRegistered() {
        // 合同声明了两个资产，但只登记了一个 → fail-closed 拒绝（SEC-003）
        DefaultActivationPlanner planner = planner(Map.of(
                "ASSET-DATA-CUSTOMER-PROFILE", asset("ASSET-DATA-CUSTOMER-PROFILE", "0.1.0")));

        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "PRE_VISIT_PREPARATION", "CUST-001", "RELATIONSHIP_MANAGER", "PD-ALLOW", NOW, null));

        PlanDecision.Deny deny = assertInstanceOf(PlanDecision.Deny.class, decision);
        assertEquals("DENY", deny.decisionCode());
    }

    @Test
    void deniesBlankTaskType() {
        DefaultActivationPlanner planner = planner(Map.of());
        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "   ", "CUST-001", "AGENT", "PD-ALLOW", NOW, null));

        PlanDecision.Deny deny = assertInstanceOf(PlanDecision.Deny.class, decision);
        assertEquals("DENY_UNMAPPED_TASK", deny.decisionCode());
    }

    @Test
    void deniesProductionMode() {
        DefaultActivationPlanner planner = planner(Map.of(
                "ASSET-DATA-CUSTOMER-PROFILE", asset("ASSET-DATA-CUSTOMER-PROFILE", "0.1.0"),
                "ASSET-KNOW-CUSTOMER-ONTOLOGY", asset("ASSET-KNOW-CUSTOMER-ONTOLOGY", "0.2.0")));

        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "PRE_VISIT_PREPARATION", "CUST-001", "RELATIONSHIP_MANAGER",
                "PD-ALLOW", NOW, null, ExecutionMode.PRODUCTION, false));

        PlanDecision.Deny deny = assertInstanceOf(PlanDecision.Deny.class, decision);
        assertEquals("DENY_PRODUCTION_MODE", deny.decisionCode());
    }

    @Test
    void deniesProductionWriteback() {
        DefaultActivationPlanner planner = planner(Map.of(
                "ASSET-DATA-CUSTOMER-PROFILE", asset("ASSET-DATA-CUSTOMER-PROFILE", "0.1.0"),
                "ASSET-KNOW-CUSTOMER-ONTOLOGY", asset("ASSET-KNOW-CUSTOMER-ONTOLOGY", "0.2.0")));

        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "PRE_VISIT_PREPARATION", "CUST-001", "RELATIONSHIP_MANAGER",
                "PD-ALLOW", NOW, null, ExecutionMode.SHADOW, true));

        PlanDecision.Deny deny = assertInstanceOf(PlanDecision.Deny.class, decision);
        assertEquals("DENY_WRITEBACK", deny.decisionCode());
    }

    @Test
    void deniesWhenRouteAndContractDisagree() {
        // route 规则为 ONTOLOGY_THEN_MAP，合同故意改为 MAP_FIRST → route/contract 不一致
        ActivationContract mismatch = new ActivationContract(
                "1.0.0", "AC-PREVISIT-001", "0.1.0", "PRE_VISIT_PREPARATION", "MAP_FIRST",
                new ActivationContract.Preconditions(List.of("callerId", "customerId"), List.of("RELATIONSHIP_MANAGER"), true),
                List.of(new ActivationContract.Activation("ASSET-DATA-CUSTOMER-PROFILE", true, "p", 1)),
                List.of("SQ-CUSTOMER-RELATIONSHIP"), List.of("CLAIM_NOT_FACT"), List.of("SP-02"),
                new ActivationContract.Context(12000, List.of("VERIFIED_FACT"), "CONTRACT_PRIORITY"),
                List.of("HG-B01"), "FAIL_CLOSED");

        DefaultActivationPlanner planner = planner(Map.of(
                "ASSET-DATA-CUSTOMER-PROFILE", asset("ASSET-DATA-CUSTOMER-PROFILE", "0.1.0")), mismatch);

        PlanDecision decision = planner.plan(new ActivationPlanRequest(
                "PRE_VISIT_PREPARATION", "CUST-001", "RELATIONSHIP_MANAGER",
                "PD-ALLOW", NOW, null));

        PlanDecision.Deny deny = assertInstanceOf(PlanDecision.Deny.class, decision);
        assertEquals("DENY_CONTRACT_MISMATCH", deny.decisionCode());
    }

    @Test
    void routeEvaluatorDeniesUnmappedTaskWithPolicyId() {
        RoutePolicyEvaluatorPort evaluator =
                new DefaultRoutePolicyEvaluator(new FakeRoutePolicyPort(routePolicy()), "RP-CORP-RM-001");
        com.gien.gits.knowledge.route.RouteDecision decision =
                evaluator.evaluate("UNKNOWN_TASK", ExecutionMode.SHADOW);

        assertInstanceOf(com.gien.gits.knowledge.route.RouteDecision.Deny.class, decision);
        assertEquals("DENY_UNMAPPED_TASK",
                ((com.gien.gits.knowledge.route.RouteDecision.Deny) decision).decisionCode());
    }

    @Test
    void routeEvaluatorDeniesProductionMode() {
        RoutePolicyEvaluatorPort evaluator =
                new DefaultRoutePolicyEvaluator(new FakeRoutePolicyPort(routePolicy()), "RP-CORP-RM-001");
        com.gien.gits.knowledge.route.RouteDecision decision =
                evaluator.evaluate("PRE_VISIT_PREPARATION", ExecutionMode.PRODUCTION);

        assertEquals("DENY_PRODUCTION_MODE",
                ((com.gien.gits.knowledge.route.RouteDecision.Deny) decision).decisionCode());
    }

    @Test
    void routeEvaluatorAllowsInScopeTaskWithContractRef() {
        RoutePolicyEvaluatorPort evaluator =
                new DefaultRoutePolicyEvaluator(new FakeRoutePolicyPort(routePolicy()), "RP-CORP-RM-001");
        com.gien.gits.knowledge.route.RouteDecision decision =
                evaluator.evaluate("PRE_VISIT_PREPARATION", ExecutionMode.SHADOW);

        assertTrue(decision.isAllowed(), () -> "expected allow, got=" + decision);
        com.gien.gits.knowledge.route.RouteDecision.AllowRoute allow = decision.allow().orElseThrow();
        assertEquals("ONTOLOGY_THEN_MAP", allow.mode());
        assertEquals("AC-PREVISIT-001", allow.activationContractRef());
        assertEquals("RP-CORP-RM-001", allow.policyId());
    }

    // --- 假 Port 实现（测试替身） ---

    private record FakeRoutePolicyPort(RoutePolicy policy) implements RoutePolicyPort {
        @Override public Optional<RoutePolicy> find(String policyId) {
            return policy.policyId().equals(policyId) ? Optional.of(policy) : Optional.empty();
        }
    }

    private record FakeActivationContractPort(Map<String, ActivationContract> contracts)
            implements ActivationContractPort {
        @Override public Optional<ActivationContract> find(String contractId) {
            return Optional.ofNullable(contracts.get(contractId));
        }
    }

    private record FakeAssetCatalogPort(Map<String, AssetManifest> assets) implements AssetCatalogPort {
        @Override public Optional<AssetManifest> find(String assetId) {
            return Optional.ofNullable(assets.get(assetId));
        }
        @Override public List<AssetManifest> listByDomain(String domain) {
            return List.of();
        }
        @Override public List<AssetManifest> listAll() {
            return List.copyOf(assets.values());
        }
    }

    private record FakeKnowledgeMapPort(Optional<KnowledgeMap> map) implements KnowledgeMapPort {
        @Override public Optional<KnowledgeMap> loadRoot() {
            return map;
        }
        @Override public Optional<KnowledgeMap> load(String mapId) {
            return Optional.empty();
        }
    }
}
