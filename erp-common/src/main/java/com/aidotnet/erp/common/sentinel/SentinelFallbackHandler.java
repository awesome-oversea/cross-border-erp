package com.aidotnet.erp.common.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.aidotnet.erp.common.api.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SentinelFallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(SentinelFallbackHandler.class);

    public static Result<Void> handleBlock(BlockException ex) {
        log.warn("Sentinel blocked: {}", ex.getRule());
        return Result.fail("RATE_LIMITED", "请求过于频繁，请稍后重试");
    }

    public static Result<Void> handleFallback(Throwable ex) {
        log.error("Sentinel fallback triggered", ex);
        return Result.fail("SERVICE_DEGRADED", "服务暂时不可用，请稍后重试");
    }
}
