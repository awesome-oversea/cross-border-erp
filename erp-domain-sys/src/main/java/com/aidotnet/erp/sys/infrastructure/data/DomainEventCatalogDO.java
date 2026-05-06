package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 领域事件目录数据对象(DomainEventCatalogDO)
 * <p>
 * 描述: 领域事件目录数据对象，对应sys_domain_event_catalog表。
 *       存储系统内所有领域事件的注册信息，支持事件发现和订阅管理。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_domain_event_catalog")
public class DomainEventCatalogDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String eventId;
    private String tenantId;
    private String eventCode;
    private String eventName;
    private String domain;
    private String aggregateType;
    private String eventType;
    private String description;
    private String payloadSchema;
    private String subscribers;
    private String version;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public DomainEventCatalogDO() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPayloadSchema() { return payloadSchema; }
    public void setPayloadSchema(String payloadSchema) { this.payloadSchema = payloadSchema; }
    public String getSubscribers() { return subscribers; }
    public void setSubscribers(String subscribers) { this.subscribers = subscribers; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
