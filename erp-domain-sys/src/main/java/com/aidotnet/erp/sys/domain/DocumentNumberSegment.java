package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record DocumentNumberSegment(
        String segmentId,
        String tenantId,
        String documentType,
        String datePart,
        long sequencePart,
        String fullNumber,
        Instant generatedAt
) {}
