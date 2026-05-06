package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.Map;

public record NotificationTemplate(
        String templateId,
        String tenantId,
        String templateCode,
        String templateName,
        String channel,
        String subject,
        String content,
        Map<String, String> variables,
        boolean enabled,
        String description,
        Instant createdAt,
        Instant updatedAt) {}
