package com.aidotnet.erp.fms.infrastructure.data;

import java.math.BigDecimal;
import java.time.Instant;

public class CostAllocationResultDO {
    private String resultId;
    private String tenantId;
    private String ruleId;
    private String costEventId;
    private String targetDimension;
    private String targetId;
    private BigDecimal allocatedAmount;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal amountInBaseCurrency;
    private String dimensionsJson;
    private Instant allocatedAt;

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getCostEventId() { return costEventId; }
    public void setCostEventId(String costEventId) { this.costEventId = costEventId; }
    public String getTargetDimension() { return targetDimension; }
    public void setTargetDimension(String targetDimension) { this.targetDimension = targetDimension; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public BigDecimal getAmountInBaseCurrency() { return amountInBaseCurrency; }
    public void setAmountInBaseCurrency(BigDecimal amountInBaseCurrency) { this.amountInBaseCurrency = amountInBaseCurrency; }
    public String getDimensionsJson() { return dimensionsJson; }
    public void setDimensionsJson(String dimensionsJson) { this.dimensionsJson = dimensionsJson; }
    public Instant getAllocatedAt() { return allocatedAt; }
    public void setAllocatedAt(Instant allocatedAt) { this.allocatedAt = allocatedAt; }
}
