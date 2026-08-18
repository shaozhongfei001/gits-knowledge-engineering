package com.gien.gits.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RoutePolicyTest {

    @Test
    void findRulePicksLowestPriorityForTaskType() {
        RoutePolicy policy = new RoutePolicy(
                "1.0.0", "RP-CORP-RM-001", "0.1.0", "MAP_FIRST", "DENY_UNMAPPED_TASK",
                List.of(
                        new RoutePolicy.Rule(40, "REPORT_GENERATION", "MAP_FIRST", "AC-NOT-IN-P20", "r1"),
                        new RoutePolicy.Rule(10, "FACT_RECONCILIATION_30M", "ONTOLOGY_FIRST", "AC-FACT-001", "r2")));

        assertTrue(policy.findRule("FACT_RECONCILIATION_30M").isPresent());
        assertEquals("ONTOLOGY_FIRST", policy.findRule("FACT_RECONCILIATION_30M").orElseThrow().mode());
    }

    @Test
    void findRuleIsEmptyForUnmappedTask() {
        RoutePolicy policy = new RoutePolicy(
                "1.0.0", "RP-CORP-RM-001", "0.1.0", "MAP_FIRST", "DENY_UNMAPPED_TASK", List.of());

        assertTrue(policy.findRule("UNKNOWN_TASK").isEmpty());
    }

    @Test
    void findRuleRejectsEqualPriorityAmbiguityFailClosed() {
        // 两个同优先级 rule 指向同一 taskType → 冲突/歧义，fail-closed 返回空
        RoutePolicy policy = new RoutePolicy(
                "1.0.0", "RP-CORP-RM-001", "0.1.0", "MAP_FIRST", "DENY_UNMAPPED_TASK",
                List.of(
                        new RoutePolicy.Rule(10, "FACT_RECONCILIATION_30M", "ONTOLOGY_FIRST", "AC-FACT-001", "r1"),
                        new RoutePolicy.Rule(10, "FACT_RECONCILIATION_30M", "MAP_FIRST", "AC-FACT-002", "r2")));

        assertTrue(policy.findRule("FACT_RECONCILIATION_30M").isEmpty(),
                "ambiguous equal-priority routes must fail closed");
    }

    @Test
    void findRulePrefersLowestPriorityUniqueRule() {
        RoutePolicy policy = new RoutePolicy(
                "1.0.0", "RP-CORP-RM-001", "0.1.0", "MAP_FIRST", "DENY_UNMAPPED_TASK",
                List.of(
                        new RoutePolicy.Rule(40, "REPORT_GENERATION", "MAP_FIRST", "AC-R1", "r1"),
                        new RoutePolicy.Rule(10, "REPORT_GENERATION", "ONTOLOGY_FIRST", "AC-R2", "r2")));

        assertTrue(policy.findRule("REPORT_GENERATION").isPresent());
        assertEquals("ONTOLOGY_FIRST", policy.findRule("REPORT_GENERATION").orElseThrow().mode());
    }
}
