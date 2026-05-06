package com.aidotnet.erp.common.security;

import com.aidotnet.erp.common.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            if (jwtProvider.isTokenValid(token)) {
                Claims claims = jwtProvider.parseToken(token);
                String userId = claims.getSubject();
                String tenantId = claims.get("tenantId", String.class);
                String username = claims.get("username", String.class);
                String permsStr = claims.get("permissions", String.class);
                Set<SimpleGrantedAuthority> authorities = permsStr != null && !permsStr.isBlank()
                        ? Set.of(permsStr.split(",")).stream()
                                .map(p -> new SimpleGrantedAuthority("ROLE_" + p))
                                .collect(Collectors.toSet())
                        : Set.of();
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                auth.setDetails(new ErpUserDetails(tenantId, userId, username));
                SecurityContextHolder.getContext().setAuthentication(auth);
                TenantContext.setTenantId(tenantId);
            }
        }
        filterChain.doFilter(request, response);
    }
}
