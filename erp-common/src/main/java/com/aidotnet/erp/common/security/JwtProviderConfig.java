package com.aidotnet.erp.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * JwtProvider 已直接组件化注册。
 * 当前类保留原有显式工厂语义，避免误删历史实现，但不再注册为运行时配置。
 */
public class JwtProviderConfig {

    @Bean
    public JwtProvider jwtProvider(
            @Value("${erp.jwt.secret:${jwt.secret:default-secret-key-for-development-only-must-be-at-least-256-bits-long}}")
            String secret,
            @Value("${erp.jwt.expiration-seconds:${jwt.expiration-seconds:7200}}") long expirationSeconds) {
        return new JwtProvider(secret, expirationSeconds);
    }
}
