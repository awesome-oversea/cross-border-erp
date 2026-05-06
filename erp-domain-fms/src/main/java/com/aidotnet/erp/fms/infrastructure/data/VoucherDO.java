package com.aidotnet.erp.fms.infrastructure.data;

import java.math.BigDecimal;
import java.time.Instant;

public class VoucherDO {
    private String voucherId;
    private String tenantId;
    private String voucherNumber;
    private String voucherType;
    private String referenceType;
    private String referenceId;
    private String currency;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private String status;
    private String linesJson;
    private Instant voucherDate;
    private String postedBy;
    private Instant postedAt;
    private String exportBatchId;
    private Instant createdAt;
    private Instant updatedAt;

    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getVoucherNumber() { return voucherNumber; }
    public void setVoucherNumber(String voucherNumber) { this.voucherNumber = voucherNumber; }
    public String getVoucherType() { return voucherType; }
    public void setVoucherType(String voucherType) { this.voucherType = voucherType; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getTotalDebit() { return totalDebit; }
    public void setTotalDebit(BigDecimal totalDebit) { this.totalDebit = totalDebit; }
    public BigDecimal getTotalCredit() { return totalCredit; }
    public void setTotalCredit(BigDecimal totalCredit) { this.totalCredit = totalCredit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLinesJson() { return linesJson; }
    public void setLinesJson(String linesJson) { this.linesJson = linesJson; }
    public Instant getVoucherDate() { return voucherDate; }
    public void setVoucherDate(Instant voucherDate) { this.voucherDate = voucherDate; }
    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }
    public Instant getPostedAt() { return postedAt; }
    public void setPostedAt(Instant postedAt) { this.postedAt = postedAt; }
    public String getExportBatchId() { return exportBatchId; }
    public void setExportBatchId(String exportBatchId) { this.exportBatchId = exportBatchId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
