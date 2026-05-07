package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("bi_developer_commission_report")
public class DeveloperCommissionReportDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String reportId;

    private String tenantId;

    private String userId;

    private String userName;

    private String teamCode;

    private String period;

    private Integer skuCount;

    private Integer orderCount;

    private BigDecimal orderRate;

    private BigDecimal salesProfit;

    private BigDecimal kpiScore;

    private BigDecimal baseCommissionRate;

    private BigDecimal commissionCoefficient;

    private BigDecimal commissionAmount;

    private String currency;

    private Instant createdAt;

    public DeveloperCommissionReportDO() {}

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getTeamCode() { return teamCode; }
    public void setTeamCode(String teamCode) { this.teamCode = teamCode; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Integer getSkuCount() { return skuCount; }
    public void setSkuCount(Integer skuCount) { this.skuCount = skuCount; }
    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    public BigDecimal getOrderRate() { return orderRate; }
    public void setOrderRate(BigDecimal orderRate) { this.orderRate = orderRate; }
    public BigDecimal getSalesProfit() { return salesProfit; }
    public void setSalesProfit(BigDecimal salesProfit) { this.salesProfit = salesProfit; }
    public BigDecimal getKpiScore() { return kpiScore; }
    public void setKpiScore(BigDecimal kpiScore) { this.kpiScore = kpiScore; }
    public BigDecimal getBaseCommissionRate() { return baseCommissionRate; }
    public void setBaseCommissionRate(BigDecimal baseCommissionRate) { this.baseCommissionRate = baseCommissionRate; }
    public BigDecimal getCommissionCoefficient() { return commissionCoefficient; }
    public void setCommissionCoefficient(BigDecimal commissionCoefficient) { this.commissionCoefficient = commissionCoefficient; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
