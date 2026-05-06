package com.aidotnet.erp.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtProviderConfig {

    @Bean
    public JwtProvider jwtProvider(
            @Value("${erp.jwt.secret:${jwt.secret:default-secret-key-for-development-only-must-be-at-least-256-bits-long}}")
            String secret,
            @Value("${erp.jwt.expiration-seconds:${jwt.expiration-seconds:7200}}") long expirationSeconds) {
        return new JwtProvider(secret, expirationSeconds);
    }
}
