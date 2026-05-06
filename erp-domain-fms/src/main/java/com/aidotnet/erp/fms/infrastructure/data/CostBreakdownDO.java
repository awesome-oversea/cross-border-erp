package com.aidotnet.erp.fms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("fms_cost_breakdown")
public class CostBreakdownDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String breakdownId;
    private String tenantId;
    private String costEventId;
    private String costType;
    private String costCategory;
    private BigDecimal amount;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal amountInBaseCurrency;
    private String remark;
    private Instant createdAt;

    public CostBreakdownDO() {}

    public String getBreakdownId() { return breakdownId; }
    public void setBreakdownId(String breakdownId) { this.breakdownId = breakdownId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCostEventId() { return costEventId; }
    public void setCostEventId(String costEventId) { this.costEventId = costEventId; }
    public String getCostType() { return costType; }
    public void setCostType(String costType) { this.costType = costType; }
    public String getCostCategory() { return costCategory; }
    public void setCostCategory(String costCategory) { this.costCategory = costCategory; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public BigDecimal getAmountInBaseCurrency() { return amountInBaseCurrency; }
    public void setAmountInBaseCurrency(BigDecimal amountInBaseCurrency) { this.amountInBaseCurrency = amountInBaseCurrency; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
