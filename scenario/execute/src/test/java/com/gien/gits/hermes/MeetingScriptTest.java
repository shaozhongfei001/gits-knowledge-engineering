package com.gien.gits.hermes;

import com.gien.gits.engagement.MeetingScript;
import com.gien.gits.engagement.MeetingScript.AgendaItem;
import com.gien.gits.engagement.MeetingScript.KycQuestionItem;
import com.gien.gits.engagement.MeetingScript.ProductDiscussionItem;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MeetingScriptTest {

    @Test
    void testConstruction() {
        Instant now = Instant.now();
        List<AgendaItem> agendaItems = List.of(
            new AgendaItem("Portfolio Review", 30, "Key points", "Alignment"));
        List<KycQuestionItem> kycQuestions = List.of(
            new KycQuestionItem("INCOME", "What is your annual income?", "Verify", "NUMBER"));
        List<ProductDiscussionItem> productDiscussions = List.of(
            new ProductDiscussionItem("PROD-1", "Wealth Fund", "Risk angle", List.of("diversification")));
        List<String> riskPoints = List.of("Market volatility");

        MeetingScript script = new MeetingScript(
            "SCRIPT-1", "CUST-001", "RM-001", "OC-1", "JOURNEY-1",
            "Review portfolio", "Customer interested in wealth management",
            agendaItems, kycQuestions, productDiscussions, riskPoints,
            "Summary of meeting", now);

        assertEquals("SCRIPT-1", script.scriptId());
        assertEquals("CUST-001", script.customerId());
        assertEquals("RM-001", script.rmId());
        assertEquals("OC-1", script.operatingCaseId());
        assertEquals("JOURNEY-1", script.journeyId());
        assertEquals("Review portfolio", script.meetingObjective());
        assertEquals("Customer interested in wealth management", script.previsitSummary());
        assertEquals(1, script.agendaItems().size());
        assertEquals(1, script.kycQuestions().size());
        assertEquals(1, script.productDiscussions().size());
        assertEquals(1, script.riskPoints().size());
        assertEquals("Summary of meeting", script.closingSummary());
        assertEquals(now, script.createdAt());
    }

    @Test
    void testConstruction_NullDefaults() {
        MeetingScript script = new MeetingScript(
            "SCRIPT-1", "CUST-001", "RM-001", "OC-1", "JOURNEY-1",
            "Objective", "Summary",
            null, null, null, null,
            "Closing", null);

        assertNotNull(script.agendaItems());
        assertTrue(script.agendaItems().isEmpty());
        assertNotNull(script.kycQuestions());
        assertTrue(script.kycQuestions().isEmpty());
        assertNotNull(script.productDiscussions());
        assertTrue(script.productDiscussions().isEmpty());
        assertNotNull(script.riskPoints());
        assertTrue(script.riskPoints().isEmpty());
        assertNotNull(script.createdAt());
    }

    @Test
    void testConstruction_Validation_BlankScriptId() {
        assertThrows(IllegalArgumentException.class, () ->
            new MeetingScript("", "CUST-001", "RM-001", "OC-1", "J1",
                "obj", "summary", List.of(), List.of(), List.of(), List.of(), "close", null));
    }

    @Test
    void testConstruction_Validation_BlankCustomerId() {
        assertThrows(IllegalArgumentException.class, () ->
            new MeetingScript("SCRIPT-1", "", "RM-001", "OC-1", "J1",
                "obj", "summary", List.of(), List.of(), List.of(), List.of(), "close", null));
    }

    @Test
    void testConstruction_Validation_BlankRmId() {
        assertThrows(IllegalArgumentException.class, () ->
            new MeetingScript("SCRIPT-1", "CUST-001", "", "OC-1", "J1",
                "obj", "summary", List.of(), List.of(), List.of(), List.of(), "close", null));
    }

    @Test
    void testAgendaItemRecord() {
        AgendaItem item = new AgendaItem("topic", 15, "key points", "outcome");
        assertEquals("topic", item.topic());
        assertEquals(15, item.durationMinutes());
        assertEquals("key points", item.keyPoints());
        assertEquals("outcome", item.expectedOutcome());
    }

    @Test
    void testKycQuestionItemRecord() {
        KycQuestionItem item = new KycQuestionItem("gap", "question", "purpose", "answer type");
        assertEquals("gap", item.gapArea());
        assertEquals("question", item.question());
        assertEquals("purpose", item.purpose());
        assertEquals("answer type", item.expectedAnswerType());
    }

    @Test
    void testProductDiscussionItemRecord() {
        ProductDiscussionItem item = new ProductDiscussionItem("P1", "Product", "angle", List.of("sp1"));
        assertEquals("P1", item.productId());
        assertEquals("Product", item.productName());
        assertEquals("angle", item.discussionAngle());
        assertEquals(List.of("sp1"), item.keySellingPoints());
    }

    @Test
    void testEquality() {
        Instant now = Instant.now();
        MeetingScript s1 = new MeetingScript("S1", "C1", "R1", "O1", "J1",
            "obj", "summary", List.of(), List.of(), List.of(), List.of(), "close", now);
        MeetingScript s2 = new MeetingScript("S1", "C1", "R1", "O1", "J1",
            "obj", "summary", List.of(), List.of(), List.of(), List.of(), "close", now);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
