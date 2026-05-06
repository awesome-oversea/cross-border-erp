package com.aidotnet.erp.common.idempotency;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.exception.ErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class IdempotencyService {

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final Duration ttl;

    public IdempotencyService() {
        this(Duration.ofMinutes(10));
    }

    public IdempotencyService(Duration ttl) {
        this.ttl = ttl;
    }

    public <T> T execute(String tenantId, String idempotencyKey, String fingerprint, Supplier<T> action) {
        String key = tenantId + ":" + idempotencyKey;
        Instant now = Instant.now();
        Entry existing = entries.get(key);
        if (existing != null && existing.expiresAt().isAfter(now)) {
            if (!Objects.equals(existing.fingerprint(), fingerprint)) {
                throw new BizException(ErrorCode.IDEMPOTENCY_CONFLICT, "idempotency key was used by a different request");
            }
            @SuppressWarnings("unchecked")
            T result = (T) existing.result();
            return result;
        }
        T result = action.get();
        entries.put(key, new Entry(fingerprint, result, now.plus(ttl)));
        return result;
    }

    public int size() {
        return entries.size();
    }

    record Entry(String fingerprint, Object result, Instant expiresAt) {
    }
}
