package com.gien.gits.engagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InteractionExtractionTest {

    @Test
    void fullConstruction() {
        InteractionExtraction ie = new InteractionExtraction(
                "OBJ-001", InteractionExtraction.ExtractionType.CLAIM,
                InteractionExtraction.ClaimType.FINANCING_NEED, "Need financing",
                "Speaker A", "EV-1",
                InteractionExtraction.ExtractionStatus.DETECTED,
                BigDecimal.valueOf(0.85), false, true, null, null);

        assertEquals("OBJ-001", ie.objectId());
        assertEquals(InteractionExtraction.ExtractionType.CLAIM, ie.type());
        assertEquals(InteractionExtraction.ClaimType.FINANCING_NEED, ie.claimType());
        assertEquals("Need financing", ie.content());
        assertEquals(BigDecimal.valueOf(0.85), ie.confidence());
        assertTrue(ie.requiresReconciliation());
    }

    @Test
    void convenienceConstruction() {
        InteractionExtraction ie = new InteractionExtraction(
                "OBJ-002", InteractionExtraction.ExtractionType.FACT_CLAIM,
                "Fact content", true, false, "EV-2",
                InteractionExtraction.ClaimType.RISK_SIGNAL, 0.72);

        assertEquals("OBJ-002", ie.objectId());
        assertEquals(InteractionExtraction.ExtractionStatus.DETECTED, ie.status());
        assertEquals(0, BigDecimal.valueOf(0.72).compareTo(ie.confidence()));
        assertTrue(ie.notFact());
    }

    @Test
    void deprecatedStringConstruction() {
        InteractionExtraction ie = new InteractionExtraction(
                "OBJ-003", InteractionExtraction.ExtractionType.COMMITMENT,
                "FINANCING_NEED", "Commitment content", "Speaker",
                "EV-3", "CANDIDATE", BigDecimal.ONE,
                false, false, null, null);

        assertEquals(InteractionExtraction.ClaimType.FINANCING_NEED, ie.claimType());
        assertEquals(InteractionExtraction.ExtractionStatus.CANDIDATE, ie.status());
    }

    @Test
    void blankObjectIdRejected() {
        assertThrows(IllegalArgumentException.class, () -> new InteractionExtraction(
                "  ", InteractionExtraction.ExtractionType.CLAIM,
                InteractionExtraction.ClaimType.FINANCING_NEED, "Content",
                null, null, InteractionExtraction.ExtractionStatus.DETECTED,
                BigDecimal.ONE, false, false, null, null));
    }

    @Test
    void blankContentRejected() {
        assertThrows(IllegalArgumentException.class, () -> new InteractionExtraction(
                "OBJ-004", InteractionExtraction.ExtractionType.CLAIM,
                InteractionExtraction.ClaimType.FINANCING_NEED, "  ",
                null, null, InteractionExtraction.ExtractionStatus.DETECTED,
                BigDecimal.ONE, false, false, null, null));
    }

    @Test
    void nullTypeRejected() {
        assertThrows(NullPointerException.class, () -> new InteractionExtraction(
                "OBJ-005", null,
                InteractionExtraction.ClaimType.FINANCING_NEED, "Content",
                null, null, InteractionExtraction.ExtractionStatus.DETECTED,
                BigDecimal.ONE, false, false, null, null));
    }

    @Test
    void extractionTypeValues() {
        assertEquals(9, InteractionExtraction.ExtractionType.values().length);
    }

    @Test
    void claimTypeValues() {
        assertEquals(8, InteractionExtraction.ClaimType.values().length);
    }

    @Test
    void extractionStatusValues() {
        assertEquals(5, InteractionExtraction.ExtractionStatus.values().length);
    }
}
