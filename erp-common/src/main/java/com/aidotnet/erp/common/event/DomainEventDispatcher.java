package com.aidotnet.erp.common.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DomainEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventDispatcher.class);

    private final Map<String, List<DomainEventHandler<DomainEvent>>> handlers = new ConcurrentHashMap<>();

    public void register(String eventType, DomainEventHandler<DomainEvent> handler) {
        handlers.computeIfAbsent(eventType, k -> new java.util.ArrayList<>()).add(handler);
        log.debug("Registered event handler: eventType={}, handler={}", eventType, handler.getClass().getSimpleName());
    }

    public void dispatch(String eventType, DomainEvent event) {
        List<DomainEventHandler<DomainEvent>> eventHandlers = handlers.get(eventType);
        if (eventHandlers == null || eventHandlers.isEmpty()) {
            log.debug("No handler registered for event type: {}", eventType);
            return;
        }
        for (DomainEventHandler<DomainEvent> handler : eventHandlers) {
            try {
                handler.handle(event);
            } catch (Exception e) {
                log.error("Event handler failed: eventType={}, handler={}, error={}",
                        eventType, handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    public void dispatchRaw(String eventType, Map<String, Object> message) {
        DomainEvent event = toDomainEvent(message);
        dispatch(eventType, event);
    }

    private DomainEvent toDomainEvent(Map<String, Object> message) {
        return new StandardDomainEvent(
                (String) message.get("eventId"),
                (String) message.get("tenantId"),
                (String) message.get("traceId"),
                (String) message.get("eventType"),
                (String) message.get("aggregateId"),
                message.get("occurredAt") != null ? java.time.Instant.parse((String) message.get("occurredAt")) : java.time.Instant.now(),
                message.get("payload") instanceof Map ? (Map<String, Object>) message.get("payload") : Map.of()
        );
    }
}
