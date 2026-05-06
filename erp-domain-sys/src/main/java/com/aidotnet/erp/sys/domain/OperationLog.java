package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record OperationLog(
        String logId,
        String tenantId,
        String userId,
        String username,
        String module,
        String action,
        String targetObjectType,
        String targetObjectId,
        String detail,
        String ipAddress,
        String userAgent,
        String traceId,
        Instant operatedAt) {}
