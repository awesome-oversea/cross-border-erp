package com.aidotnet.erp.wms.infrastructure.data;

import java.math.BigDecimal;

public class TransferOrderLineDO {
    private String lineId;
    private String tenantId;
    private String transferId;
    private String sellerSku;
    private int transferQuantity;
    private int receivedQuantity;
    private BigDecimal unitCost;
    private String batchNo;

    public String getLineId() { return lineId; }
    public void setLineId(String lineId) { this.lineId = lineId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public int getTransferQuantity() { return transferQuantity; }
    public void setTransferQuantity(int transferQuantity) { this.transferQuantity = transferQuantity; }
    public int getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
}
