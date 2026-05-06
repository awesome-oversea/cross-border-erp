package com.aidotnet.erp.fba.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("fba_carton_label")
public class CartonLabelDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String labelId;
    private String tenantId;
    private String fbaShipmentId;
    private String cartonId;
    private String sellerSku;
    private Integer quantityPerCarton;
    private Integer numberOfCartons;
    private String labelUrl;
    private Instant createdAt;

    public CartonLabelDO() {}

    public String getLabelId() { return labelId; }
    public void setLabelId(String labelId) { this.labelId = labelId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getFbaShipmentId() { return fbaShipmentId; }
    public void setFbaShipmentId(String fbaShipmentId) { this.fbaShipmentId = fbaShipmentId; }
    public String getCartonId() { return cartonId; }
    public void setCartonId(String cartonId) { this.cartonId = cartonId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public Integer getQuantityPerCarton() { return quantityPerCarton; }
    public void setQuantityPerCarton(Integer quantityPerCarton) { this.quantityPerCarton = quantityPerCarton; }
    public Integer getNumberOfCartons() { return numberOfCartons; }
    public void setNumberOfCartons(Integer numberOfCartons) { this.numberOfCartons = numberOfCartons; }
    public String getLabelUrl() { return labelUrl; }
    public void setLabelUrl(String labelUrl) { this.labelUrl = labelUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
