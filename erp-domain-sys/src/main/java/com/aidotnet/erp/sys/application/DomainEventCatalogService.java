package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.DomainEventCatalog;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DomainEventCatalogService {

    private final SysExtStore extStore;

    public DomainEventCatalogService(SysExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public DomainEventCatalog registerEvent(String tenantId, RegisterEventCommand command) {
        extStore.findDomainEventCatalogByCode(tenantId, command.eventCode())
                .ifPresent(existing -> { throw new BizException("EVENT_CODE_DUPLICATED", "事件编码已存在"); });
        Instant now = Instant.now();
        DomainEventCatalog catalog = new DomainEventCatalog(
                UUID.randomUUID().toString(), tenantId, command.eventCode(), command.eventName(),
                command.domain(), command.aggregateType(), command.eventType(), command.description(),
                command.payloadSchema(), command.subscribers(), "1.0", true, now, now);
        return extStore.saveDomainEventCatalog(catalog);
    }

    @Transactional
    public DomainEventCatalog updateEvent(String tenantId, String eventId, UpdateEventCommand command) {
        DomainEventCatalog existing = getEvent(tenantId, eventId);
        Instant now = Instant.now();
        DomainEventCatalog updated = new DomainEventCatalog(
                existing.eventId(), existing.tenantId(), existing.eventCode(),
                command.eventName() != null ? command.eventName() : existing.eventName(),
                existing.domain(), existing.aggregateType(), existing.eventType(),
                command.description() != null ? command.description() : existing.description(),
                command.payloadSchema() != null ? command.payloadSchema() : existing.payloadSchema(),
                command.subscribers() != null ? command.subscribers() : existing.subscribers(),
                existing.version(), existing.enabled(), existing.createdAt(), now);
        return extStore.saveDomainEventCatalog(updated);
    }

    @Transactional
    public DomainEventCatalog subscribe(String tenantId, String eventCode, String subscriber) {
        DomainEventCatalog event = extStore.findDomainEventCatalogByCode(tenantId, eventCode)
                .orElseThrow(() -> new BizException("EVENT_NOT_FOUND", "事件不存在"));
        List<String> subscribers = new java.util.ArrayList<>(event.subscribers());
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
        Instant now = Instant.now();
        DomainEventCatalog updated = new DomainEventCatalog(
                event.eventId(), event.tenantId(), event.eventCode(), event.eventName(),
                event.domain(), event.aggregateType(), event.eventType(), event.description(),
                event.payloadSchema(), subscribers, event.version(), event.enabled(), event.createdAt(), now);
        return extStore.saveDomainEventCatalog(updated);
    }

    @Transactional
    public DomainEventCatalog unsubscribe(String tenantId, String eventCode, String subscriber) {
        DomainEventCatalog event = extStore.findDomainEventCatalogByCode(tenantId, eventCode)
                .orElseThrow(() -> new BizException("EVENT_NOT_FOUND", "事件不存在"));
        List<String> subscribers = new java.util.ArrayList<>(event.subscribers());
        subscribers.remove(subscriber);
        Instant now = Instant.now();
        DomainEventCatalog updated = new DomainEventCatalog(
                event.eventId(), event.tenantId(), event.eventCode(), event.eventName(),
                event.domain(), event.aggregateType(), event.eventType(), event.description(),
                event.payloadSchema(), subscribers, event.version(), event.enabled(), event.createdAt(), now);
        return extStore.saveDomainEventCatalog(updated);
    }

    public DomainEventCatalog getEvent(String tenantId, String eventId) {
        return extStore.findDomainEventCatalog(tenantId, eventId)
                .orElseThrow(() -> new BizException("EVENT_NOT_FOUND", "事件不存在"));
    }

    public DomainEventCatalog getEventByCode(String tenantId, String eventCode) {
        return extStore.findDomainEventCatalogByCode(tenantId, eventCode)
                .orElseThrow(() -> new BizException("EVENT_NOT_FOUND", "事件不存在"));
    }

    public List<DomainEventCatalog> listEvents(String tenantId, String domain) {
        return extStore.listDomainEventCatalogs(tenantId, domain);
    }

    public record RegisterEventCommand(String eventCode, String eventName, String domain,
                                       String aggregateType, String eventType, String description,
                                       String payloadSchema, List<String> subscribers) {}
    public record UpdateEventCommand(String eventName, String description, String payloadSchema,
                                     List<String> subscribers) {}
}
