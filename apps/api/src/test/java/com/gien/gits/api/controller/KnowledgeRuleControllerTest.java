package com.gien.gits.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.ontology.PolicyRule;
import com.gien.gits.ontology.ExternalEvent;
import com.gien.gits.ontology.ProductKnowledgeCard;
import com.gien.gits.api.service.KycInsightService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KnowledgeRuleController.class)
@AutoConfigureMockMvc(addFilters = false)
class KnowledgeRuleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean KycInsightService kycInsightService;

    @Test
    void testCreatePolicyRule() throws Exception {
        PolicyRule rule = new PolicyRule("RULE-1", "KYC compliance rule",
            PolicyRule.Severity.HIGH, "logic", "required output", "scope", "ref");

        mockMvc.perform(post("/api/v1/engagement/policy-rule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rule)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ruleId").value("RULE-1"))
            .andExpect(jsonPath("$.status").value("CREATED"));

        verify(kycInsightService).savePolicyRule(any(PolicyRule.class));
    }

    @Test
    void testListPolicyRules() throws Exception {
        PolicyRule r1 = new PolicyRule("RULE-1", "KYC rule", PolicyRule.Severity.HIGH, "logic1", "output1");
        PolicyRule r2 = new PolicyRule("RULE-2", "AML rule", PolicyRule.Severity.CRITICAL, "logic2", "output2");
        when(kycInsightService.getAllPolicyRules()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/v1/engagement/policy-rule"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testCreateExternalEvent() throws Exception {
        ExternalEvent event = new ExternalEvent("EVT-1", LocalDate.now(),
            ExternalEvent.SourceType.REGULATORY, "Source", "Entity", "Title", "Content",
            ExternalEvent.Confidence.HIGH, ExternalEvent.Reliability.VERIFIED,
            true, List.of("theme1"), "signal", "noGo", "evidenceRef");

        mockMvc.perform(post("/api/v1/engagement/external-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.eventId").value("EVT-1"))
            .andExpect(jsonPath("$.status").value("CREATED"));

        verify(kycInsightService).saveExternalEvent(any(ExternalEvent.class));
    }

    @Test
    void testListExternalEvents() throws Exception {
        when(kycInsightService.getRecentExternalEvents(10)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/engagement/external-event"))
            .andExpect(status().isOk());
    }

    @Test
    void testCreateProduct() throws Exception {
        ProductKnowledgeCard card = new ProductKnowledgeCard("PROD-1", "Wealth Fund",
            "Growth fund", List.of("diversification"), List.of("id required"),
            List.of("market risk"), "trigger", List.of("no mis-selling"), "source");

        mockMvc.perform(post("/api/v1/engagement/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(card)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.productId").value("PROD-1"))
            .andExpect(jsonPath("$.status").value("CREATED"));

        verify(kycInsightService).saveProduct(any(ProductKnowledgeCard.class));
    }

    @Test
    void testListProducts() throws Exception {
        when(kycInsightService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/engagement/product"))
            .andExpect(status().isOk());
    }
}
