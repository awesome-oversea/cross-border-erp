package com.aidotnet.erp.common.security;

import com.aidotnet.erp.common.context.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);
    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public TenantContextFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantId = request.getHeader(TENANT_HEADER);
        String userId = null;
        String username = null;

        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            try {
                if (jwtProvider.isTokenValid(token)) {
                    var claims = jwtProvider.parseToken(token);
                    userId = claims.getSubject();
                    username = claims.get("username", String.class);
                    if (tenantId == null || tenantId.isBlank()) {
                        tenantId = claims.get("tenantId", String.class);
                    }
                }
            } catch (Exception e) {
                log.debug("JWT parsing failed: {}", e.getMessage());
            }
        }

        if (tenantId != null && !tenantId.isBlank()) {
            SecurityContext.set(tenantId, userId, username);
            TraceContext.setTraceId(request.getHeader("X-Trace-Id"));
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContext.clear();
            TraceContext.clear();
        }
    }
}
