package com.aidotnet.erp.common.notify;

import com.aidotnet.erp.common.persistence.entity.NotificationRecordEntity;
import com.aidotnet.erp.common.persistence.mapper.NotificationRecordMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PersistentNotificationService {

    private final NotificationRecordMapper mapper;

    public PersistentNotificationService(NotificationRecordMapper mapper) {
        this.mapper = mapper;
    }

    public NotificationRecord send(String tenantId, String channel, String receiver, String template,
                                   Map<String, String> variables) {
        String content = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        LocalDateTime now = LocalDateTime.now();
        NotificationRecordEntity entity = new NotificationRecordEntity();
        entity.setTenantId(tenantId);
        entity.setChannel(channel);
        entity.setReceiver(receiver);
        entity.setContent(content);
        entity.setSentAt(now);
        mapper.insert(entity);
        return new NotificationRecord(tenantId, channel, receiver, content, now.toInstant(ZoneOffset.UTC));
    }
}
