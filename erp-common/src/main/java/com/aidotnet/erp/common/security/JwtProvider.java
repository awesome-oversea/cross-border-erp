package com.aidotnet.erp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtProvider(
            @Value("${erp.jwt.secret:${jwt.secret:changeme-must-be-at-least-256-bits-long}}")
            String secret,
            @Value("${erp.jwt.expiration-seconds:${jwt.expiration-seconds:7200}}") long expirationSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(String tenantId, String userId, String username, Set<String> permissions) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claim("tenantId", tenantId)
                .claim("username", username)
                .claim("permissions", String.join(",", permissions))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
