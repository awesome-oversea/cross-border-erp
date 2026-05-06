package com.aidotnet.erp.scm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("scm_processing_order")
public class ProcessingOrderDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String processId;
    private String tenantId;
    private String name;
    private String rawMaterials;
    private String outputSku;
    private int outputQuantity;
    private BigDecimal totalCost;
    private String currency;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public ProcessingOrderDO() {}

    public String getProcessId() { return processId; }
    public void setProcessId(String processId) { this.processId = processId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRawMaterials() { return rawMaterials; }
    public void setRawMaterials(String rawMaterials) { this.rawMaterials = rawMaterials; }
    public String getOutputSku() { return outputSku; }
    public void setOutputSku(String outputSku) { this.outputSku = outputSku; }
    public int getOutputQuantity() { return outputQuantity; }
    public void setOutputQuantity(int outputQuantity) { this.outputQuantity = outputQuantity; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
