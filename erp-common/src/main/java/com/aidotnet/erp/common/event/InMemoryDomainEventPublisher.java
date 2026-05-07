package com.aidotnet.erp.common.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("test")
public class InMemoryDomainEventPublisher implements DomainEventPublisher {

    private final DomainEventDispatcher dispatcher;
    private final Map<String, List<DomainEventHandler<DomainEvent>>> handlers = new ConcurrentHashMap<>();
    private final List<DomainEvent> publishedEvents = new ArrayList<>();

    public InMemoryDomainEventPublisher() {
        this(null);
    }

    @Autowired
    public InMemoryDomainEventPublisher(DomainEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public synchronized void publish(DomainEvent event) {
        publishedEvents.add(event);
        if (dispatcher != null) {
            dispatcher.dispatch(event.eventType(), event);
        }
        handlers.getOrDefault(event.eventType(), List.of()).forEach(handler -> handler.handle(event));
    }

    public synchronized void subscribe(String eventType, DomainEventHandler<DomainEvent> handler) {
        handlers.computeIfAbsent(eventType, key -> new ArrayList<>()).add(handler);
    }

    public synchronized List<DomainEvent> publishedEvents() {
        return List.copyOf(publishedEvents);
    }
}
