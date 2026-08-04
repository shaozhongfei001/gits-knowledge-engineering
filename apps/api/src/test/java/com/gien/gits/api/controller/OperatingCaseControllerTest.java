package com.gien.gits.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.CaseType;
import com.gien.gits.ontology.OperatingCase;
import com.gien.gits.ontology.port.WritableOperatingCaseRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OperatingCaseController.class)
@AutoConfigureMockMvc(addFilters = false)
class OperatingCaseControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean WritableOperatingCaseRepository operatingCaseRepository;

    @Test
    void testCreateCase() throws Exception {
        UUID caseId = UUID.randomUUID();
        OperatingCaseController.CreateCaseRequest request = new OperatingCaseController.CreateCaseRequest(
            caseId, "CONTINUOUS_ENGAGEMENT", CaseStatus.OPEN, "Review customer engagement",
            Instant.now(), null, "RM-001");

        mockMvc.perform(post("/api/case")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.caseId").value(caseId.toString()))
            .andExpect(jsonPath("$.caseType").value("CONTINUOUS_ENGAGEMENT"))
            .andExpect(jsonPath("$.status").value("OPEN"));

        verify(operatingCaseRepository).save(any(OperatingCase.class));
    }

    @Test
    void testGetCase() throws Exception {
        UUID caseId = UUID.randomUUID();
        Instant now = Instant.now();
        OperatingCase oc = new OperatingCase(
            caseId, CaseType.CONTINUOUS_ENGAGEMENT, CaseStatus.OPEN, "Review customer engagement",
            now, null, now, "RM-001");
        when(operatingCaseRepository.findById(caseId)).thenReturn(Optional.of(oc));

        mockMvc.perform(get("/api/case/{caseId}", caseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.caseId").value(caseId.toString()))
            .andExpect(jsonPath("$.caseType").value("CONTINUOUS_ENGAGEMENT"))
            .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void testGetCaseNotFound() throws Exception {
        UUID caseId = UUID.randomUUID();
        when(operatingCaseRepository.findById(caseId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/case/{caseId}", caseId))
            .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCaseWithClaimReconciliation() throws Exception {
        UUID caseId = UUID.randomUUID();
        OperatingCaseController.CreateCaseRequest request = new OperatingCaseController.CreateCaseRequest(
            caseId, "CLAIM_RECONCILIATION", CaseStatus.OPEN, "Reconcile claims",
            Instant.now(), null, "RM-002");

        mockMvc.perform(post("/api/case")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.caseType").value("CLAIM_RECONCILIATION"));

        verify(operatingCaseRepository).save(any(OperatingCase.class));
    }
}
