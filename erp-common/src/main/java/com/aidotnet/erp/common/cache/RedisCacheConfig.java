package com.aidotnet.erp.common.cache;

import com.aidotnet.erp.common.tenant.TenantContext;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    private static final String CACHE_KEY_PREFIX = "erp:cache:";

    @Bean
    public CacheManager erpCacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(CACHE_KEY_PREFIX)
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(java.util.Map.of(
                        "product", defaultConfig.entryTtl(Duration.ofHours(2)),
                        "inventory", defaultConfig.entryTtl(Duration.ofMinutes(5)),
                        "order", defaultConfig.entryTtl(Duration.ofMinutes(10)),
                        "supplier", defaultConfig.entryTtl(Duration.ofHours(4)),
                        "warehouse", defaultConfig.entryTtl(Duration.ofHours(4)),
                        "customer", defaultConfig.entryTtl(Duration.ofMinutes(30)),
                        "campaign", defaultConfig.entryTtl(Duration.ofMinutes(15)),
                        "shipment", defaultConfig.entryTtl(Duration.ofMinutes(10)),
                        "permission", defaultConfig.entryTtl(Duration.ofHours(1)),
                        "config", defaultConfig.entryTtl(Duration.ofHours(6))
                ))
                .transactionAware()
                .build();
    }
}
