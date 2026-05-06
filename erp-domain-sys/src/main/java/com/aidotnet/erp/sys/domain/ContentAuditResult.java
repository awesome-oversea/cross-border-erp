package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.List;

public record ContentAuditResult(
        String resultId,
        String tenantId,
        String auditType,
        String sourceType,
        String sourceId,
        boolean passed,
        List<ContentAuditViolation> violations,
        String auditedBy,
        Instant auditedAt) {}
