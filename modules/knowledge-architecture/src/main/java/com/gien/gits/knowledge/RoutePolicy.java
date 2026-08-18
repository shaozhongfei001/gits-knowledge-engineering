package com.gien.gits.knowledge;

import java.util.List;
import java.util.Optional;

/**
 * 场景路由策略（Route Policy）领域模型，对应合同 CTR-ROUTE-001
 * (specs/knowledge-architecture/schemas/route-policy.schema.json)。
 *
 * <p>仅承载合同已定义的字段，不发明额外字段。</p>
 */
public record RoutePolicy(
        String schemaVersion,
        String policyId,
        String version,
        String defaultMode,
        String defaultDecision,
        List<Rule> rules) {

    public RoutePolicy {
        rules = orEmpty(rules);
    }

    private static List<Rule> orEmpty(List<Rule> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    public record Rule(
            Integer priority,
            String taskType,
            String mode,
            String activationContractRef,
            String reason) {}

    /** 未映射任务默认拒绝（defaultDecision = DENY_UNMAPPED_TASK 语义）。 */
    public Optional<Rule> findRule(String taskType) {
        return rules.stream()
                .filter(r -> r.taskType().equals(taskType))
                .min(java.util.Comparator.comparingInt(r -> r.priority() == null ? Integer.MAX_VALUE : r.priority()));
    }
}
