package com.aidotnet.erp.wms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("wms_stock_check_line")
public class StockCheckLineDO {

    private String checkId;
    private String tenantId;
    private String sellerSku;
    private String locationId;
    private Integer systemQty;
    private Integer actualQty;
    private Integer diffQty;
    private String status;
    private Instant createdAt;

    public String getCheckId() {
        return checkId;
    }

    public void setCheckId(String checkId) {
        this.checkId = checkId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSellerSku() {
        return sellerSku;
    }

    public void setSellerSku(String sellerSku) {
        this.sellerSku = sellerSku;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Integer getSystemQty() {
        return systemQty;
    }

    public void setSystemQty(Integer systemQty) {
        this.systemQty = systemQty;
    }

    public Integer getActualQty() {
        return actualQty;
    }

    public void setActualQty(Integer actualQty) {
        this.actualQty = actualQty;
    }

    public Integer getDiffQty() {
        return diffQty;
    }

    public void setDiffQty(Integer diffQty) {
        this.diffQty = diffQty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
