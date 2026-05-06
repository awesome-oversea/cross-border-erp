package com.aidotnet.erp.fms.infrastructure.data;

import java.math.BigDecimal;
import java.time.Instant;

public class ProfitDeviationAlertDO {
    private String alertId;
    private String tenantId;
    private String dimensionType;
    private String dimensionId;
    private String sellerSku;
    private BigDecimal expectedMargin;
    private BigDecimal actualMargin;
    private BigDecimal deviation;
    private BigDecimal deviationThreshold;
    private String severity;
    private String status;
    private Instant detectedAt;
    private Instant resolvedAt;

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getDimensionType() { return dimensionType; }
    public void setDimensionType(String dimensionType) { this.dimensionType = dimensionType; }
    public String getDimensionId() { return dimensionId; }
    public void setDimensionId(String dimensionId) { this.dimensionId = dimensionId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public BigDecimal getExpectedMargin() { return expectedMargin; }
    public void setExpectedMargin(BigDecimal expectedMargin) { this.expectedMargin = expectedMargin; }
    public BigDecimal getActualMargin() { return actualMargin; }
    public void setActualMargin(BigDecimal actualMargin) { this.actualMargin = actualMargin; }
    public BigDecimal getDeviation() { return deviation; }
    public void setDeviation(BigDecimal deviation) { this.deviation = deviation; }
    public BigDecimal getDeviationThreshold() { return deviationThreshold; }
    public void setDeviationThreshold(BigDecimal deviationThreshold) { this.deviationThreshold = deviationThreshold; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
