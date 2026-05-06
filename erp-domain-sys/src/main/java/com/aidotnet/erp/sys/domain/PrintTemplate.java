package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.Map;

public record PrintTemplate(
        String templateId,
        String tenantId,
        String templateCode,
        String templateName,
        String templateType,
        String content,
        String paperSize,
        String orientation,
        Map<String, Object> variables,
        boolean enabled,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
