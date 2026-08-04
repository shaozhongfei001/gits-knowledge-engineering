package com.gien.gits.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.engagement.PrevisitReportContent;
import com.gien.gits.engagement.QuickBattleCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testJourneyStartResponseSerialization() throws Exception {
        JourneyStartResponse response = new JourneyStartResponse(
            "JRN-001", "CUST-001", "INSIGHT_ANALYSIS", "2026-08-04T10:00:00Z");

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("JRN-001"));
        assertTrue(json.contains("CUST-001"));
        assertTrue(json.contains("INSIGHT_ANALYSIS"));

        JourneyStartResponse deserialized = objectMapper.readValue(json, JourneyStartResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void testJourneyStartResponse_NullFields() throws Exception {
        JourneyStartResponse response = new JourneyStartResponse(null, null, null, null);
        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);

        JourneyStartResponse deserialized = objectMapper.readValue(json, JourneyStartResponse.class);
        assertNull(deserialized.journeyId());
        assertNull(deserialized.customerId());
        assertNull(deserialized.phase());
        assertNull(deserialized.startedAt());
    }

    @Test
    void testJourneyCompleteResponseSerialization() throws Exception {
        JourneyCompleteResponse response = new JourneyCompleteResponse("COMPLETED", "JRN-001");

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("COMPLETED"));
        assertTrue(json.contains("JRN-001"));

        JourneyCompleteResponse deserialized = objectMapper.readValue(json, JourneyCompleteResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void testPostvisitExecutionResponseSerialization() throws Exception {
        PostvisitExecutionResponse response = new PostvisitExecutionResponse(
            "TRN-001", "ANL-001", "RPT-1", "CRM-1", 3, true);

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("TRN-001"));
        assertTrue(json.contains("ANL-001"));
        assertTrue(json.contains("RPT-1"));

        PostvisitExecutionResponse deserialized = objectMapper.readValue(json, PostvisitExecutionResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void testPrevisitExecutionResponseSerialization() throws Exception {
        PrevisitReportContent report = new PrevisitReportContent(
            "RPT-001", "CUST-001", "Test Customer", "RM-1", "Visit objective",
            new PrevisitReportContent.CustomerOverview("Finance", "Large", "VIP", 10000000L, "LOW", "Good"),
            new PrevisitReportContent.KycGapSummary(List.of("A"), List.of("B"), List.of("C"), List.of("D")),
            List.of(new PrevisitReportContent.ProductScheme("P1", "Loan", "Match", "100K", "1Y", List.of(), List.of(), List.of())),
            List.of("Q1"), List.of("R1"), "Strategy");
        QuickBattleCard card = new QuickBattleCard(
            "CARD-001", "Test Customer", "Visit objective", "VIP", "LOW",
            List.of("Point1"), List.of("Hint1"), List.of("Don't1"), "Bottom line");

        PrevisitExecutionResponse response = new PrevisitExecutionResponse(report, card);

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("RPT-001"));
        assertTrue(json.contains("CARD-001"));

        PrevisitExecutionResponse deserialized = objectMapper.readValue(json, PrevisitExecutionResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void testNewEvidenceResponseSerialization() throws Exception {
        NewEvidenceResponse response = new NewEvidenceResponse("RPT-UPDATED", "RPT-NEXT");

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("RPT-UPDATED"));
        assertTrue(json.contains("RPT-NEXT"));

        NewEvidenceResponse deserialized = objectMapper.readValue(json, NewEvidenceResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void testKycGapProfileCreatedResponseSerialization() throws Exception {
        KycGapProfileCreatedResponse response = new KycGapProfileCreatedResponse("PROFILE-1", "CREATED");

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("PROFILE-1"));
        assertTrue(json.contains("CREATED"));

        KycGapProfileCreatedResponse deserialized = objectMapper.readValue(json, KycGapProfileCreatedResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void testSignalConfirmResponseSerialization() throws Exception {
        SignalConfirmResponse response = new SignalConfirmResponse("SIG-001", "CONFIRMED");

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("SIG-001"));
        assertTrue(json.contains("CONFIRMED"));

        SignalConfirmResponse deserialized = objectMapper.readValue(json, SignalConfirmResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void testSignalDismissResponseSerialization() throws Exception {
        SignalDismissResponse response = new SignalDismissResponse("SIG-001", "DISMISSED");

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("SIG-001"));
        assertTrue(json.contains("DISMISSED"));

        SignalDismissResponse deserialized = objectMapper.readValue(json, SignalDismissResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void testCustomerCreatedResponseSerialization() throws Exception {
        CustomerCreatedResponse response = new CustomerCreatedResponse("CUST-001", "CREATED");

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("CUST-001"));
        assertTrue(json.contains("CREATED"));

        CustomerCreatedResponse deserialized = objectMapper.readValue(json, CustomerCreatedResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void testPolicyRuleCreatedResponseSerialization() throws Exception {
        PolicyRuleCreatedResponse response = new PolicyRuleCreatedResponse("RULE-1", "CREATED");

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("RULE-1"));
        assertTrue(json.contains("CREATED"));

        PolicyRuleCreatedResponse deserialized = objectMapper.readValue(json, PolicyRuleCreatedResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void testExternalEventCreatedResponseSerialization() throws Exception {
        ExternalEventCreatedResponse response = new ExternalEventCreatedResponse("EVT-1", "CREATED");

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("EVT-1"));
        assertTrue(json.contains("CREATED"));

        ExternalEventCreatedResponse deserialized = objectMapper.readValue(json, ExternalEventCreatedResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void testProductCreatedResponseSerialization() throws Exception {
        ProductCreatedResponse response = new ProductCreatedResponse("PROD-1", "CREATED");

        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("PROD-1"));
        assertTrue(json.contains("CREATED"));

        ProductCreatedResponse deserialized = objectMapper.readValue(json, ProductCreatedResponse.class);
        assertEquals(response, deserialized);
    }
}
