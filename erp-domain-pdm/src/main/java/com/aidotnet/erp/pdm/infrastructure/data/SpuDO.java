package com.aidotnet.erp.pdm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * SPU数据对象
 * <p>
 * 描述: 对应pdm_spu表，存储产品标准单元(Standard Product Unit)数据。
 *       SPU是产品的抽象层，一个SPU可包含多个SKU。
 * </p>
 *
 * @author ERP系统
 */
@TableName("pdm_spu")
public class SpuDO {

    /** SPU唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String spuId;
    /** 租户ID */
    private String tenantId;
    /** SPU编码，租户内唯一 */
    private String sku;
    /** 产品标题 */
    private String title;
    /** 产品描述 */
    private String description;
    /** 所属类目ID */
    private String categoryId;
    /** 所属品牌ID */
    private String brandId;
    /** 产品状态: DRAFT/APPROVED/ACTIVE/INACTIVE/ARCHIVED */
    private String status;
    /** 知识产权状态: none/pending/clear/infringing */
    private String ipStatus;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public SpuDO() {}

    public String getSpuId() { return spuId; }
    public void setSpuId(String spuId) { this.spuId = spuId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIpStatus() { return ipStatus; }
    public void setIpStatus(String ipStatus) { this.ipStatus = ipStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
