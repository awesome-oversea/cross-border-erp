package com.aidotnet.erp.common.message;

public record MessageRoute(String eventTypePrefix, MessageBrokerType brokerType, String bindingName) {

    public MessageRoute {
        if (eventTypePrefix == null || eventTypePrefix.isBlank()) {
            throw new IllegalArgumentException("eventTypePrefix must not be blank");
        }
        if (brokerType == null) {
            throw new IllegalArgumentException("brokerType must not be null");
        }
        if (bindingName == null || bindingName.isBlank()) {
            throw new IllegalArgumentException("bindingName must not be blank");
        }
        eventTypePrefix = eventTypePrefix.trim();
        bindingName = bindingName.trim();
    }

    public boolean matches(String eventType) {
        return eventType != null && eventType.startsWith(eventTypePrefix);
    }
}
