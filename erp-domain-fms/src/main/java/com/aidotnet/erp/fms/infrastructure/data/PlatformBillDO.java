package com.aidotnet.erp.fms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("fms_platform_bill")
public class PlatformBillDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String billId;
    private String tenantId;
    private String platform;
    private String store;
    private String billType;
    private String period;
    private String sellerSku;
    private String marketplaceId;
    private String sourceType;
    private String sourceId;
    private String currency;
    private BigDecimal amount;
    private String rawData;
    private String status;
    private String settlementId;
    private Instant createdAt;
    private Instant updatedAt;

    public PlatformBillDO() {}

    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }
    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public String getMarketplaceId() { return marketplaceId; }
    public void setMarketplaceId(String marketplaceId) { this.marketplaceId = marketplaceId; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSettlementId() { return settlementId; }
    public void setSettlementId(String settlementId) { this.settlementId = settlementId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
