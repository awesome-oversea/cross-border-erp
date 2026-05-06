package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.Map;

public record NotificationSetting(
        String settingId,
        String tenantId,
        String channel,
        String channelName,
        Map<String, Object> rules,
        boolean enabled,
        String description,
        Instant createdAt,
        Instant updatedAt) {}
