package com.gien.gits.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.api.dto.KycGapProfileCreatedResponse;
import com.gien.gits.api.dto.SignalConfirmResponse;
import com.gien.gits.api.dto.SignalDismissResponse;
import com.gien.gits.api.service.KycInsightService;
import com.gien.gits.action.port.AuditLogPort;
import com.gien.gits.ontology.KycGapProfile;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KycInsightController.class)
@AutoConfigureMockMvc(addFilters = false)
class KycInsightControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean KycInsightService kycInsightService;
    @MockitoBean AuditLogPort auditLogPort;

    private KycGapProfile sampleProfile() {
        return new KycGapProfile(
            "PROFILE-1", "CUST-001", LocalDate.now(),
            List.of("item1"), List.of("item2"), List.of("item3"),
            List.of(), List.of(), List.of());
    }

    @Test
    void testCreateKycGapProfile() throws Exception {
        KycGapProfile profile = sampleProfile();

        mockMvc.perform(post("/api/v1/engagement/kyc/gap-profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profile)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.profileId").value("PROFILE-1"))
            .andExpect(jsonPath("$.status").value("CREATED"));

        verify(kycInsightService).saveKycGapProfile(any(KycGapProfile.class));
    }

    @Test
    void testGetKycGapProfile() throws Exception {
        KycGapProfile profile = sampleProfile();
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/api/v1/engagement/kyc/{customerId}/gap-profile", "CUST-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profileId").value("PROFILE-1"))
            .andExpect(jsonPath("$.customerId").value("CUST-001"));
    }

    @Test
    void testGetKycGapProfileNotFound() throws Exception {
        when(kycInsightService.getKycGapProfile("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/engagement/kyc/{customerId}/gap-profile", "UNKNOWN"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testConfirmSignal() throws Exception {
        String signalId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/v1/engagement/signal/{signalId}/confirm", signalId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.signalId").value(signalId))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(kycInsightService).confirmSignal(UUID.fromString(signalId));
    }

    @Test
    void testDismissSignal() throws Exception {
        String signalId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/v1/engagement/signal/{signalId}/dismiss", signalId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.signalId").value(signalId))
            .andExpect(jsonPath("$.status").value("DISMISSED"));

        verify(kycInsightService).dismissSignal(UUID.fromString(signalId));
    }
}
