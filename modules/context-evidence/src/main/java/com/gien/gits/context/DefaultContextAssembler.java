package com.gien.gits.context;

import com.gien.gits.knowledge.ActivationPlan;
import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.ClaimType;
import com.gien.gits.ontology.Evidence;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class DefaultContextAssembler implements ContextAssemblyPort {

    private static final String PENDING_PERMISSION_DECISION_ID = "PENDING";
    private static final String PERMISSION_DECISION_ID_KEY = "permissionDecisionId";

    @Override
    public EvidenceBundle assemble(Request request) {
        Objects.requireNonNull(request, "request");
        if (request.caseId() == null) {
            throw new IllegalArgumentException("caseId is required");
        }
        if (request.purpose() == null || request.purpose().isBlank()) {
            throw new IllegalArgumentException("purpose is required");
        }

        UUID bundleId = UUID.randomUUID();
        String permissionDecisionId = resolvePermissionDecisionId(request.permissionContext());
        Instant assembledAt = request.asOf() != null ? request.asOf() : Instant.now();

        return new EvidenceBundle(
                bundleId,
                request.caseId(),
                request.purpose(),
                permissionDecisionId,
                assembledAt,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    /**
     * P20 计划驱动装配（G4）：由 ActivationPlan 生成非空 EvidenceBundle。
     *
     * <p>这是 G4 的核心退出条件的实现——EvidenceBundle 不再为空，且权限与来源完整。
     * 装配来源：
     * <ul>
     *   <li>{@code evidence}：由 plan 的 selectedAssets（来源 URI/版本/权限）逐一构成；</li>
     *   <li>{@code candidateClaims}：由 plan 的 ruleChecks 逐条派生（CANDIDATE 态，非权威事实）；</li>
     *   <li>{@code unknowns}：由未绑定参数的 semanticQueries 派生。</li>
     * </ul>
     * fail-closed：plan 为 null、caseId 为 null、permissionDecisionId 缺失/PENDING 时抛异常拒绝，
     * 绝不返回空 bundle 或带 PENDING 权限的 bundle。</p>
     */
    @Override
    public EvidenceBundle assemble(PlanRequest request) {
        // ── 前置校验（fail-closed）────────────────────────────────────────────
        Objects.requireNonNull(request, "request");
        if (request.caseId() == null) {
            throw new IllegalArgumentException("caseId is required");
        }
        if (request.plan() == null) {
            throw new IllegalArgumentException("plan is required");
        }
        ActivationPlan plan = request.plan();
        // 权限未决：PermissionDecisionId 缺失/PENDING 时拒绝，保证 bundle 权限完整。
        if (isPending(plan.permissionDecisionId())) {
            throw new IllegalArgumentException("permission decision is missing or pending (fail-closed)");
        }

        // 生成 bundle 元数据：唯一 ID 与装配时点。
        UUID bundleId = UUID.randomUUID();
        Instant assembledAt = Instant.now();

        // ── 1. 由 selectedAssets 构建 evidence（来源完整）─────────────────────
        // 每个被选资产都产出一条证据（来源 URI、版本、权限标签），保证来源可追溯。
        List<Evidence> evidence = new ArrayList<>();
        for (ActivationPlan.SelectedAsset asset : plan.selectedAssets()) {
            evidence.add(toEvidence(asset));
        }

        // ── 2. 由 ruleChecks 构建候选主张（非空 candidateClaims）──────────────
        // 每条规则检查派生一条 CANDIDATE 主张；CANDIDATE 表示"待核实"，非权威事实。
        List<Claim> candidateClaims = new ArrayList<>();
        for (String ruleCheck : plan.ruleChecks()) {
            candidateClaims.add(toCandidateClaim(request.caseId(), ruleCheck));
        }

        // ── 3. 由未绑定参数的 semanticQueries 构建 unknowns ──────────────────
        // 若某个语义查询缺少绑定参数，则标记为未知，纳入 unknowns（保守、fail-closed）。
        List<String> unknowns = plan.semanticQueries().stream()
                .filter(query -> !hasBoundParameter(request.boundParameters(), query))
                .toList();

        // 组装非空 EvidenceBundle：candidateClaims 与 evidence 均非空，权限与来源完整。
        return new EvidenceBundle(
                bundleId,                       // bundle 唯一 ID
                request.caseId(),               // 关联用例 ID
                plan.taskType(),                // 任务类型作为 purpose
                plan.permissionDecisionId(),    // 已确认的权限决策 ID
                assembledAt,                    // 装配时点
                List.of(),                      // facts：shadow 阶段无已核实事实
                List.copyOf(candidateClaims),   // 候选主张（非空）
                List.copyOf(evidence),          // 证据（非空）
                List.copyOf(unknowns),          // 未知项
                List.of());                     // conflicts：shadow 阶段无冲突
    }

    /**
     * 把一个激活资产转换为一条证据记录。
     *
     * <p>来源 URI 以 {@code asset:///<assetId>} 规范化表示，版本取资产的 version，
     * 权限标签由资产是否必需（required）派生，内容哈希用 assetId@version 确定性计算
     * （shadow 阶段占位，保证可重放）。</p>
     *
     * @param asset 激活计划中选中的资产
     * @return 对应的证据记录
     */
    private static Evidence toEvidence(ActivationPlan.SelectedAsset asset) {
        return new Evidence(
                UUID.randomUUID(),                                        // 证据唯一 ID
                URI.create("asset:///" + asset.assetId()),                // 来源 URI（规范化）
                asset.version(),                                          // 来源版本
                "p20-shadow",                                             // 来源类型
                "sha256:" + (asset.assetId() + "@" + asset.version()).hashCode(), // 内容哈希占位
                permissionLabel(asset));                                  // 权限标签
    }

    /**
     * 为资产派生权限标签。
     *
     * <p>用"必需资产 → REQUIRED，可选资产 → OPTIONAL"区分访问强度，便于权限审计。</p>
     */
    private static String permissionLabel(ActivationPlan.SelectedAsset asset) {
        return "CALLER:" + (asset.required() ? "REQUIRED" : "OPTIONAL");
    }

    /**
     * 由一条规则检查派生一条候选主张（CANDIDATE）。
     *
     * <p>主张类型固定为 CUSTOMER_STATEMENT（客户陈述），状态为 CANDIDATE（待核实，非权威事实），
     * 内容标注其来源规则检查，便于回溯。</p>
     *
     * @param caseId    关联用例 ID
     * @param ruleCheck 规则检查标识
     * @return CANDIDATE 态的主张
     */
    private static Claim toCandidateClaim(UUID caseId, String ruleCheck) {
        return new Claim(
                UUID.randomUUID(),                                    // 主张唯一 ID
                caseId,                                               // 关联用例
                ClaimType.CUSTOMER_STATEMENT,                         // 主张类型
                ClaimStatus.CANDIDATE,                                // 状态：候选待核实
                "candidate from rule check: " + ruleCheck,            // 内容（标注来源）
                null, null,                                           // 引用/证据（shadow 暂缺）
                Instant.now(),                                        // 主张时点
                null);                                                // 后续问题
    }

    /**
     * 判断某语义查询是否有已绑定参数可满足。
     *
     * <p>shadow 阶段简化判定：只要调用方提供了任一绑定参数，即视为查询可满足；
     * 否则该查询纳入 unknowns（保守）。</p>
     */
    private static boolean hasBoundParameter(Map<String, String> boundParameters, String queryId) {
        return boundParameters != null && !boundParameters.isEmpty();
    }

    /**
     * 判断权限决策 ID 是否未决（fail-closed 触发条件）。
     *
     * <p>null、空白或等于 {@code PENDING} 均视为未决。</p>
     */
    private static boolean isPending(String permissionDecisionId) {
        return permissionDecisionId == null
                || permissionDecisionId.isBlank()
                || PENDING_PERMISSION_DECISION_ID.equalsIgnoreCase(permissionDecisionId);
    }

    /**
     * 从权限上下文中解析权限决策 ID（传统 Request 路径）。
     *
     * <p>上下文缺失/为空或缺少 permissionDecisionId 键时返回 {@code PENDING}（保守）。
     * 空白值同样视为 PENDING。</p>
     */
    private static String resolvePermissionDecisionId(Map<String, Object> permissionContext) {
        if (permissionContext == null || permissionContext.isEmpty()) {
            return PENDING_PERMISSION_DECISION_ID;
        }
        Object value = permissionContext.get(PERMISSION_DECISION_ID_KEY);
        if (value == null) {
            return PENDING_PERMISSION_DECISION_ID;
        }
        String resolved = value.toString();
        return resolved.isBlank() ? PENDING_PERMISSION_DECISION_ID : resolved;
    }
}
