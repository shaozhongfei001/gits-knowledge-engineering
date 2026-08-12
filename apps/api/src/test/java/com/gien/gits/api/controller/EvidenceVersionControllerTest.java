package com.gien.gits.api.controller;

import com.gien.gits.ontology.domain.EvidenceVersionLink;
import com.gien.gits.action.port.AuditLogPort;
import com.gien.gits.ontology.port.EvidenceVersionLinkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EvidenceVersionController.class)
@AutoConfigureMockMvc(addFilters = false)
class EvidenceVersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvidenceVersionLinkRepository evidenceVersionLinkRepository;

    @MockitoBean
    private AuditLogPort auditLogPort;

    @Test
    void listEvidenceVersions_shouldReturnVersionChain() throws Exception {
        var link = new EvidenceVersionLink(
                "link-001", "evidence-001", null, "link-002",
                1, "CREATE", "初始创建", "user-001", Instant.now()
        );
        when(evidenceVersionLinkRepository.findVersionChain("evidence-001"))
                .thenReturn(List.of(link));

        mockMvc.perform(get("/api/v1/evidences/evidence-001/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].versionId").value("link-001"))
                .andExpect(jsonPath("$[0].evidenceId").value("evidence-001"))
                .andExpect(jsonPath("$[0].version").value(1))
                .andExpect(jsonPath("$[0].changeDescription").value("初始创建"));
    }

    @Test
    void listEvidenceVersions_emptyChain_shouldReturnEmptyArray() throws Exception {
        when(evidenceVersionLinkRepository.findVersionChain("nonexistent"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/evidences/nonexistent/versions"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void listEvidenceVersions_multipleVersions_shouldReturnOrdered() throws Exception {
        var link1 = new EvidenceVersionLink(
                "link-001", "evidence-001", null, "link-002",
                1, "CREATE", "初始创建", "user-001", Instant.now()
        );
        var link2 = new EvidenceVersionLink(
                "link-002", "evidence-001", "link-001", "link-003",
                2, "UPDATE", "补充附件", "user-002", Instant.now()
        );
        when(evidenceVersionLinkRepository.findVersionChain("evidence-001"))
                .thenReturn(List.of(link1, link2));

        mockMvc.perform(get("/api/v1/evidences/evidence-001/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].version").value(1))
                .andExpect(jsonPath("$[1].version").value(2))
                .andExpect(jsonPath("$[1].previousVersionId").value("link-001"));
    }
}
