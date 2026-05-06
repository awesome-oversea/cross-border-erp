package com.aidotnet.erp.common.message;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.exception.ErrorCode;
import com.aidotnet.erp.common.persistence.entity.OutboxEventEntity;
import com.aidotnet.erp.common.persistence.mapper.OutboxEventMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnBean(StreamBridge.class)
@EnableConfigurationProperties(PlatformMessageProperties.class)
public class StreamDomainEventPublisher implements DomainEventPublisher {

    private final StreamBridge streamBridge;
    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapper objectMapper;
    private final PlatformMessageProperties properties;

    public StreamDomainEventPublisher(StreamBridge streamBridge, OutboxEventMapper outboxEventMapper,
                                      ObjectMapper objectMapper, PlatformMessageProperties properties) {
        this.streamBridge = streamBridge;
        this.outboxEventMapper = outboxEventMapper;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void publish(DomainEvent event) {
        String payloadJson = writePayload(event);
        appendOutbox(event, payloadJson);
        String bindingName = properties.bindingFor(event.eventType());
        Message<String> message = MessageBuilder.withPayload(payloadJson)
                .setHeader("eventId", event.eventId())
                .setHeader("tenantId", event.tenantId())
                .setHeader("traceId", event.traceId())
                .setHeader("eventType", event.eventType())
                .setHeader("aggregateId", event.aggregateId())
                .build();
        if (!streamBridge.send(bindingName, message)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "failed to publish domain event");
        }
    }

    private void appendOutbox(DomainEvent event, String payloadJson) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId(event.eventId());
        entity.setTenantId(event.tenantId());
        entity.setTraceId(event.traceId());
        entity.setEventType(event.eventType());
        entity.setAggregateId(event.aggregateId());
        entity.setPayloadJson(payloadJson);
        entity.setStatus("PUBLISHED");
        entity.setOccurredAt(LocalDateTime.ofInstant(event.occurredAt(), ZoneOffset.UTC));
        entity.setCreatedAt(LocalDateTime.now());
        outboxEventMapper.insert(entity);
    }

    private String writePayload(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "failed to serialize domain event");
        }
    }
}
