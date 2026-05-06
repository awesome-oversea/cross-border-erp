package com.aidotnet.erp.pdm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * SKU数据对象
 * <p>
 * 描述: 对应pdm_sku表，存储最小可售单元(Stock Keeping Unit)数据。
 *       SKU是SPU的具体规格组合，包含物流和清关信息。
 * </p>
 *
 * @author ERP系统
 */
@TableName("pdm_sku")
public class SkuDO {

    /** SKU唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String skuId;
    /** 租户ID */
    private String tenantId;
    /** 所属SPU ID */
    private String spuId;
    /** 卖家SKU编码，租户内唯一 */
    private String sellerSku;
    /** SKU标题 */
    private String title;
    /** 重量(千克)，用于物流运费计算 */
    private BigDecimal weightKg;
    /** 申报价值，用于清关申报 */
    private BigDecimal declaredValue;
    /** 申报价值币种 */
    private String currency;
    /** 产品状态: DRAFT/APPROVED/ACTIVE/INACTIVE/ARCHIVED */
    private String status;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public SkuDO() {}

    public String getSkuId() { return skuId; }
    public void setSkuId(String skuId) { this.skuId = skuId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSpuId() { return spuId; }
    public void setSpuId(String spuId) { this.spuId = spuId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public BigDecimal getDeclaredValue() { return declaredValue; }
    public void setDeclaredValue(BigDecimal declaredValue) { this.declaredValue = declaredValue; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
