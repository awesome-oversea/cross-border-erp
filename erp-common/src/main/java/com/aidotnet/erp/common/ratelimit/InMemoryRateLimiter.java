package com.aidotnet.erp.common.ratelimit;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.exception.ErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class InMemoryRateLimiter {

    private final Map<String, Deque<Instant>> requests = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final Duration window;

    public InMemoryRateLimiter(int maxRequests, Duration window) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be positive");
        }
        if (window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive");
        }
        this.maxRequests = maxRequests;
        this.window = window;
    }

    public void check(String scope, String actor) {
        String key = scope + ":" + actor;
        Instant now = Instant.now();
        Instant min = now.minus(window);
        Deque<Instant> deque = requests.computeIfAbsent(key, ignored -> new ConcurrentLinkedDeque<>());
        while (!deque.isEmpty() && deque.peekFirst().isBefore(min)) {
            deque.pollFirst();
        }
        if (deque.size() >= maxRequests) {
            throw new BizException(ErrorCode.RATE_LIMITED, "request rate limit exceeded");
        }
        deque.addLast(now);
    }
}
