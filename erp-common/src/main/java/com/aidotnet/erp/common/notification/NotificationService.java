package com.aidotnet.erp.common.notification;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final Map<String, NotificationSender> senderMap;

    public NotificationService(List<NotificationSender> senders) {
        this.senderMap = senders.stream()
                .collect(Collectors.toMap(NotificationSender::channel, Function.identity()));
    }

    public void send(NotificationMessage message) {
        NotificationSender sender = senderMap.get(message.channel());
        if (sender == null) {
            log.warn("No sender found for channel: {}, falling back to log", message.channel());
            logNotification(message);
            return;
        }
        sender.send(message);
    }

    private void logNotification(NotificationMessage message) {
        log.info("NOTIFICATION: tenant={}, channel={}, to={}, subject={}",
                message.tenantId(), message.channel(), message.recipient(), message.subject());
    }
}
