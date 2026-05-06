package com.aidotnet.erp.ads.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 广告活动持久化对象，承载广告活动的核心投放信息与 PMS 优化配置。
 */
@TableName("ads_campaign")
public class AdCampaignDO {

    /** 广告活动唯一标识。 */
    @TableId(type = IdType.ASSIGN_ID)
    private String campaignId;
    /** 租户标识，用于租户级数据隔离。 */
    private String tenantId;
    /** 广告投放平台，例如 AmazonAds。 */
    private String platform;
    /** 广告活动名称。 */
    private String campaignName;
    /** 每日预算金额。 */
    private BigDecimal dailyBudget;
    /** 广告活动状态。 */
    private String status;
    /** 是否启用 PMS 自动优化。 */
    private Boolean pmsOptimizationEnabled;
    /** PMS 自动优化适用范围。 */
    private String pmsOptimizationScope;
    /** 创建时间。 */
    private Instant createdAt;
    /** 更新时间。 */
    private Instant updatedAt;

    public AdCampaignDO() {}

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    public BigDecimal getDailyBudget() { return dailyBudget; }
    public void setDailyBudget(BigDecimal dailyBudget) { this.dailyBudget = dailyBudget; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getPmsOptimizationEnabled() { return pmsOptimizationEnabled; }
    public void setPmsOptimizationEnabled(Boolean pmsOptimizationEnabled) { this.pmsOptimizationEnabled = pmsOptimizationEnabled; }
    public String getPmsOptimizationScope() { return pmsOptimizationScope; }
    public void setPmsOptimizationScope(String pmsOptimizationScope) { this.pmsOptimizationScope = pmsOptimizationScope; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
