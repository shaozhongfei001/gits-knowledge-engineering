package com.gien.gits.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API Key 认证过滤器 — 校验 X-API-KEY 请求头
 * <p>
 * 当 engagement.security.api-key 为空时跳过认证（开发模式）；
 * 配置后，除 actuator/health 和 actuator/info 外的所有请求必须携带匹配的 X-API-KEY。
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final String configuredApiKey;

    public ApiKeyAuthenticationFilter(@Value("${engagement.security.api-key:}") String configuredApiKey) {
        this.configuredApiKey = configuredApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 开发模式: api-key 为空则跳过认证
        if (!isAuthenticationEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestPath = request.getRequestURI();

        // actuator/health 和 actuator/info 公开访问
        if (isActuatorPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedApiKey = request.getHeader(API_KEY_HEADER);

        if (providedApiKey == null || providedApiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Missing X-API-KEY header\"}");
            return;
        }

        if (!configuredApiKey.equals(providedApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid API key\"}");
            return;
        }

        // 认证通过
        SecurityContextHolder.getContext().setAuthentication(new ApiKeyAuthenticationToken(providedApiKey));
        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticationEnabled() {
        return configuredApiKey != null && !configuredApiKey.isBlank();
    }

    private boolean isActuatorPublicEndpoint(String path) {
        return path.endsWith("/actuator/health") || path.endsWith("/actuator/info");
    }
}
