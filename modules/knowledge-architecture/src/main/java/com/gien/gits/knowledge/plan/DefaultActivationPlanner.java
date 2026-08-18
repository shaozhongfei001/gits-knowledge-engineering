package com.gien.gits.knowledge.plan;

import com.gien.gits.knowledge.ActivationContract;
import com.gien.gits.knowledge.ActivationPlan;
import com.gien.gits.knowledge.AssetManifest;
import com.gien.gits.knowledge.KnowledgeMap;
import com.gien.gits.knowledge.port.ActivationContractPort;
import com.gien.gits.knowledge.port.ActivationPlannerPort;
import com.gien.gits.knowledge.port.AssetCatalogPort;
import com.gien.gits.knowledge.port.KnowledgeMapPort;
import com.gien.gits.knowledge.port.RoutePolicyEvaluatorPort;
import com.gien.gits.knowledge.route.RouteDecision;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 默认激活计划生成器（P20 shadow 基础设施，不迁移生产流程、不启用 fusion、不执行生产写回）。
 *
 * <p>由 Route Policy Evaluator 解析唯一合法 route，再经 Activation Contract → Asset Catalog
 * 汇合为可重放的 {@link ActivationPlan}。所有校验 fail-closed：任务未映射、合同不在 P20、
 * 权限未决、请求 production mode、请求生产写回、route/contract 不一致、资产未登记等一律返回
 * {@link PlanDecision.Deny}，绝不返回部分计划，绝无业务副作用。</p>
 */
public final class DefaultActivationPlanner implements ActivationPlannerPort {

    /** 权限决策未决标记。 */
    private static final String PERMISSION_PENDING = "PENDING";

    /** Shadow 规划器版本，用于 trace.plannerVersion 与 golden 区分。 */
    public static final String PLANNER_VERSION = "p20-shadow-0.1.0";

    /** Ontology 版本占位（shadow 阶段不加载真实本体，仅登记合同引用）。 */
    private static final String ONTOLOGY_VERSION = "CTR-SEM-001/002@CURRENT_BASE";

    private final RoutePolicyEvaluatorPort routeEvaluator;
    private final ActivationContractPort activationContractPort;
    private final AssetCatalogPort assetCatalogPort;
    private final KnowledgeMapPort knowledgeMapPort;
    private final Supplier<Instant> clock;

