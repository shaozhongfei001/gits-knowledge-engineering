package com.gien.gits.api.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P13 G1: BusinessMetrics单元测试
 */
class BusinessMetricsTest {

    private MeterRegistry registry;
    private BusinessMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new BusinessMetrics(registry);
    }

    @Test
    @DisplayName("recordJourneyStarted递增engagement_journey_started_total")
    void recordJourneyStarted_incrementsCounter() {
        metrics.recordJourneyStarted();
        metrics.recordJourneyStarted();
        assertEquals(2.0, registry.counter("engagement_journey_started_total").count());
    }

    @Test
    @DisplayName("recordJourneyCompleted递增engagement_journey_completed_total")
    void recordJourneyCompleted_incrementsCounter() {
        metrics.recordJourneyCompleted();
        assertEquals(1.0, registry.counter("engagement_journey_completed_total").count());
    }

    @Test
    @DisplayName("recordDmnDecision递增dmn_decision_total并设置status标签")
    void recordDmnDecision_incrementsCounterWithTag() {
        metrics.recordDmnDecision("CONFLICT_REQUIRES_HUMAN_REVIEW");
        metrics.recordDmnDecision("VERIFIED_FACT");
        metrics.recordDmnDecision("CONFLICT_REQUIRES_HUMAN_REVIEW");

        assertEquals(2.0, registry.counter("dmn_decision_total", "status", "CONFLICT_REQUIRES_HUMAN_REVIEW").count());
        assertEquals(1.0, registry.counter("dmn_decision_total", "status", "VERIFIED_FACT").count());
    }

    @Test
    @DisplayName("recordLlmCall递增llm_call_total并设置mode和result标签")
    void recordLlmCall_incrementsCounterWithTags() {
        metrics.recordLlmCall("mock", "success");
        metrics.recordLlmCall("real", "error");

        assertEquals(1.0, registry.counter("llm_call_total", "mode", "mock", "result", "success").count());
        assertEquals(1.0, registry.counter("llm_call_total", "mode", "real", "result", "error").count());
    }

    @Test
    @DisplayName("recordCrmWriteback递增crm_writeback_total并设置mode和result标签")
    void recordCrmWriteback_incrementsCounterWithTags() {
        metrics.recordCrmWriteback("logging", "success");
        metrics.recordCrmWriteback("http", "failed");

        assertEquals(1.0, registry.counter("crm_writeback_total", "mode", "logging", "result", "success").count());
        assertEquals(1.0, registry.counter("crm_writeback_total", "mode", "http", "result", "failed").count());
    }

    @Test
    @DisplayName("recordClaimReconciliation递增claim_reconciliation_total并设置status标签")
    void recordClaimReconciliation_incrementsCounterWithTag() {
        metrics.recordClaimReconciliation("VERIFIED_FACT");
        metrics.recordClaimReconciliation("CANDIDATE_CLAIM");

        assertEquals(1.0, registry.counter("claim_reconciliation_total", "status", "VERIFIED_FACT").count());
        assertEquals(1.0, registry.counter("claim_reconciliation_total", "status", "CANDIDATE_CLAIM").count());
    }
}
