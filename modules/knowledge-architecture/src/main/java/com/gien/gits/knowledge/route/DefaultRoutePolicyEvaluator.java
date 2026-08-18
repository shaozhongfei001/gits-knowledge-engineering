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

    @Override
    public RouteDecision evaluate(String taskType, ExecutionMode executionMode) {
        if (executionMode == ExecutionMode.PRODUCTION) {
            return new RouteDecision.Deny("DENY_PRODUCTION_MODE",
                    "production mode is not authorized for P20 shadow slice", policyId);
        }
        if (taskType == null || taskType.isBlank()) {
            return new RouteDecision.Deny("DENY_UNMAPPED_TASK", "taskType is blank", policyId);
        }

        Optional<RoutePolicy> policyOpt = routePolicyPort.find(policyId);
        if (policyOpt.isEmpty()) {
            return new RouteDecision.Deny("DENY_UNMAPPED_TASK",
                    "route policy '" + policyId + "' not resolvable (fail-closed)", policyId);
        }
        RoutePolicy policy = policyOpt.get();

        Optional<RoutePolicy.Rule> ruleOpt = policy.findRule(taskType);
        if (ruleOpt.isEmpty()) {
            return new RouteDecision.Deny("DENY_UNMAPPED_TASK",
                    "task '" + taskType + "' is unmapped by policy '" + policyId + "'", policyId);
        }
        RoutePolicy.Rule rule = ruleOpt.get();

        if (NOT_IN_P20_REF.equals(rule.activationContractRef())) {
            return new RouteDecision.Deny("DENY_NOT_IN_P20",
                    "task '" + taskType + "' maps to contract outside P20 scope", policyId);
        }

        return new RouteDecision.AllowRoute(
                taskType,
                rule.mode(),
                rule.activationContractRef(),
                policyId,
                policy.version(),
                rule.reason(),
                rule.priority() == null ? Integer.MAX_VALUE : rule.priority());
    }
}
