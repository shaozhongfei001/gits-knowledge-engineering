package com.gien.gits.knowledge;

import java.util.List;

/**
 * 资产激活合同（Activation Contract）领域模型，对应合同 CTR-ACTIVATION-001
 * (specs/knowledge-architecture/schemas/activation-contract.schema.json)。
 *
 * <p>仅承载合同已定义的字段，不发明额外字段。</p>
 */
public record ActivationContract(
        String schemaVersion,
        String contractId,
        String version,
        String taskType,
        String routeMode,
        Preconditions preconditions,
        List<Activation> activations,
        List<String> semanticQueries,
        List<String> ruleChecks,
        List<String> skills,
        Context context,
        List<String> humanGates,
        String failurePolicy) {

    public ActivationContract {
        activations = orEmptyActivations(activations);
        semanticQueries = orEmpty(semanticQueries);
        ruleChecks = orEmpty(ruleChecks);
        skills = orEmpty(skills);
        humanGates = orEmpty(humanGates);
    }

    private static List<String> orEmpty(List<String> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    private static List<Activation> orEmptyActivations(List<Activation> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    public record Preconditions(
            List<String> requiredInputs,
            List<String> requiredRoles,
            boolean permissionDecisionRequired) {

        public Preconditions {
            requiredInputs = orEmpty(requiredInputs);
            requiredRoles = orEmpty(requiredRoles);
        }

        private static List<String> orEmpty(List<String> value) {
            return value == null ? List.of() : List.copyOf(value);
        }
    }

    public record Activation(String assetId, boolean required, String purpose, Integer sequence) {}

    public record Context(Integer maxTokens, List<String> priorityOrder, String trimPolicy) {

        public Context {
            priorityOrder = orEmpty(priorityOrder);
        }

        private static List<String> orEmpty(List<String> value) {
            return value == null ? List.of() : List.copyOf(value);
        }
    }
}
