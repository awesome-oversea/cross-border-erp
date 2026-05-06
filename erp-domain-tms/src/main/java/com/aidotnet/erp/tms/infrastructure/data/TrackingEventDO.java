package com.aidotnet.erp.tms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("tms_tracking_event")
public class TrackingEventDO {

    private String tenantId;
    private String shipmentId;
    private String eventId;
    private String status;
    private String location;
    private String description;
    private Instant occurredAt;

    public TrackingEventDO() {}

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
