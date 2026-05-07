package com.aidotnet.erp.crm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 客户画像数据对象
 * <p>
 * 描述: 对应crm_customer_profile表，存储客户360度画像信息。
 *       包含分群、生命周期价值、退货率、偏好渠道和风险等级等。
 * </p>
 *
 * @author ERP系统
 */
@TableName("crm_customer_profile")
public class CustomerProfileDO {

    /** 画像唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String profileId;
    /** 租户ID */
    private String tenantId;
    /** 客户ID */
    private String customerId;
    /** 客户分群: VIP/REGULAR/NEW/CHURNED */
    private String segment;
    /** 生命周期价值(LTV) */
    private BigDecimal lifetimeValue;
    /** 平均订单金额 */
    private BigDecimal avgOrderValue;
    /** 总订单数 */
    private Integer totalOrders;
    /** 总退货数 */
    private Integer totalReturns;
    /** 退货率 */
    private BigDecimal returnRate;
    /** 偏好渠道 */
    private String preferredChannel;
    /** 偏好语言 */
    private String preferredLanguage;
    /** 风险等级: LOW/MEDIUM/HIGH */
    private String riskLevel;
    /** 扩展属性，JSON格式 */
    private String attributes;
    /** 首次下单时间 */
    private Instant firstOrderAt;
    /** 最近下单时间 */
    private Instant lastOrderAt;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public CustomerProfileDO() {
    }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }
    public BigDecimal getLifetimeValue() { return lifetimeValue; }
    public void setLifetimeValue(BigDecimal lifetimeValue) { this.lifetimeValue = lifetimeValue; }
    public BigDecimal getAvgOrderValue() { return avgOrderValue; }
    public void setAvgOrderValue(BigDecimal avgOrderValue) { this.avgOrderValue = avgOrderValue; }
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    public Integer getTotalReturns() { return totalReturns; }
    public void setTotalReturns(Integer totalReturns) { this.totalReturns = totalReturns; }
    public BigDecimal getReturnRate() { return returnRate; }
    public void setReturnRate(BigDecimal returnRate) { this.returnRate = returnRate; }
    public String getPreferredChannel() { return preferredChannel; }
    public void setPreferredChannel(String preferredChannel) { this.preferredChannel = preferredChannel; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getAttributes() { return attributes; }
    public void setAttributes(String attributes) { this.attributes = attributes; }
    public Instant getFirstOrderAt() { return firstOrderAt; }
    public void setFirstOrderAt(Instant firstOrderAt) { this.firstOrderAt = firstOrderAt; }
    public Instant getLastOrderAt() { return lastOrderAt; }
    public void setLastOrderAt(Instant lastOrderAt) { this.lastOrderAt = lastOrderAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
