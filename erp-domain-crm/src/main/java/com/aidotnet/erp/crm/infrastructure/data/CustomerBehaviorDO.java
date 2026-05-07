package com.aidotnet.erp.crm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 客户行为数据对象
 * <p>
 * 描述: 对应crm_customer_behavior表，存储客户行为事件记录。
 *       记录浏览、加购、下单、评价等行为，用于客户行为分析。
 * </p>
 *
 * @author ERP系统
 */
@TableName("crm_customer_behavior")
public class CustomerBehaviorDO {

    /** 行为唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String behaviorId;
    /** 租户ID */
    private String tenantId;
    /** 客户ID */
    private String customerId;
    /** 行为类型: BROWSE/ADD_TO_CART/ORDER/REVIEW/RETURN/COMPLAINT */
    private String behaviorType;
    /** 行为渠道 */
    private String channel;
    /** 对象类型: product/order/review */
    private String objectType;
    /** 对象ID */
    private String objectId;
    /** 行为上下文，JSON格式 */
    private String context;
    /** 行为发生时间 */
    private Instant occurredAt;
    /** 记录创建时间 */
    private Instant createdAt;

    public CustomerBehaviorDO() {
    }

    public String getBehaviorId() { return behaviorId; }
    public void setBehaviorId(String behaviorId) { this.behaviorId = behaviorId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getBehaviorType() { return behaviorType; }
    public void setBehaviorType(String behaviorType) { this.behaviorType = behaviorType; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
