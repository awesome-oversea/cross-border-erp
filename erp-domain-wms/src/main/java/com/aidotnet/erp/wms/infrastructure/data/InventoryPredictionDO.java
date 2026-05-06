package com.aidotnet.erp.wms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("wms_inventory_prediction")
public class InventoryPredictionDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String predictionId;
    private String tenantId;
    private String warehouseId;
    private String sellerSku;
    private Integer predictedDemand;
    private BigDecimal predictedDaysOfSupply;
    private Integer recommendedRestockQty;
    private BigDecimal confidence;
    private String modelVersion;
    private java.time.LocalDate predictionDate;
    private Integer horizonDays;
    private Instant createdAt;

    public InventoryPredictionDO() {}

    public String getPredictionId() { return predictionId; }
    public void setPredictionId(String predictionId) { this.predictionId = predictionId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public Integer getPredictedDemand() { return predictedDemand; }
    public void setPredictedDemand(Integer predictedDemand) { this.predictedDemand = predictedDemand; }
    public BigDecimal getPredictedDaysOfSupply() { return predictedDaysOfSupply; }
    public void setPredictedDaysOfSupply(BigDecimal predictedDaysOfSupply) { this.predictedDaysOfSupply = predictedDaysOfSupply; }
    public Integer getRecommendedRestockQty() { return recommendedRestockQty; }
    public void setRecommendedRestockQty(Integer recommendedRestockQty) { this.recommendedRestockQty = recommendedRestockQty; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public java.time.LocalDate getPredictionDate() { return predictionDate; }
    public void setPredictionDate(java.time.LocalDate predictionDate) { this.predictionDate = predictionDate; }
    public Integer getHorizonDays() { return horizonDays; }
    public void setHorizonDays(Integer horizonDays) { this.horizonDays = horizonDays; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
