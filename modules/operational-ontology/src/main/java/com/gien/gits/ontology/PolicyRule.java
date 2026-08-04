package com.gien.gits.ontology;

import java.util.Objects;

/**
 * 政策规则 — 监管/内部政策约束
 */
public record PolicyRule(
        String ruleId,
        String name,
        Severity severity,
        String logic,
        String requiredOutput,
        String scope,
        String sourceRef) {

    /** 严重程度 — 与SQL CHECK约束对齐: CRITICAL / HIGH / MEDIUM */
    public enum Severity { CRITICAL, HIGH, MEDIUM }

    public PolicyRule {
        if (ruleId == null || ruleId.isBlank()) {
            throw new IllegalArgumentException("ruleId is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        Objects.requireNonNull(severity, "severity");
    }

    /** 兼容旧构造器（5参数版） */
    public PolicyRule(String ruleId, String name, Severity severity, String logic, String requiredOutput) {
        this(ruleId, name, severity, logic, requiredOutput, null, null);
    }
}
