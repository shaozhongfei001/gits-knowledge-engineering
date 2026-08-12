package com.gien.gits.api.controller;

import com.gien.gits.ontology.AuditTraceEntry;
import com.gien.gits.action.port.AuditLogPort;
import com.gien.gits.ontology.port.AuditTraceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditTraceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditTraceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditTraceRepository auditTraceRepository;

    @MockitoBean
    private AuditLogPort auditLogPort;

    @Test
    void listAuditTrace_shouldReturnEntries() throws Exception {
        var entry = new AuditTraceEntry(
                "trace-001", "HumanGate", "gate-001", "DECIDE",
                Map.of("status", "PENDING"), Map.of("status", "APPROVED"),
                "user-001", "RM", Instant.now(), "corr-001"
        );
        when(auditTraceRepository.findAll()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/audit-trace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].traceId").value("trace-001"))
                .andExpect(jsonPath("$[0].entityType").value("HumanGate"))
                .andExpect(jsonPath("$[0].operation").value("DECIDE"));
    }

    @Test
    void listAuditTrace_byEntity_shouldReturnFiltered() throws Exception {
        when(auditTraceRepository.findByEntityTypeAndEntityId("HumanGate", "gate-001"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/audit-trace")
                        .param("entityType", "HumanGate")
                        .param("entityId", "gate-001"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
