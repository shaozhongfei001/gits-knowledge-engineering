package com.gien.gits.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecurityConfig 集成测试 — 验证安全基线行为
 * <p>
 * 默认 profile 下 api-key 为空（开发模式），所有端点可访问。
 * API Key 认证逻辑在 ApiKeyAuthenticationFilterTest 中单元测试覆盖。
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // --- 开发模式 (api-key 为空) ---

    @Test
    void actuatorHealth_shouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }

    @Test
    void actuatorInfo_shouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/info"))
            .andExpect(status().isOk());
    }

    @Test
    void apiEndpoint_shouldBeAccessibleWhenApiKeyDisabled() throws Exception {
        // 默认 profile 下 api-key 为空，认证被跳过
        // 请求可能返回404(端点不存在)或500(缺少参数)，但不应返回401
        mockMvc.perform(get("/api/v1/engagement/journey/start"))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                if (status == 401) {
                    throw new AssertionError("Should not return 401 when api-key is disabled, got: " + status);
                }
            });
    }

    @Test
    void apiEndpoint_withValidApiKey_shouldNotReturn401() throws Exception {
        // 即使 api-key 禁用模式下，带 X-API-KEY 头也不应导致 401
        mockMvc.perform(get("/api/v1/engagement/journey/start")
                .header("X-API-KEY", "any-value"))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                if (status == 401) {
                    throw new AssertionError("Should not return 401 when api-key is disabled, got: " + status);
                }
            });
    }
}
