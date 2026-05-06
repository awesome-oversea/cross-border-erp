package com.aidotnet.erp.common.event;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
