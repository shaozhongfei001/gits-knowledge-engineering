package com.gien.gits.knowledge.route;

import com.gien.gits.knowledge.RoutePolicy;
import com.gien.gits.knowledge.plan.ExecutionMode;
import com.gien.gits.knowledge.port.RoutePolicyEvaluatorPort;
import com.gien.gits.knowledge.port.RoutePolicyPort;
import java.util.Objects;
import java.util.Optional;

/**
 * 默认路由策略评估器（P20 shadow slice）。
 *
 * <p>fail-closed：任务未映射 → DENY_UNMAPPED_TASK；合同引用 AC-NOT-IN-P20 → DENY_NOT_IN_P20；
 * 请求 PRODUCTION 模式 → DENY_PRODUCTION_MODE；route policy 不可解析 → DENY。
 * 结果确定、可解释、带所依据的 policy/contract ID，不产生业务副作用。</p>
 */
public final class DefaultRoutePolicyEvaluator implements RoutePolicyEvaluatorPort {

    /** 路由规则中标识“不在 P20 范围”的激活合同引用。 */
    public static final String NOT_IN_P20_REF = "AC-NOT-IN-P20";

    private final RoutePolicyPort routePolicyPort;
    private final String policyId;

    public DefaultRoutePolicyEvaluator(RoutePolicyPort routePolicyPort, String policyId) {
        this.routePolicyPort = Objects.requireNonNull(routePolicyPort, "routePolicyPort");
        this.policyId = Objects.requireNonNull(policyId, "policyId");
    }

    /**
     * 评估指定任务在给定执行模式下的唯一合法 route。
     *
     * <p>评估管线（全部 fail-closed，结果确定且可解释）：
     * <ol>
     *   <li>执行模式护栏：production 模式一律拒绝；</li>
     *   <li>任务类型校验：空白 taskType 视为未映射；</li>
     *   <li>策略加载：从 Route Policy 仓库按 policyId 加载策略；</li>
     *   <li>规则解析：在策略中解析该任务对应的唯一合法 rule（含歧义 fail-closed）；</li>
     *   <li>范围校验：rule 指向 AC-NOT-IN-P20 则拒绝（任务在 P20 之外）；</li>
     *   <li>放行：返回携带 mode/合同引用/版本/优先级/原因的 AllowRoute，供后续计划组装。</li>
     * </ol>
     * 返回结果带所依据的 policy/contract ID，不产生业务副作用。</p>
     */
    @Override
    public RouteDecision evaluate(String taskType, ExecutionMode executionMode) {
        // ── 1. 执行模式护栏：P20 仅允许 shadow ────────────────────────────────
        // 任何 production 模式请求一律拒绝（即使任务在 P20 范围内）。
        if (executionMode == ExecutionMode.PRODUCTION) {
            return new RouteDecision.Deny("DENY_PRODUCTION_MODE",
                    "production mode is not authorized for P20 shadow slice", policyId);
        }
        // 空白 taskType：无法匹配任何 route，视为未映射任务。
        if (taskType == null || taskType.isBlank()) {
            return new RouteDecision.Deny("DENY_UNMAPPED_TASK", "taskType is blank", policyId);
        }

        // ── 2. 加载路由策略 ───────────────────────────────────────────────────
        Optional<RoutePolicy> policyOpt = routePolicyPort.find(policyId);
        if (policyOpt.isEmpty()) {
            // 策略不存在或不可解析：无法做任何判定，fail-closed 拒绝。
            return new RouteDecision.Deny("DENY_UNMAPPED_TASK",
                    "route policy '" + policyId + "' not resolvable (fail-closed)", policyId);
        }
        RoutePolicy policy = policyOpt.get();

        // ── 3. 解析该任务的唯一合法 rule ──────────────────────────────────────
        // findRule 对同优先级并列的歧义 route 返回空（fail-closed），此处统一视为未映射。
        Optional<RoutePolicy.Rule> ruleOpt = policy.findRule(taskType);
        if (ruleOpt.isEmpty()) {
            return new RouteDecision.Deny("DENY_UNMAPPED_TASK",
                    "task '" + taskType + "' is unmapped by policy '" + policyId + "'", policyId);
        }
        RoutePolicy.Rule rule = ruleOpt.get();

        // ── 4. 范围校验：任务映射的合同是否在 P20 之内 ────────────────────────
        // AC-NOT-IN-P20 是路由规则中标识"不在 P20 范围"的合同引用占位符。
        if (NOT_IN_P20_REF.equals(rule.activationContractRef())) {
            return new RouteDecision.Deny("DENY_NOT_IN_P20",
                    "task '" + taskType + "' maps to contract outside P20 scope", policyId);
        }

        // ── 5. 放行：返回完整、可解释的 route 信息 ────────────────────────────
        return new RouteDecision.AllowRoute(
                taskType,                       // 任务类型
                rule.mode(),                    // 主路径模式（决定后续走 Wiki 还是 Ontology 优先）
                rule.activationContractRef(),   // 对应激活合同引用
                policyId,                       // 所依据的路由策略 ID
                policy.version(),               // 策略版本（版本溯源）
                rule.reason(),                  // 选择该 route 的原因（可解释性）
                rule.priority() == null ? Integer.MAX_VALUE : rule.priority()); // 优先级
    }
}
