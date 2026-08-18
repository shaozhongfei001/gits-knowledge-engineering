package com.gien.gits.knowledge.route;

import java.util.Optional;

/**
 * 路由策略评估结果（sealed：AllowRoute 或 Deny）。
 *
 * <p>AllowRoute 携带唯一合法 route 与解释；Deny 携带决策码、原因与所依据的 contract/object ID。
 * 结果确定、可解释、无业务副作用。</p>
 */
public sealed interface RouteDecision permits RouteDecision.AllowRoute, RouteDecision.Deny {

    record AllowRoute(
            String taskType,
            String mode,
            String activationContractRef,
            String policyId,
            String policyVersion,
            String reason,
            int priority) implements RouteDecision {}

    record Deny(String decisionCode, String reason, String policyId) implements RouteDecision {}

    default Optional<AllowRoute> allow() {
        return this instanceof AllowRoute allow ? Optional.of(allow) : Optional.empty();
    }

    default boolean isAllowed() {
        return this instanceof AllowRoute;
    }
}
