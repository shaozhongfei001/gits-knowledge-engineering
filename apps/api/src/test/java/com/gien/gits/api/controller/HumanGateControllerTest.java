package com.gien.gits.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.action.port.AuditLogPort;
import com.gien.gits.api.dto.HumanGateDecisionRequest;
import com.gien.gits.ontology.*;
import com.gien.gits.ontology.port.HumanGateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HumanGateController.class)
@AutoConfigureMockMvc(addFilters = false)
class HumanGateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HumanGateRepository humanGateRepository;

    @MockitoBean
    private AuditLogPort auditLogPort;

    @Test
    void listHumanGates_shouldReturnEmptyList() throws Exception {
        when(humanGateRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/human-gates"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void listHumanGates_byStatus_shouldReturnFiltered() throws Exception {
        var gate = createTestGate();
        when(humanGateRepository.findByStatus(HumanGateStatus.PENDING)).thenReturn(List.of(gate));

        mockMvc.perform(get("/api/v1/human-gates")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gateId").value("gate-001"))
                .andExpect(jsonPath("$[0].gateType").value("C01_PREVISIT_APPROVE"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getHumanGate_shouldReturnGate() throws Exception {
        var gate = createTestGate();
        when(humanGateRepository.findById("gate-001")).thenReturn(Optional.of(gate));

        mockMvc.perform(get("/api/v1/human-gates/gate-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gateId").value("gate-001"));
    }

    @Test
    void getHumanGate_notFound_shouldReturn404() throws Exception {
        when(humanGateRepository.findById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/human-gates/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void decideHumanGate_shouldReturnUpdatedGate() throws Exception {
        var request = new HumanGateDecisionRequest(GateDecision.APPROVE, null, "Looks good", "user-001");
        var updatedGate = createTestGate().withDecision(GateDecision.APPROVE, null, "Looks good", "user-001");

        when(humanGateRepository.decide("gate-001", GateDecision.APPROVE, null, "Looks good", "user-001"))
                .thenReturn(updatedGate);

        mockMvc.perform(post("/api/v1/human-gates/gate-001/decide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    private HumanGate createTestGate() {
        return new HumanGate(
                "gate-001",
                GateType.C01_PREVISIT_APPROVE,
                "journey-001",
                "customer-001",
                "case-001",
                HumanGateStatus.PENDING,
                "访前报告审批",
                Map.of("reportId", "report-001", "summary", "客户经营状况良好"),
                List.of("evidence-001"),
                null,
                null,
                null,
                null,
                Instant.now(),
                null
        );
    }
}
