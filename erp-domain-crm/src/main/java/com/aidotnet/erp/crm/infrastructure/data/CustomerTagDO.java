package com.aidotnet.erp.crm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 客户标签数据对象
 * <p>
 * 描述: 对应crm_customer_tag表，存储客户标签(支持多标签)。
 *       用于客户精细化分群和画像构建。
 * </p>
 *
 * @author ERP系统
 */
@TableName("crm_customer_tag")
public class CustomerTagDO {

    /** 标签ID(主键) */
    @TableId(type = IdType.ASSIGN_ID)
    private String tagId;
    /** 租户ID */
    private String tenantId;
    /** 客户ID */
    private String customerId;
    /** 标签名称 */
    private String tagName;
    /** 标签值 */
    private String tagValue;
    /** 打标时间 */
    @TableField("created_at")
    private Instant taggedAt;

    public CustomerTagDO() {}

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getTagValue() { return tagValue; }
    public void setTagValue(String tagValue) { this.tagValue = tagValue; }
    public Instant getTaggedAt() { return taggedAt; }
    public void setTaggedAt(Instant taggedAt) { this.taggedAt = taggedAt; }
}
