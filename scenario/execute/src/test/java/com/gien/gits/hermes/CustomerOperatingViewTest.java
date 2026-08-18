package com.gien.gits.hermes;

import com.gien.gits.engagement.CustomerOperatingView;
import com.gien.gits.engagement.CustomerOperatingView.OpportunitySignalSummary;
import com.gien.gits.engagement.CustomerOperatingView.CommitmentSummary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerOperatingViewTest {

    @Test
    void testConstruction() {
        CustomerOperatingView view = new CustomerOperatingView(
            "CUST-001", "Test Customer", "FINANCE", "LARGE",
            "GOLD", "LOW", "RM-001", "RM Name",
            List.of("known1"), List.of("partial1"), List.of("unknown1"),
            List.of(new OpportunitySignalSummary("CROSS_SELL", "signal content", BigDecimal.valueOf(0.8), "ACTIVE")),
            5, Instant.now(),
            1, "INSIGHT_ANALYSIS",
            List.of(new CommitmentSummary("FOLLOW_UP", "commitment content", "RM-001", "2026-12-31", false)),
            0, List.of());

        assertEquals("CUST-001", view.customerId());
        assertEquals("Test Customer", view.customerName());
        assertEquals("GOLD", view.customerTier());
        assertEquals("LOW", view.riskLevel());
        assertEquals("RM-001", view.rmId());
        assertEquals(1, view.knownKycItems().size());
        assertEquals(1, view.activeSignals().size());
        assertEquals(5, view.totalInteractions());
        assertTrue(view.hasActiveSignals());
        assertFalse(view.hasOpenReconciliations());
        assertTrue(view.hasKycGaps());
        assertEquals("低风险", view.riskLevelDescription());
    }

    @Test
    void testHasActiveSignals() {
        CustomerOperatingView withSignals = new CustomerOperatingView(
            "CUST-001", "Test", "IND", "SM", "GOLD", "LOW", "RM-1", "RM",
            List.of(), List.of(), List.of(),
            List.of(new OpportunitySignalSummary("TYPE", "content", BigDecimal.ONE, "ACTIVE")),
            0, null, 0, null, List.of(), 0, List.of());
        assertTrue(withSignals.hasActiveSignals());

        CustomerOperatingView noSignals = new CustomerOperatingView(
            "CUST-002", "Test", "IND", "SM", "GOLD", "LOW", "RM-1", "RM",
            List.of(), List.of(), List.of(),
            List.of(), 0, null, 0, null, List.of(), 0, List.of());
        assertFalse(noSignals.hasActiveSignals());
    }

    @Test
    void testHasKycGaps() {
        CustomerOperatingView withGaps = new CustomerOperatingView(
            "CUST-001", "Test", "IND", "SM", "GOLD", "LOW", "RM-1", "RM",
            List.of(), List.of("partial1"), List.of("unknown1"),
            List.of(), 0, null, 0, null, List.of(), 0, List.of());
        assertTrue(withGaps.hasKycGaps());

        CustomerOperatingView noGaps = new CustomerOperatingView(
            "CUST-002", "Test", "IND", "SM", "GOLD", "LOW", "RM-1", "RM",
            List.of("known1"), List.of(), List.of(),
            List.of(), 0, null, 0, null, List.of(), 0, List.of());
        assertFalse(noGaps.hasKycGaps());
    }

    @Test
    void testRiskLevelDescription() {
        CustomerOperatingView high = new CustomerOperatingView(
            "C1", "T", "I", "S", "G", "HIGH", "R", "N",
            List.of(), List.of(), List.of(), List.of(), 0, null, 0, null, List.of(), 0, List.of());
        assertEquals("高风险", high.riskLevelDescription());

        CustomerOperatingView medium = new CustomerOperatingView(
            "C2", "T", "I", "S", "G", "MEDIUM", "R", "N",
            List.of(), List.of(), List.of(), List.of(), 0, null, 0, null, List.of(), 0, List.of());
        assertEquals("中风险", medium.riskLevelDescription());

        CustomerOperatingView low = new CustomerOperatingView(
            "C3", "T", "I", "S", "G", "LOW", "R", "N",
            List.of(), List.of(), List.of(), List.of(), 0, null, 0, null, List.of(), 0, List.of());
        assertEquals("低风险", low.riskLevelDescription());
    }

    @Test
    void testEquality() {
        CustomerOperatingView v1 = new CustomerOperatingView(
            "CUST-001", "Test", "FIN", "L", "GOLD", "LOW", "RM-1", "RM",
            List.of(), List.of(), List.of(), List.of(), 0, null, 0, null, List.of(), 0, List.of());
        CustomerOperatingView v2 = new CustomerOperatingView(
            "CUST-001", "Test", "FIN", "L", "GOLD", "LOW", "RM-1", "RM",
            List.of(), List.of(), List.of(), List.of(), 0, null, 0, null, List.of(), 0, List.of());

        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
    }
}
