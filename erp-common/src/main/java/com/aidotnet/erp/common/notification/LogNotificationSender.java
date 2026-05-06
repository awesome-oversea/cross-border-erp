package com.aidotnet.erp.common.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationSender.class);

    @Override
    public void send(NotificationMessage message) {
        log.info("SEND: channel=LOG, tenant={}, to={}, subject={}, content={}",
                message.tenantId(), message.recipient(), message.subject(), message.content());
    }

    @Override
    public String channel() {
        return "LOG";
    }
}
