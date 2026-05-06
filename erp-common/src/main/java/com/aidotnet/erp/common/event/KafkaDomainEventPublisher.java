package com.aidotnet.erp.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(DomainEventPublisher.class)
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);
    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    public KafkaDomainEventPublisher(StreamBridge streamBridge, ObjectMapper objectMapper) {
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("eventId", event.eventId());
            message.put("eventType", event.eventType());
            message.put("aggregateId", event.aggregateId());
            message.put("tenantId", event.tenantId());
            message.put("traceId", event.traceId());
            message.put("occurredAt", event.occurredAt().toString());
            if (event instanceof StandardDomainEvent sde) {
                message.put("payload", sde.payload());
            }
            String bindingName = deriveBinding(event.eventType());
            streamBridge.send(bindingName, message);
            log.info("Published domain event: type={}, id={}, tenant={}", event.eventType(), event.eventId(), event.tenantId());
        } catch (Exception e) {
            log.error("Failed to publish domain event: type={}, id={}", event.eventType(), event.eventId(), e);
            throw new RuntimeException("Event publish failed", e);
        }
    }

    private String deriveBinding(String eventType) {
        if (eventType == null || !eventType.contains(".")) {
            return "erpKafkaOutbound-out-0";
        }
        String domain = eventType.split("\\.")[1];
        return domain + "Events-out-0";
    }
}
