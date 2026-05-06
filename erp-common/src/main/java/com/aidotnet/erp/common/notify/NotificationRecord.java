package com.aidotnet.erp.common.notify;

import java.time.Instant;

public record NotificationRecord(String tenantId, String channel, String receiver, String content, Instant sentAt) {
}
