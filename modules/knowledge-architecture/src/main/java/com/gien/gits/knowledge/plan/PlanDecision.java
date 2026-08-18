package com.gien.gits.knowledge.plan;

import com.gien.gits.knowledge.ActivationPlan;
import java.util.Optional;

/**
 * 激活计划决策结果（sealed：AllowPlan 或 Deny）。
 *
 * <p>Deny 携带决策码与原因，fail-closed；AllowPlan 携带已生成的可重放 {@link ActivationPlan}。</p>
 */
public sealed interface PlanDecision permits PlanDecision.AllowPlan, PlanDecision.Deny {

    record AllowPlan(ActivationPlan plan) implements PlanDecision {}

    record Deny(String decisionCode, String reason) implements PlanDecision {}

    /** 便捷访问：若为 AllowPlan 返回其计划，否则返回空。 */
    default Optional<ActivationPlan> planOpt() {
        return this instanceof AllowPlan allow ? Optional.of(allow.plan()) : Optional.empty();
    }

    default boolean isAllowed() {
        return this instanceof AllowPlan;
    }
}
