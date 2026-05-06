package com.aidotnet.erp.common.notification;

public interface NotificationSender {

    void send(NotificationMessage message);

    String channel();
}
