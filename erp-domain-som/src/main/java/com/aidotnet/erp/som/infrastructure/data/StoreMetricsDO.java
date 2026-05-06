package com.aidotnet.erp.som.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("som_store_metrics")
public class StoreMetricsDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String metricsId;
    private String tenantId;
    private String storeId;
    private String marketplaceId;
    private BigDecimal totalRevenue;
    private BigDecimal totalOrders;
    private BigDecimal averageOrderValue;
    private BigDecimal returnRate;
    private BigDecimal feedbackScore;
    private Instant periodStart;
    private Instant periodEnd;
    private Instant createdAt;

    public StoreMetricsDO() {}

    public String getMetricsId() { return metricsId; }
    public void setMetricsId(String metricsId) { this.metricsId = metricsId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getMarketplaceId() { return marketplaceId; }
    public void setMarketplaceId(String marketplaceId) { this.marketplaceId = marketplaceId; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public BigDecimal getTotalOrders() { return totalOrders; }
    public void setTotalOrders(BigDecimal totalOrders) { this.totalOrders = totalOrders; }
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    public BigDecimal getReturnRate() { return returnRate; }
    public void setReturnRate(BigDecimal returnRate) { this.returnRate = returnRate; }
    public BigDecimal getFeedbackScore() { return feedbackScore; }
    public void setFeedbackScore(BigDecimal feedbackScore) { this.feedbackScore = feedbackScore; }
    public Instant getPeriodStart() { return periodStart; }
    public void setPeriodStart(Instant periodStart) { this.periodStart = periodStart; }
    public Instant getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(Instant periodEnd) { this.periodEnd = periodEnd; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
