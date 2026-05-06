package com.aidotnet.erp.fms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@TableName("fms_platform_settlement")
public class PlatformSettlementDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String settlementId;
    private String tenantId;
    private String platform;
    private String store;
    private String settlementType;
    private BigDecimal amount;
    private BigDecimal reconciledAmount;
    private Integer linkedBillCount;
    private BigDecimal receivedAmount;
    private String currency;
    private LocalDate settlementDate;
    private String status;
    private String withdrawalStatus;
    private String withdrawalReference;
    private String forexStatus;
    private BigDecimal forexRate;
    private Instant receivedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public PlatformSettlementDO() {}

    public String getSettlementId() { return settlementId; }
    public void setSettlementId(String settlementId) { this.settlementId = settlementId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }
    public String getSettlementType() { return settlementType; }
    public void setSettlementType(String settlementType) { this.settlementType = settlementType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getReconciledAmount() { return reconciledAmount; }
    public void setReconciledAmount(BigDecimal reconciledAmount) { this.reconciledAmount = reconciledAmount; }
    public Integer getLinkedBillCount() { return linkedBillCount; }
    public void setLinkedBillCount(Integer linkedBillCount) { this.linkedBillCount = linkedBillCount; }
    public BigDecimal getReceivedAmount() { return receivedAmount; }
    public void setReceivedAmount(BigDecimal receivedAmount) { this.receivedAmount = receivedAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getWithdrawalStatus() { return withdrawalStatus; }
    public void setWithdrawalStatus(String withdrawalStatus) { this.withdrawalStatus = withdrawalStatus; }
    public String getWithdrawalReference() { return withdrawalReference; }
    public void setWithdrawalReference(String withdrawalReference) { this.withdrawalReference = withdrawalReference; }
    public String getForexStatus() { return forexStatus; }
    public void setForexStatus(String forexStatus) { this.forexStatus = forexStatus; }
    public BigDecimal getForexRate() { return forexRate; }
    public void setForexRate(BigDecimal forexRate) { this.forexRate = forexRate; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
