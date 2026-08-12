package com.gien.gits.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.action.port.AuditLogPort;
import com.gien.gits.adapter.persistence.JdbcCrmWritebackCommandRepository;
import com.gien.gits.adapter.persistence.entity.CrmWritebackCommandEntity;
import com.gien.gits.adapter.persistence.entity.CrmWritebackCommandEntity.CrmWritebackStatus;
import com.gien.gits.api.dto.CrmWritebackDecisionRequest;
import com.gien.gits.ontology.GateDecision;
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

@WebMvcTest(CrmWritebackController.class)
@AutoConfigureMockMvc(addFilters = false)
class CrmWritebackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JdbcCrmWritebackCommandRepository repository;

    @MockitoBean
    private AuditLogPort auditLogPort;

    @Test
    void listCrmWritebackCommands_shouldReturnEmptyList() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/crm/writeback-commands"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void listCrmWritebackCommands_byStatus_shouldReturnFiltered() throws Exception {
        var cmd = createTestCommand();
        when(repository.findByStatus("PENDING")).thenReturn(List.of(cmd));

        mockMvc.perform(get("/api/v1/crm/writeback-commands")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].commandId").value("crm-cmd-001"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void listCrmWritebackCommands_byJourneyId_shouldReturnFiltered() throws Exception {
        var cmd = createTestCommand();
        when(repository.findByJourneyId("jny-001")).thenReturn(List.of(cmd));

        mockMvc.perform(get("/api/v1/crm/writeback-commands")
                        .param("journeyId", "jny-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].commandId").value("crm-cmd-001"));
    }

    @Test
    void listCrmWritebackCommands_byCustomerId_shouldReturnFiltered() throws Exception {
        var cmd = createTestCommand();
        when(repository.findByCustomerId("CUST-001")).thenReturn(List.of(cmd));

        mockMvc.perform(get("/api/v1/crm/writeback-commands")
                        .param("customerId", "CUST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].commandId").value("crm-cmd-001"));
    }

    @Test
    void getCrmWritebackCommand_shouldReturnCommand() throws Exception {
        var cmd = createTestCommand();
        when(repository.findById("crm-cmd-001")).thenReturn(Optional.of(cmd));

        mockMvc.perform(get("/api/v1/crm/writeback-commands/crm-cmd-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commandId").value("crm-cmd-001"))
                .andExpect(jsonPath("$.operation").value("UPDATE"))
                .andExpect(jsonPath("$.targetEntity").value("CustomerProfile"));
    }

    @Test
    void getCrmWritebackCommand_notFound_shouldReturn404() throws Exception {
        when(repository.findById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/crm/writeback-commands/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void decideCrmWritebackCommand_shouldReturnUpdatedCommand() throws Exception {
        var request = new CrmWritebackDecisionRequest(GateDecision.APPROVE, null, "确认写回", "user-001");
        var updated = createTestCommand().withDecision(GateDecision.APPROVE, null, "确认写回", "user-001");

        when(repository.decide("crm-cmd-001", GateDecision.APPROVE, null, "确认写回", "user-001"))
                .thenReturn(updated);

        mockMvc.perform(post("/api/v1/crm/writeback-commands/crm-cmd-001/decide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    private CrmWritebackCommandEntity createTestCommand() {
        return new CrmWritebackCommandEntity(
                "crm-cmd-001", "jny-001", "CUST-001", "case-001",
                "UPDATE", "CustomerProfile",
                Map.of("operatingStatus", "EXPANDING"),
                CrmWritebackStatus.PENDING,
                true, null, null, null, null,
                Instant.now(), null, null, null
        );
    }
}
