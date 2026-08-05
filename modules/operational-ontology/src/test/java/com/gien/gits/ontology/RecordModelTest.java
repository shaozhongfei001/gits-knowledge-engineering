package com.gien.gits.ontology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecordModelTest {

    // ── TransactionRecord ──────────────────────────────────────

    @Test
    void transactionRecordValidConstruction() {
        UUID id = UUID.randomUUID();
        TransactionRecord record = new TransactionRecord(id, "CUST-001",
                LocalDate.of(2026, 1, 15), "DEPOSIT", "Counterparty A",
                100000L, "Test deposit", "REF-1");

        assertEquals(id, record.id());
        assertEquals("CUST-001", record.customerId());
        assertEquals(LocalDate.of(2026, 1, 15), record.transactionDate());
        assertEquals(100000L, record.amountCny());
        assertNotNull(record.createdAt());
    }

    @Test
    void transactionRecordRejectsBlankCustomerId() {
        assertThrows(IllegalArgumentException.class, () -> new TransactionRecord(
                UUID.randomUUID(), "  ", LocalDate.now(), "DEPOSIT", "CP",
                100L, "desc", "ref"));
    }

    @Test
    void transactionRecordRejectsNullId() {
        assertThrows(NullPointerException.class, () -> new TransactionRecord(
                null, "CUST-001", LocalDate.now(), "DEPOSIT", "CP",
                100L, "desc", "ref"));
    }

    @Test
    void transactionRecordRejectsNullTransactionDate() {
        assertThrows(NullPointerException.class, () -> new TransactionRecord(
                UUID.randomUUID(), "CUST-001", null, "DEPOSIT", "CP",
                100L, "desc", "ref"));
    }

    // ── GroupRelationship ──────────────────────────────────────

    @Test
    void groupRelationshipValidConstruction() {
        UUID id = UUID.randomUUID();
        GroupRelationship rel = new GroupRelationship(id, "GRP-001", "E-001", "E-002",
                "控股", 60);

        assertEquals(id, rel.id());
        assertEquals("GRP-001", rel.groupId());
        assertEquals("E-001", rel.fromEntityId());
        assertEquals("E-002", rel.toEntityId());
        assertEquals(60, rel.ownershipRatio());
        assertNotNull(rel.createdAt());
    }

    @Test
    void groupRelationshipRejectsBlankGroupId() {
        assertThrows(IllegalArgumentException.class, () -> new GroupRelationship(
                UUID.randomUUID(), "", "E-001", "E-002", "type", 50));
    }

    @Test
    void groupRelationshipRejectsBlankFromEntityId() {
        assertThrows(IllegalArgumentException.class, () -> new GroupRelationship(
                UUID.randomUUID(), "GRP-001", "  ", "E-002", "type", 50));
    }

    @Test
    void groupRelationshipRejectsNullId() {
        assertThrows(NullPointerException.class, () -> new GroupRelationship(
                null, "GRP-001", "E-001", "E-002", "type", 50));
    }

    // ── BankRelationshipSnapshot ───────────────────────────────

    @Test
    void bankRelationshipSnapshotValidConstruction() {
        UUID id = UUID.randomUUID();
        BankRelationshipSnapshot snapshot = new BankRelationshipSnapshot(id, "CUST-001", "2026-01",
                500000L, 200000L, 100000L, 300000L, 150000L, 150000L,
                50000L, 20000L, 100, true, false, 80000L, 5, "HIGH", null);

        assertEquals(id, snapshot.id());
        assertEquals("CUST-001", snapshot.customerId());
        assertEquals("2026-01", snapshot.snapshotMonth());
        assertEquals(500000L, snapshot.avgDailyDepositCny());
        assertTrue(snapshot.cashManagementOpened());
        assertNotNull(snapshot.createdAt());
    }

    @Test
    void bankRelationshipSnapshotRejectsBlankCustomerId() {
        assertThrows(IllegalArgumentException.class, () -> new BankRelationshipSnapshot(
                UUID.randomUUID(), "", "2026-01", 0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0, false, false, 0L, 0, "LOW", null));
    }

    @Test
    void bankRelationshipSnapshotRejectsBlankSnapshotMonth() {
        assertThrows(IllegalArgumentException.class, () -> new BankRelationshipSnapshot(
                UUID.randomUUID(), "CUST-001", "  ", 0L, 0L, 0L, 0L, 0L, 0L,
                0L, 0L, 0, false, false, 0L, 0, "LOW", null));
    }

    // ── CreditFacility ─────────────────────────────────────────

    @Test
    void creditFacilityValidConstruction() {
        CreditFacility facility = new CreditFacility("FAC-001", "CUST-001", "Borrower A",
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1),
                5000000L, 2000000L, 3000000L, 1500000L, 300000L, 200000L,
                "Real Estate", List.of("贸易融资"), List.of("股市投资"),
                List.of("季度审计"), "reconciled", "REF-1");

        assertEquals("FAC-001", facility.facilityId());
        assertEquals("CUST-001", facility.customerId());
        assertEquals(1, facility.purposeAllowed().size());
        assertEquals(1, facility.purposeRestrictions().size());
        assertNotNull(facility.createdAt());
        assertNotNull(facility.updatedAt());
    }

    @Test
    void creditFacilityRejectsBlankFacilityId() {
        assertThrows(IllegalArgumentException.class, () -> new CreditFacility(
                "", "CUST-001", "Borrower", LocalDate.now(), LocalDate.now(),
                0L, 0L, 0L, 0L, 0L, 0L, null, null, null, null, null, null));
    }

    @Test
    void creditFacilityRejectsBlankCustomerId() {
        assertThrows(IllegalArgumentException.class, () -> new CreditFacility(
                "FAC-001", "  ", "Borrower", LocalDate.now(), LocalDate.now(),
                0L, 0L, 0L, 0L, 0L, 0L, null, null, null, null, null, null));
    }

    @Test
    void creditFacilityNullListsDefaultToEmpty() {
        CreditFacility facility = new CreditFacility("FAC-002", "CUST-001", "Borrower",
                LocalDate.now(), LocalDate.now(), 0L, 0L, 0L, 0L, 0L, 0L,
                null, null, null, null, null, null);

        assertTrue(facility.purposeAllowed().isEmpty());
        assertTrue(facility.purposeRestrictions().isEmpty());
        assertTrue(facility.covenants().isEmpty());
    }

    // ── RelationshipReport ─────────────────────────────────────

    @Test
    void relationshipReportValidConstruction() {
        UUID reportId = UUID.randomUUID();
        Instant generatedAt = Instant.now();
        RelationshipReport report = new RelationshipReport(reportId, "CASE-001", "J-001",
                RelationshipReport.ReportType.INTERNAL_RELATIONSHIP, "Report content",
                List.of("EV-1"), List.of("REC-1"), generatedAt, null);

        assertEquals(reportId, report.reportId());
        assertEquals(RelationshipReport.ReportType.INTERNAL_RELATIONSHIP, report.reportType());
        assertEquals("Report content", report.content());
        assertEquals(1, report.basedOnEvidence().size());
        assertNotNull(report.createdAt());
    }

    @Test
    void relationshipReportRejectsBlankContent() {
        assertThrows(IllegalArgumentException.class, () -> new RelationshipReport(
                UUID.randomUUID(), "CASE-001", "J-001",
                RelationshipReport.ReportType.CRM_CALL, "  ",
                List.of(), List.of(), Instant.now(), null));
    }

    @Test
    void relationshipReportRejectsNullReportType() {
        assertThrows(NullPointerException.class, () -> new RelationshipReport(
                UUID.randomUUID(), "CASE-001", "J-001",
                null, "content", List.of(), List.of(), Instant.now(), null));
    }

    @Test
    void relationshipReportNullListsDefaultToEmpty() {
        RelationshipReport report = new RelationshipReport(UUID.randomUUID(), "CASE-001", "J-001",
                RelationshipReport.ReportType.UPDATED_RELATIONSHIP, "content",
                null, null, Instant.now(), null);

        assertTrue(report.basedOnEvidence().isEmpty());
        assertTrue(report.basedOnReconciliations().isEmpty());
    }

    // ── LegalEntity ────────────────────────────────────────────

    @Test
    void legalEntityValidConstruction() {
        LegalEntity entity = new LegalEntity("E-001", "GRP-001", "Acme Corp",
                "子公司", "60%", "CUST-001", "ACTIVE", "REF-1");

        assertEquals("E-001", entity.entityId());
        assertEquals("GRP-001", entity.groupId());
        assertEquals("Acme Corp", entity.name());
        assertNotNull(entity.createdAt());
    }

    @Test
    void legalEntityRejectsBlankEntityId() {
        assertThrows(IllegalArgumentException.class, () -> new LegalEntity(
                "  ", "GRP-001", "Name", "role", "60%", "C-1", "ACTIVE", "ref"));
    }

    @Test
    void legalEntityRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new LegalEntity(
                "E-001", "GRP-001", "  ", "role", "60%", "C-1", "ACTIVE", "ref"));
    }

    // ── ProductKnowledgeCard ───────────────────────────────────

    @Test
    void productKnowledgeCardValidConstruction() {
        ProductKnowledgeCard card = new ProductKnowledgeCard("P-001", "FX对冲套件",
                "外汇风险管理产品", List.of("条件1"), List.of("材料1"),
                List.of("风险1"), "触发条件", List.of("禁止用语1"), "证据来源");

        assertEquals("P-001", card.productId());
        assertEquals("FX对冲套件", card.name());
        assertEquals(1, card.keyConditions().size());
        assertNotNull(card.createdAt());
    }

    @Test
    void productKnowledgeCardRejectsBlankProductId() {
        assertThrows(IllegalArgumentException.class, () -> new ProductKnowledgeCard(
                "", "Name", "def", List.of(), List.of(), List.of(), null, List.of(), null));
    }

    @Test
    void productKnowledgeCardRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new ProductKnowledgeCard(
                "P-001", "  ", "def", List.of(), List.of(), List.of(), null, List.of(), null));
    }

    @Test
    void productKnowledgeCardNullListsDefaultToEmpty() {
        ProductKnowledgeCard card = new ProductKnowledgeCard("P-002", "Name", "def",
                null, null, null, null, null, null);

        assertTrue(card.keyConditions().isEmpty());
        assertTrue(card.requiredMaterials().isEmpty());
        assertTrue(card.riskPoints().isEmpty());
        assertTrue(card.prohibitedPhrases().isEmpty());
    }
}
