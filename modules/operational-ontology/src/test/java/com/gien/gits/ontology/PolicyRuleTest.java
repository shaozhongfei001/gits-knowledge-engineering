package com.gien.gits.ontology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PolicyRuleTest {

    @Test
    void validConstructionWithAllFields() {
        PolicyRule rule = new PolicyRule("R-001", "KYC验证规则", PolicyRule.Severity.CRITICAL,
                "logic", "output", "scope", "ref");

        assertEquals("R-001", rule.ruleId());
        assertEquals("KYC验证规则", rule.name());
        assertEquals(PolicyRule.Severity.CRITICAL, rule.severity());
        assertEquals("logic", rule.logic());
        assertEquals("output", rule.requiredOutput());
        assertEquals("scope", rule.scope());
        assertEquals("ref", rule.sourceRef());
    }

    @Test
    void fiveArgConstructorSetsScopeAndSourceRefToNull() {
        PolicyRule rule = new PolicyRule("R-002", "风控规则", PolicyRule.Severity.HIGH,
                "logic", "output");

        assertEquals("R-002", rule.ruleId());
        assertEquals(PolicyRule.Severity.HIGH, rule.severity());
        assertEquals(null, rule.scope());
        assertEquals(null, rule.sourceRef());
    }

    @Test
    void blankRuleIdRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PolicyRule(
                "", "name", PolicyRule.Severity.MEDIUM, "logic", "output"));
    }

    @Test
    void blankNameRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PolicyRule(
                "R-003", "  ", PolicyRule.Severity.MEDIUM, "logic", "output"));
    }

    @Test
    void nullSeverityRejected() {
        assertThrows(NullPointerException.class, () -> new PolicyRule(
                "R-004", "name", null, "logic", "output"));
    }

    @Test
    void severityValues() {
        assertEquals(3, PolicyRule.Severity.values().length);
        assertEquals(PolicyRule.Severity.CRITICAL, PolicyRule.Severity.valueOf("CRITICAL"));
        assertEquals(PolicyRule.Severity.HIGH, PolicyRule.Severity.valueOf("HIGH"));
        assertEquals(PolicyRule.Severity.MEDIUM, PolicyRule.Severity.valueOf("MEDIUM"));
    }
}
