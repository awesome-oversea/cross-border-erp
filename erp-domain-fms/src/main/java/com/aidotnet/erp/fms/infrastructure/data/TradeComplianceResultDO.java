package com.aidotnet.erp.fms.infrastructure.data;

import java.time.Instant;

public class TradeComplianceResultDO {
    private String resultId;
    private String tenantId;
    private String orderId;
    private String sellerSku;
    private String hsCode;
    private String originCountry;
    private String destinationCountry;
    private String status;
    private String violationsJson;
    private String warningsJson;
    private String detailsJson;
    private Instant checkedAt;

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }
    public String getOriginCountry() { return originCountry; }
    public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }
    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry) { this.destinationCountry = destinationCountry; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getViolationsJson() { return violationsJson; }
    public void setViolationsJson(String violationsJson) { this.violationsJson = violationsJson; }
    public String getWarningsJson() { return warningsJson; }
    public void setWarningsJson(String warningsJson) { this.warningsJson = warningsJson; }
    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }
    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}
