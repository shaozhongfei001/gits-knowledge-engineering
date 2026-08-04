package com.gien.gits.hermes;

import com.gien.gits.engagement.OutreachScript;
import com.gien.gits.engagement.OutreachScript.OutreachChannel;
import com.gien.gits.engagement.OutreachScript.TalkingPoint;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OutreachScriptTest {

    @Test
    void testConstruction() {
        Instant now = Instant.now();
        List<TalkingPoint> talkingPoints = List.of(
            new TalkingPoint("Wealth Management", "Discuss portfolio", "What are your goals?", 1));
        List<String> riskReminders = List.of("AML check required");

        OutreachScript script = new OutreachScript(
            "SCRIPT-1", "CUST-001", "RM-001", "OC-1", "JOURNEY-1",
            OutreachChannel.PHONE, "Discuss investment options",
            "Good morning, this is your RM", talkingPoints, riskReminders,
            "Thank you for your time", "Schedule follow-up meeting", now);

        assertEquals("SCRIPT-1", script.scriptId());
        assertEquals("CUST-001", script.customerId());
        assertEquals("RM-001", script.rmId());
        assertEquals("OC-1", script.operatingCaseId());
        assertEquals("JOURNEY-1", script.journeyId());
        assertEquals(OutreachChannel.PHONE, script.channel());
        assertEquals("Discuss investment options", script.objective());
        assertEquals("Good morning, this is your RM", script.openingLine());
        assertEquals(1, script.talkingPoints().size());
        assertEquals(1, script.riskReminders().size());
        assertEquals("Thank you for your time", script.closingLine());
        assertEquals("Schedule follow-up meeting", script.followUpAction());
        assertEquals(now, script.createdAt());
    }

    @Test
    void testConstruction_NullDefaults() {
        OutreachScript script = new OutreachScript(
            "SCRIPT-1", "CUST-001", "RM-001", "OC-1", "JOURNEY-1",
            OutreachChannel.EMAIL, "Objective", "Opening",
            null, null, "Closing", "Follow-up", null);

        assertNotNull(script.talkingPoints());
        assertTrue(script.talkingPoints().isEmpty());
        assertNotNull(script.riskReminders());
        assertTrue(script.riskReminders().isEmpty());
        assertNotNull(script.createdAt());
    }

    @Test
    void testConstruction_Validation_BlankScriptId() {
        assertThrows(IllegalArgumentException.class, () ->
            new OutreachScript("", "CUST-001", "RM-001", "OC-1", "J1",
                OutreachChannel.PHONE, "obj", "open", List.of(), List.of(), "close", "follow", null));
    }

    @Test
    void testConstruction_Validation_BlankCustomerId() {
        assertThrows(IllegalArgumentException.class, () ->
            new OutreachScript("SCRIPT-1", "", "RM-001", "OC-1", "J1",
                OutreachChannel.PHONE, "obj", "open", List.of(), List.of(), "close", "follow", null));
    }

    @Test
    void testConstruction_Validation_BlankRmId() {
        assertThrows(IllegalArgumentException.class, () ->
            new OutreachScript("SCRIPT-1", "CUST-001", "", "OC-1", "J1",
                OutreachChannel.PHONE, "obj", "open", List.of(), List.of(), "close", "follow", null));
    }

    @Test
    void testTalkingPointRecord() {
        TalkingPoint tp = new TalkingPoint("topic", "detail", "question", 1);
        assertEquals("topic", tp.topic());
        assertEquals("detail", tp.detail());
        assertEquals("question", tp.suggestedQuestion());
        assertEquals(1, tp.priority());
    }

    @Test
    void testOutreachChannelEnum() {
        assertEquals(4, OutreachChannel.values().length);
        assertNotNull(OutreachChannel.valueOf("PHONE"));
        assertNotNull(OutreachChannel.valueOf("WECHAT"));
        assertNotNull(OutreachChannel.valueOf("EMAIL"));
        assertNotNull(OutreachChannel.valueOf("FACE_TO_FACE"));
    }

    @Test
    void testEquality() {
        Instant now = Instant.now();
        OutreachScript s1 = new OutreachScript("S1", "C1", "R1", "O1", "J1",
            OutreachChannel.PHONE, "obj", "open", List.of(), List.of(), "close", "follow", now);
        OutreachScript s2 = new OutreachScript("S1", "C1", "R1", "O1", "J1",
            OutreachChannel.PHONE, "obj", "open", List.of(), List.of(), "close", "follow", now);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
