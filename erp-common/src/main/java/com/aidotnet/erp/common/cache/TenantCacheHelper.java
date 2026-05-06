package com.aidotnet.erp.common.cache;

import com.aidotnet.erp.common.tenant.TenantContext;
import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TenantCacheHelper {

    private final RedisCacheService redisCacheService;

    public TenantCacheHelper(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    public String buildKey(String module, String suffix) {
        String tenantId = TenantContext.getTenantId();
        return redisCacheService.tenantKey(tenantId != null ? tenantId : "default", module, suffix);
    }

    public void put(String module, String suffix, String value, Duration ttl) {
        redisCacheService.put(buildKey(module, suffix), value, ttl);
    }

    public void put(String module, String suffix, String value) {
        redisCacheService.put(buildKey(module, suffix), value);
    }

    public Optional<String> get(String module, String suffix) {
        return redisCacheService.get(buildKey(module, suffix));
    }

    public void evict(String module, String suffix) {
        redisCacheService.delete(buildKey(module, suffix));
    }

    public boolean tryLock(String lockKey, Duration ttl) {
        String tenantId = TenantContext.getTenantId();
        String fullKey = "erp:lock:" + (tenantId != null ? tenantId : "default") + ":" + lockKey;
        return redisCacheService.setIfAbsent(fullKey, "1", ttl);
    }

    public void unlock(String lockKey) {
        String tenantId = TenantContext.getTenantId();
        String fullKey = "erp:lock:" + (tenantId != null ? tenantId : "default") + ":" + lockKey;
        redisCacheService.delete(fullKey);
    }
}
