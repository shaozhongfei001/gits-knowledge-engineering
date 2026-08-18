package com.gien.gits.knowledge;

import java.util.List;

/**
 * 可重放激活计划（Replayable Activation Plan）领域模型，对应合同 CTR-PLAN-001
 * (specs/knowledge-architecture/schemas/activation-plan.schema.json)。
 *
 * <p>仅承载合同已定义的字段，不发明额外字段。</p>
 */
public record ActivationPlan(
        String schemaVersion,
        String planId,
        String taskId,
        String taskType,
        String routeMode,
        Versions versions,
        List<SelectedAsset> selectedAssets,
        List<String> semanticQueries,
        List<String> ruleChecks,
        List<String> skills,
        Context context,
        String permissionDecisionId,
        Trace trace) {

    public ActivationPlan {
        selectedAssets = orEmptyAssets(selectedAssets);
        semanticQueries = orEmpty(semanticQueries);
        ruleChecks = orEmpty(ruleChecks);
        skills = orEmpty(skills);
    }

    private static List<String> orEmpty(List<String> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    private static List<SelectedAsset> orEmptyAssets(List<SelectedAsset> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    public record Versions(
            String knowledgeMap,
            String routePolicy,
            String activationContract,
            String ontology) {}

    public record SelectedAsset(String assetId, String version, boolean required, Integer sequence) {}

    public record Context(Integer maxTokens, String trimPolicy) {}

    public record Trace(String planHash, String createdAt, String plannerVersion) {}
}
