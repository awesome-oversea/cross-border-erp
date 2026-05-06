package com.aidotnet.erp.common.notification;

import java.util.Map;

public record NotificationMessage(
        String tenantId,
        String channel,
        String recipient,
        String subject,
        String content,
        Map<String, String> extra
) {
    public NotificationMessage {
        extra = extra != null ? Map.copyOf(extra) : Map.of();
    }
}
