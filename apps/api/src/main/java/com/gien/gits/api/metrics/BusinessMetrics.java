package com.gien.gits.api.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * P13 G1: 可观测性 — 自定义业务指标
 */
@Component
public class BusinessMetrics {

    private final MeterRegistry registry;

    public BusinessMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordJourneyStarted() {
        registry.counter("engagement_journey_started_total").increment();
    }

    public void recordJourneyCompleted() {
        registry.counter("engagement_journey_completed_total").increment();
    }

    public void recordDmnDecision(String status) {
        registry.counter("dmn_decision_total", "status", status).increment();
    }

    public void recordLlmCall(String mode, String result) {
        registry.counter("llm_call_total", "mode", mode, "result", result).increment();
    }

    public void recordCrmWriteback(String mode, String result) {
        registry.counter("crm_writeback_total", "mode", mode, "result", result).increment();
    }

    public void recordClaimReconciliation(String status) {
        registry.counter("claim_reconciliation_total", "status", status).increment();
    }
}
