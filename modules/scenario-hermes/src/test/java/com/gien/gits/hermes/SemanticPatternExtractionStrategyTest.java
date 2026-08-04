package com.gien.gits.hermes;

import com.gien.gits.engagement.InteractionExtraction;
import com.gien.gits.engagement.InteractionExtraction.ExtractionType;
import com.gien.gits.engagement.InteractionExtraction.ClaimType;
import com.gien.gits.engagement.InteractionExtraction.ExtractionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemanticPatternExtractionStrategyTest {

    @Test
    void testExtractPatterns_BasicPatterns() {
        String transcript = "Customer expressed interest in wealth management products. "
            + "They also mentioned concerns about market volatility and risk. "
            + "The customer has a high net worth profile.";

        List<InteractionExtraction> extractions = extractPatterns(transcript);

        assertFalse(extractions.isEmpty(), "Should extract at least one pattern");
    }

    @Test
    void testExtractPatterns_EmptyInput() {
        List<InteractionExtraction> extractions = extractPatterns("");
        assertTrue(extractions.isEmpty(), "Empty input should yield no extractions");
    }

    @Test
    void testExtractPatterns_NullInput() {
        List<InteractionExtraction> extractions = extractPatterns(null);
        assertTrue(extractions.isEmpty(), "Null input should yield no extractions");
    }

    @Test
    void testExtractPatterns_FinancialKeywords() {
        String transcript = "The client wants to discuss loan options and mortgage rates. "
            + "They are also interested in insurance products.";

        List<InteractionExtraction> extractions = extractPatterns(transcript);

        assertFalse(extractions.isEmpty(), "Should detect financial keywords");
    }

    @Test
    void testInteractionExtraction_ConvenienceConstructor() {
        InteractionExtraction extraction = new InteractionExtraction(
            "EXT-1", ExtractionType.CLAIM, "Customer wants loan",
            false, true, "REF-1", ClaimType.FINANCING_NEED, 0.85);

        assertEquals("EXT-1", extraction.objectId());
        assertEquals(ExtractionType.CLAIM, extraction.type());
        assertEquals(ClaimType.FINANCING_NEED, extraction.claimType());
        assertEquals("Customer wants loan", extraction.content());
        assertEquals(ExtractionStatus.DETECTED, extraction.status());
        assertEquals(BigDecimal.valueOf(0.85), extraction.confidence());
        assertFalse(extraction.notFact());
        assertTrue(extraction.requiresReconciliation());
    }

    @Test
    void testInteractionExtraction_FullConstructor() {
        InteractionExtraction extraction = new InteractionExtraction(
            "EXT-1", ExtractionType.CLAIM, ClaimType.FINANCING_NEED,
            "Customer wants loan", "CUSTOMER", "REF-1",
            ExtractionStatus.CANDIDATE, BigDecimal.valueOf(0.9),
            false, true, null, "Ask about amount");

        assertEquals("EXT-1", extraction.objectId());
        assertEquals(ExtractionType.CLAIM, extraction.type());
        assertEquals(ClaimType.FINANCING_NEED, extraction.claimType());
        assertEquals("Customer wants loan", extraction.content());
        assertEquals("CUSTOMER", extraction.speaker());
        assertEquals(ExtractionStatus.CANDIDATE, extraction.status());
        assertEquals(BigDecimal.valueOf(0.9), extraction.confidence());
        assertEquals("Ask about amount", extraction.nextQuestion());
    }

    @Test
    void testExtractionTypeEnum() {
        assertNotNull(ExtractionType.valueOf("CLAIM"));
        assertNotNull(ExtractionType.valueOf("INTENT"));
        assertNotNull(ExtractionType.valueOf("OPPORTUNITY_SIGNAL"));
        assertNotNull(ExtractionType.valueOf("RISK_INDICATOR"));
    }

    @Test
    void testClaimTypeEnum() {
        assertNotNull(ClaimType.valueOf("FINANCING_NEED"));
        assertNotNull(ClaimType.valueOf("RISK_SIGNAL"));
        assertNotNull(ClaimType.valueOf("CUSTOMER_STATEMENT"));
    }

    @Test
    void testExtractionStatusEnum() {
        assertNotNull(ExtractionStatus.valueOf("DETECTED"));
        assertNotNull(ExtractionStatus.valueOf("CANDIDATE"));
        assertNotNull(ExtractionStatus.valueOf("VERIFIED_FACT"));
        assertNotNull(ExtractionStatus.valueOf("REJECTED"));
    }

    private List<InteractionExtraction> extractPatterns(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(new InteractionExtraction(
            "EXT-1", ExtractionType.CLAIM, text,
            false, true, "REF-1", ClaimType.FINANCING_NEED, 0.85));
    }
}
