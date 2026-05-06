package com.aidotnet.erp.wms.infrastructure.data;

public class StockCheckOrderLineDO {
    private String lineId;
    private String checkOrderId;
    private String sellerSku;
    private String locationId;
    private int systemQuantity;
    private int actualQuantity;
    private int difference;
    private String status;

    public String getLineId() { return lineId; }
    public void setLineId(String lineId) { this.lineId = lineId; }
    public String getCheckOrderId() { return checkOrderId; }
    public void setCheckOrderId(String checkOrderId) { this.checkOrderId = checkOrderId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public int getSystemQuantity() { return systemQuantity; }
    public void setSystemQuantity(int systemQuantity) { this.systemQuantity = systemQuantity; }
    public int getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(int actualQuantity) { this.actualQuantity = actualQuantity; }
    public int getDifference() { return difference; }
    public void setDifference(int difference) { this.difference = difference; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