    public DefaultActivationPlanner(
            RoutePolicyEvaluatorPort routeEvaluator,
            ActivationContractPort activationContractPort,
            AssetCatalogPort assetCatalogPort,
            KnowledgeMapPort knowledgeMapPort,
            Supplier<Instant> clock) {
        this.routeEvaluator = Objects.requireNonNull(routeEvaluator, "routeEvaluator");
        this.activationContractPort = Objects.requireNonNull(activationContractPort, "activationContractPort");
        this.assetCatalogPort = Objects.requireNonNull(assetCatalogPort, "assetCatalogPort");
        this.knowledgeMapPort = Objects.requireNonNull(knowledgeMapPort, "knowledgeMapPort");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public PlanDecision plan(ActivationPlanRequest request) {
        if (request == null) {
            return new PlanDecision.Deny("DENY_UNMAPPED_TASK", "request is null (fail-closed)");
        }
        if (request.executionMode() == ExecutionMode.PRODUCTION) {
            return new PlanDecision.Deny("DENY_PRODUCTION_MODE",
                    "production mode is not authorized for P20 shadow slice");
        }
        if (request.writebackRequested()) {
            return new PlanDecision.Deny("DENY_WRITEBACK",
                    "production writeback is not authorized for P20 shadow slice");
        }
        if (!isPermissionResolved(request.permissionDecisionId())) {
            return new PlanDecision.Deny("DENY_PERMISSION_PENDING",
                    "permission decision is missing or pending (fail-closed)");
        }

        RouteDecision routeDecision = routeEvaluator.evaluate(request.taskType(), request.executionMode());
        if (!routeDecision.isAllowed()) {
            return toPlanDeny(routeDecision);
        }
        RouteDecision.AllowRoute route = routeDecision.allow().orElseThrow();

        Optional<ActivationContract> contractOpt =
                activationContractPort.find(route.activationContractRef());
        if (contractOpt.isEmpty()) {
            return new PlanDecision.Deny("DENY_NOT_IN_P20",
                    "activation contract '" + route.activationContractRef() + "' not resolvable (fail-closed)");
        }
        ActivationContract contract = contractOpt.get();

        // 引用完整性：route 与 activation contract 的 taskType / routeMode 必须一致（否则 fail-closed）。
        if (!request.taskType().equals(contract.taskType())
                || !route.mode().equals(contract.routeMode())) {
            return new PlanDecision.Deny("DENY_CONTRACT_MISMATCH",
                    "route policy and activation contract disagree for task '"
                            + request.taskType() + "' (routeMode=" + route.mode()
                            + ", contract=" + contract.routeMode() + ")");
        }

        Optional<KnowledgeMap> mapOpt = knowledgeMapPort.loadRoot();
        if (mapOpt.isEmpty()) {
            return new PlanDecision.Deny("DENY_UNMAPPED_TASK", "knowledge map not resolvable (fail-closed)");
        }
        KnowledgeMap map = mapOpt.get();

        // 解析合同声明的资产；任一未登记则 fail-closed 拒绝（SEC-003）。
        List<ActivationPlan.SelectedAsset> selectedAssets = resolveAssets(contract);
        if (selectedAssets == null) {
            return new PlanDecision.Deny("DENY",
                    "one or more activation assets are not registered in Asset Manifest (fail-closed)");
        }

        Instant now = clock.get();
        String planId = "AP-" + request.taskType() + "-" + safeToken(request.customerId()) + "-SHADOW";
        String taskId = "TASK-" + request.taskType() + "-" + safeToken(request.customerId()) + "-SHADOW";
        String planHash = sha256(deterministicContent(route.mode(), selectedAssets, contract));

        ActivationPlan plan = new ActivationPlan(
                "1.0.0",
                planId,
                taskId,
                request.taskType(),
                route.mode(),
                new ActivationPlan.Versions(
                        map.mapId() + "@" + map.version(),
                        route.policyId() + "@" + route.policyVersion(),
                        contract.contractId() + "@" + contract.version(),
                        ONTOLOGY_VERSION),
                selectedAssets,
                contract.semanticQueries(),
                contract.ruleChecks(),
                contract.skills(),
                new ActivationPlan.Context(
                        contract.context().maxTokens(),
                        contract.context().trimPolicy()),
                request.permissionDecisionId(),
                new ActivationPlan.Trace(planHash, now.toString(), PLANNER_VERSION));

        return new PlanDecision.AllowPlan(plan);
    }

    private static PlanDecision toPlanDeny(RouteDecision decision) {
        return new PlanDecision.Deny(decision instanceof RouteDecision.Deny deny ? deny.decisionCode() : "DENY",
                decision instanceof RouteDecision.Deny deny ? deny.reason() : "route evaluation denied (fail-closed)");
    }

    private boolean isPermissionResolved(String permissionDecisionId) {
        return permissionDecisionId != null
                && !permissionDecisionId.isBlank()
                && !PERMISSION_PENDING.equalsIgnoreCase(permissionDecisionId);
    }

    /**
     * 解析合同声明的激活资产，按 sequence 排序，并逐项验证资产已登记。
     *
     * @return 已解析资产列表；任一资产未登记返回 {@code null}（fail-closed）
     */
    private List<ActivationPlan.SelectedAsset> resolveAssets(ActivationContract contract) {
        List<ActivationPlan.SelectedAsset> result = new java.util.ArrayList<>();
        List<ActivationContract.Activation> activations = new java.util.ArrayList<>(contract.activations());
        activations.sort(Comparator.comparing(a -> a.sequence() == null ? Integer.MAX_VALUE : a.sequence()));

        for (ActivationContract.Activation activation : activations) {
            Optional<AssetManifest> assetOpt = assetCatalogPort.find(activation.assetId());
            if (assetOpt.isEmpty()) {
                return null; // 资产未登记 → fail-closed
            }
            AssetManifest asset = assetOpt.get();
            result.add(new ActivationPlan.SelectedAsset(
                    asset.assetId(),
                    asset.version(),
                    activation.required(),
                    activation.sequence()));
        }
        return List.copyOf(result);
    }

    /** 确定性内容用于 planHash：仅含黄金可比对的确定性字段。 */
    private String deterministicContent(
            String routeMode,
            List<ActivationPlan.SelectedAsset> selectedAssets,
            ActivationContract contract) {
        StringBuilder sb = new StringBuilder();
        sb.append(routeMode).append('|');
        for (ActivationPlan.SelectedAsset asset : selectedAssets) {
            sb.append(asset.assetId()).append('@').append(asset.version())
                    .append(':').append(asset.required()).append(':').append(asset.sequence()).append(';');
        }
        sb.append(contract.semanticQueries()).append('|');
        sb.append(contract.ruleChecks()).append('|');
        sb.append(contract.skills()).append('|');
        sb.append(contract.context().maxTokens()).append('|').append(contract.context().trimPolicy());
        return sb.toString();
    }

    private static String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 unavailable", error);
        }
    }

    private static String safeToken(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        return value.replaceAll("[^A-Za-z0-9-]", "_");
    }
}
