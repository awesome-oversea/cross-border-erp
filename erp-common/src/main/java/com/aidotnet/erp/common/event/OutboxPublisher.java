package com.aidotnet.erp.common.event;

import com.aidotnet.erp.common.persistence.entity.OutboxEventEntity;
import com.aidotnet.erp.common.persistence.mapper.OutboxEventMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int MAX_RETRY_COUNT = 5;
    private final OutboxEventMapper outboxMapper;
    private final DomainEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxEventMapper outboxMapper, DomainEventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.outboxMapper = outboxMapper;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveToOutbox(DomainEvent event) {
        try {
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("eventId", event.eventId());
            payloadMap.put("eventType", event.eventType());
            payloadMap.put("aggregateId", event.aggregateId());
            payloadMap.put("tenantId", event.tenantId());
            payloadMap.put("traceId", event.traceId());
            payloadMap.put("occurredAt", event.occurredAt().toString());
            if (event instanceof StandardDomainEvent sde && sde.payload() != null) {
                payloadMap.put("payload", sde.payload());
            }
            String payload = objectMapper.writeValueAsString(payloadMap);

            OutboxEventEntity entity = new OutboxEventEntity();
            entity.setEventId(UUID.randomUUID().toString());
            entity.setTenantId(event.tenantId());
            entity.setTraceId(event.traceId());
            entity.setEventType(event.eventType());
            entity.setAggregateId(event.aggregateId());
            entity.setPayloadJson(payload);
            entity.setStatus("PENDING");
            entity.setOccurredAt(LocalDateTime.ofInstant(event.occurredAt(), ZoneOffset.UTC));
            entity.setCreatedAt(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
            outboxMapper.insert(entity);
            log.debug("Saved to outbox: eventId={}, type={}", entity.getEventId(), entity.getEventType());
        } catch (Exception e) {
            log.error("Failed to save event to outbox: type={}", event.eventType(), e);
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPending() {
        LambdaQueryWrapper<OutboxEventEntity> query = new LambdaQueryWrapper<OutboxEventEntity>()
                .eq(OutboxEventEntity::getStatus, "PENDING")
                .lt(OutboxEventEntity::getCreatedAt, LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1))
                .orderByAsc(OutboxEventEntity::getCreatedAt)
                .last("LIMIT 100");
        List<OutboxEventEntity> pending = outboxMapper.selectList(query);
        for (OutboxEventEntity entity : pending) {
            try {
                DomainEvent event = toDomainEvent(entity);
                eventPublisher.publish(event);
                entity.setStatus("PUBLISHED");
                outboxMapper.updateById(entity);
                log.info("Outbox published: eventId={}, type={}", entity.getEventId(), entity.getEventType());
            } catch (Exception e) {
                log.warn("Outbox publish failed, will retry: eventId={}, type={}", entity.getEventId(), entity.getEventType(), e);
                entity.setStatus("FAILED");
                outboxMapper.updateById(entity);
            }
        }
    }

    private DomainEvent toDomainEvent(OutboxEventEntity entity) {
        Map<String, Object> payload = null;
        try {
            payload = objectMapper.readValue(entity.getPayloadJson(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception ignored) {}
        Map<String, Object> eventPayload = payload != null && payload.containsKey("payload")
                ? (Map<String, Object>) payload.get("payload") : Map.of();
        return new StandardDomainEvent(
                entity.getEventId(),
                entity.getTenantId(),
                entity.getTraceId(),
                entity.getEventType(),
                entity.getAggregateId(),
                entity.getOccurredAt().toInstant(ZoneOffset.UTC),
                eventPayload
        );
    }
}
