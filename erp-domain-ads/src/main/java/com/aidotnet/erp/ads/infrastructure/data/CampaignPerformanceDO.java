package com.aidotnet.erp.ads.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 广告活动效果数据持久化对象，记录广告活动的花费、曝光、点击等核心效果指标。
 * <p>
 * 对应数据库表: ads_campaign_performance
 * </p>
 * <p>
 * 业务说明:
 *   效果数据按时间段(periodStart~periodEnd)记录，支持按日/周/月粒度统计。
 *   核心指标包括: 花费(spend)、曝光(impressions)、点击(clicks)、
 *   点击率(CTR)、订单(orders)、ACOS、ROAS。
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.ads.domain.CampaignPerformance
 */
@TableName("ads_campaign_performance")
public class CampaignPerformanceDO {

    /** 效果记录唯一标识。 */
    @TableId(type = IdType.ASSIGN_ID)
    private String performanceId;

    /** 租户标识，用于多租户数据隔离。 */
    private String tenantId;

    /** 关联广告活动ID。 */
    private String campaignId;

    /** 广告花费金额。 */
    private BigDecimal spend;

    /** 曝光量。 */
    private Integer impressions;

    /** 点击量。 */
    private Integer clicks;

    /** 点击率(CTR)，clicks / impressions。 */
    private BigDecimal ctr;

    /** 订单量。 */
    private Integer orders;

    /** 广告成本销售比(ACOS)，spend / orders。 */
    private BigDecimal acos;

    /** 广告支出回报率(ROAS)，orders / spend。 */
    private BigDecimal roas;

    /** 统计周期起始时间。 */
    private Instant periodStart;

    /** 统计周期结束时间。 */
    private Instant periodEnd;

    /** 创建时间。 */
    private Instant createdAt;

    public CampaignPerformanceDO() {}

    public String getPerformanceId() { return performanceId; }
    public void setPerformanceId(String performanceId) { this.performanceId = performanceId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public BigDecimal getSpend() { return spend; }
    public void setSpend(BigDecimal spend) { this.spend = spend; }
    public Integer getImpressions() { return impressions; }
    public void setImpressions(Integer impressions) { this.impressions = impressions; }
    public Integer getClicks() { return clicks; }
    public void setClicks(Integer clicks) { this.clicks = clicks; }
    public BigDecimal getCtr() { return ctr; }
    public void setCtr(BigDecimal ctr) { this.ctr = ctr; }
    public Integer getOrders() { return orders; }
    public void setOrders(Integer orders) { this.orders = orders; }
    public BigDecimal getAcos() { return acos; }
    public void setAcos(BigDecimal acos) { this.acos = acos; }
    public BigDecimal getRoas() { return roas; }
    public void setRoas(BigDecimal roas) { this.roas = roas; }
    public Instant getPeriodStart() { return periodStart; }
    public void setPeriodStart(Instant periodStart) { this.periodStart = periodStart; }
    public Instant getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(Instant periodEnd) { this.periodEnd = periodEnd; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
