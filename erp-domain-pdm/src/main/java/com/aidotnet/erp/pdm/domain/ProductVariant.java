package com.aidotnet.erp.pdm.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 产品变体领域模型
 * <p>
 * 描述: 产品变体实体，代表SPU下的不同规格组合。
 *       每个变体有独立的采购成本、销售价格、SKU编码和图片。
 *       如: "iPhone 15 黑色 128G" 和 "iPhone 15 白色 256G" 是同一SPU的不同变体。
 * </p>
 * <p>
 * 业务规则:
 *   1. 变体必须归属于某个SPU(spuId)
 *   2. 变体创建后为DRAFT状态，需激活后才可参与销售
 *   3. sellerSku在租户内唯一
 *   4. 变体属性(variantAttributes)为JSON格式，描述规格差异
 * </p>
 *
 * @param variantId       变体唯一标识
 * @param tenantId        租户ID
 * @param spuId           所属SPU ID
 * @param variantName     变体名称，如 "黑色128G"
 * @param variantAttributes 变体属性，JSON格式，如 {"color":"黑色","storage":"128G"}
 * @param purchaseCost    采购成本
 * @param sellingPrice    销售价格
 * @param priceAdjustment 价格调整值，用于变体间的价格差异
 * @param sellerSku       卖家SKU编码，租户内唯一
 * @param images          变体图片URL列表
 * @param status          产品状态: DRAFT/ACTIVE/INACTIVE/ARCHIVED
 * @param createdAt       创建时间
 * @param updatedAt       更新时间
 * @author ERP系统
 */
public record ProductVariant(
        String variantId,
        String tenantId,
        String spuId,
        String variantName,
        String variantAttributes,
        BigDecimal purchaseCost,
        BigDecimal sellingPrice,
        BigDecimal priceAdjustment,
        String sellerSku,
        List<String> images,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
