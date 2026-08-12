package com.gien.gits.engagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class CustomerOperatingViewTest {

    private CustomerOperatingView createDefaultView() {
        return new CustomerOperatingView(
                "CUST-001", "张三", "ENTERPRISE",
                "LARGE", "VIP", "LOW", "RM-001", "李经理",
                List.of("annual_report"), List.of("financial_statement"), List.of("tax_record"),
                List.of(new CustomerOperatingView.OpportunitySignalSummary(
                        "FINANCING_NEED", "Working capital needed", BigDecimal.valueOf(0.8), "ACTIVE")),
                5, Instant.now(),
                1, "PRE_VISIT",
                List.of(new CustomerOperatingView.CommitmentSummary(
                        "BANK_COMMITMENT", "Will follow up", "RM-001", "2026-09-01", false)),
                0,
                List.of("overdue_payment"));
    }

    @Test
    void viewConstructionWithAllFields() {
        CustomerOperatingView view = createDefaultView();

        assertEquals("CUST-001", view.customerId());
        assertEquals("张三", view.customerName());
        assertEquals("ENTERPRISE", view.industry());
        assertEquals("LARGE", view.enterpriseScale());
        assertEquals("VIP", view.customerTier());
        assertEquals("LOW", view.riskLevel());
        assertEquals("RM-001", view.rmId());
        assertEquals("李经理", view.rmName());
    }

    @Test
    void kycItemsAccessible() {
        CustomerOperatingView view = createDefaultView();

        assertEquals(1, view.knownKycItems().size());
        assertEquals(1, view.partialKycItems().size());
        assertEquals(1, view.unknownKycItems().size());
        assertEquals("annual_report", view.knownKycItems().get(0));
    }

    @Test
    void opportunitySignalsAccessible() {
        CustomerOperatingView view = createDefaultView();

        assertEquals(1, view.activeSignals().size());
        assertEquals("FINANCING_NEED", view.activeSignals().get(0).signalType());
        assertEquals(BigDecimal.valueOf(0.8), view.activeSignals().get(0).confidence());
    }

    @Test
    void commitmentSummaryAccessible() {
        CustomerOperatingView view = createDefaultView();

        assertEquals(1, view.pendingCommitments().size());
        assertEquals("BANK_COMMITMENT", view.pendingCommitments().get(0).commitmentType());
        assertFalse(view.pendingCommitments().get(0).fulfilled());
    }

    @Test
    void hasActiveSignalsReturnsTrueWhenSignalsPresent() {
        CustomerOperatingView view = createDefaultView();
        assertTrue(view.hasActiveSignals());
    }

    @Test
    void hasActiveSignalsReturnsFalseWhenEmpty() {
        CustomerOperatingView view = new CustomerOperatingView(
                "CUST-002", "李四", "RETAIL", "SMALL", "NORMAL", "MEDIUM",
                "RM-002", "王经理",
                List.of(), List.of(), List.of(),
                List.of(), 0, null, 0, null, List.of(), 0, List.of());

        assertFalse(view.hasActiveSignals());
    }

    @Test
    void hasKycGapsReturnsTrueWhenUnknownItemsPresent() {
        CustomerOperatingView view = createDefaultView();
        assertTrue(view.hasKycGaps());
    }

    @Test
    void hasOpenReconciliationsReturnsFalseWhenCountIsZero() {
        CustomerOperatingView view = createDefaultView();
        assertFalse(view.hasOpenReconciliations());
    }

    @Test
    void riskLevelDescription() {
        CustomerOperatingView view = createDefaultView();
        assertEquals("低风险", view.riskLevelDescription());

        CustomerOperatingView highRisk = new CustomerOperatingView(
                "CUST-003", "王五", "RETAIL", "MEDIUM", "NORMAL", "HIGH",
                "RM-003", "赵经理",
                List.of(), List.of(), List.of(),
                List.of(), 0, null, 0, null, List.of(), 0, List.of());
        assertEquals("高风险", highRisk.riskLevelDescription());
    }

    @Test
    void nullListsDefaultToEmpty() {
        CustomerOperatingView view = new CustomerOperatingView(
                "CUST-004", "赵六", "RETAIL", "SMALL", "NORMAL", "LOW",
                "RM-004", "孙经理",
                null, null, null,
                null, 0, null, 0, null, null, 0, null);

        assertNotNull(view.knownKycItems());
        assertNotNull(view.activeSignals());
        assertNotNull(view.pendingCommitments());
        assertTrue(view.knownKycItems().isEmpty());
        assertTrue(view.activeSignals().isEmpty());
    }
}
