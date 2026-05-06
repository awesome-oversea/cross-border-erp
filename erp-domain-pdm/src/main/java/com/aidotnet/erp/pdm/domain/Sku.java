package com.aidotnet.erp.pdm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * SKU(库存单元)领域模型
 * <p>
 * 描述: 产品主数据域核心实体，代表产品的最小可售单元。
 *       SKU是SPU的具体规格组合，包含重量、申报价值等物流和清关信息。
 *       如: "iPhone 15 黑色 128G"是一个SKU，对应具体的重量和申报价值。
 * </p>
 * <p>
 * 业务规则:
 *   1. sellerSku(卖家SKU编码)在同一租户下唯一
 *   2. SKU必须归属于某个SPU(spuId)
 *   3. 申报价值(declaredValue)用于清关和物流计算
 *   4. 重量(weightKg)用于物流运费计算
 * </p>
 *
 * @param skuId         SKU唯一标识
 * @param tenantId      租户ID
 * @param spuId         所属SPU ID
 * @param sellerSku     卖家SKU编码，租户内唯一
 * @param title         SKU标题，包含规格描述
 * @param weightKg      重量(千克)，用于物流运费计算
 * @param declaredValue 申报价值，用于清关申报
 * @param currency      申报价值币种，如 USD、CNY
 * @param status        产品状态: DRAFT(草稿)、APPROVED(已审核)、ACTIVE(上架)、INACTIVE(下架)、ARCHIVED(归档)
 * @param createdAt     创建时间
 * @param updatedAt     更新时间
 * @author ERP系统
 */
public record Sku(String skuId, String tenantId, String spuId, String sellerSku, String title, BigDecimal weightKg,
                  BigDecimal declaredValue, String currency, ProductStatus status, Instant createdAt, Instant updatedAt) {}
