package com.aidotnet.erp.common.event;

@FunctionalInterface
public interface DomainEventHandler<T extends DomainEvent> {

    void handle(T event);
}
