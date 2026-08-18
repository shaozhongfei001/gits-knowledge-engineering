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

    /**
     * 解析任务对应的唯一合法 route。
     *
     * <p>fail-closed：若存在多个同优先级 rule（route 冲突/歧义），返回空以触发拒绝，
     * 不允许随机选择（P20 契约：同等优先级的多条冲突 route 不得随机选择）。</p>
     */
    public Optional<Rule> findRule(String taskType) {
        List<Rule> candidates = rules.stream()
                .filter(r -> taskType.equals(r.taskType()))
                .toList();
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        int minPriority = candidates.stream()
                .map(Rule::priority)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .min()
                .orElse(Integer.MAX_VALUE);
        List<Rule> top = candidates.stream()
                .filter(r -> (r.priority() == null ? Integer.MAX_VALUE : r.priority()) == minPriority)
                .toList();
        if (top.size() > 1) {
            // route 冲突/歧义 → fail-closed
            return Optional.empty();
        }
        return Optional.of(top.get(0));
    }
}
