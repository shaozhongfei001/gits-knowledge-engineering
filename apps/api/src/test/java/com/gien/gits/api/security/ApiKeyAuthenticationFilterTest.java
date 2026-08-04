package com.gien.gits.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ApiKeyAuthenticationFilter 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // --- api-key 已配置 (认证模式) ---

    @Test
    void validApiKey_shouldAuthenticateAndProceed() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("test-secret-key");

        when(request.getRequestURI()).thenReturn("/api/v1/engagement/journey/start");
        when(request.getHeader("X-API-KEY")).thenReturn("test-secret-key");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isInstanceOf(ApiKeyAuthenticationToken.class);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void invalidApiKey_shouldReturn401() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("test-secret-key");

        when(request.getRequestURI()).thenReturn("/api/v1/engagement/journey/start");
        when(request.getHeader("X-API-KEY")).thenReturn("wrong-key");
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(new java.io.StringWriter()));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void missingApiKey_shouldReturn401() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("test-secret-key");

        when(request.getRequestURI()).thenReturn("/api/v1/engagement/journey/start");
        when(request.getHeader("X-API-KEY")).thenReturn(null);
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(new java.io.StringWriter()));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void blankApiKey_shouldReturn401() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("test-secret-key");

        when(request.getRequestURI()).thenReturn("/api/v1/engagement/journey/start");
        when(request.getHeader("X-API-KEY")).thenReturn("   ");
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(new java.io.StringWriter()));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    // --- api-key 为空 (开发/禁用模式) ---

    @Test
    void emptyApiKey_shouldSkipAuthentication() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void nullApiKey_shouldSkipAuthentication() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // --- actuator 公开端点 ---

    @Test
    void actuatorHealth_shouldBeAccessibleWithoutApiKey() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("test-secret-key");

        when(request.getRequestURI()).thenReturn("/actuator/health");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void actuatorInfo_shouldBeAccessibleWithoutApiKey() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("test-secret-key");

        when(request.getRequestURI()).thenReturn("/actuator/info");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
}
