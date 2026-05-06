package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * Webhook投递记录数据对象(WebhookDeliveryDO)
 * <p>
 * 描述: Webhook投递记录数据对象，对应sys_webhook_delivery表。
 *       记录每次Webhook回调的请求/响应详情，支持重试和问题排查。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_webhook_delivery")
public class WebhookDeliveryDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String deliveryId;
    private String tenantId;
    private String endpointId;
    private String eventType;
    private String payload;
    private Integer statusCode;
    private String response;
    private Boolean success;
    private Integer attemptCount;
    private Instant nextRetryAt;
    private Instant deliveredAt;

    public WebhookDeliveryDO() {}

    public String getDeliveryId() { return deliveryId; }
    public void setDeliveryId(String deliveryId) { this.deliveryId = deliveryId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getEndpointId() { return endpointId; }
    public void setEndpointId(String endpointId) { this.endpointId = endpointId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public Integer getAttemptCount() { return attemptCount; }
    public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }
    public Instant getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}
