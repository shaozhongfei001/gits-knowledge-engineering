package com.gien.gits.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contract conformance test for CTR-API-001 / getArchitectureStatus.
 * Asserts the response matches specs/openapi/gits-kno-api.openapi.json
 * (ArchitectureStatus schema) exactly: 4 fields, const/enum values, no extras.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ArchitectureStatusContractTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getArchitectureStatusConformsToOpenApiContract() throws Exception {
        mockMvc.perform(get("/api/v1/architecture/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andExpect(jsonPath("$.packageId").value("GITS-KNO-DEV-PACKAGE-V0.1"))
                .andExpect(jsonPath("$.state").value("DEV_PACKAGE_CANDIDATE"))
                .andExpect(jsonPath("$.productionReady").value(false))
                .andExpect(jsonPath("$.frozen").value(false))
                .andExpect(jsonPath("$.packageId").isString())
                .andExpect(jsonPath("$.state").isString())
                .andExpect(jsonPath("$.productionReady").isBoolean())
                .andExpect(jsonPath("$.frozen").isBoolean());
    }
}
