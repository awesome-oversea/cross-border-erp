package com.aidotnet.erp.common.notify;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MockNotificationService {

    private final List<NotificationRecord> records = new ArrayList<>();

    public NotificationRecord send(String tenantId, String channel, String receiver, String template,
                                   Map<String, String> variables) {
        String content = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        NotificationRecord record = new NotificationRecord(tenantId, channel, receiver, content, Instant.now());
        records.add(record);
        return record;
    }

    public List<NotificationRecord> records() {
        return List.copyOf(records);
    }
}
