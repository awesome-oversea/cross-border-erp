package com.aidotnet.erp.som.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Listing领域模型
 * <p>
 * 描述: 销售运营域核心实体，表示产品在某个销售平台上的上架信息。
 *       一个产品(SPU)可在多个平台/店铺创建多个Listing。
 * </p>
 * <p>
 * 状态流转: DRAFT(草稿) → ACTIVE(上架) → INACTIVE(下架) → ARCHIVED(归档)
 * </p>
 *
 * @param listingId             Listing唯一标识
 * @param tenantId              租户ID
 * @param productId             关联产品ID(SPU)
 * @param storeId               关联店铺ID
 * @param title                 Listing标题
 * @param description           Listing描述
 * @param price                 售价
 * @param originalPrice         原价
 * @param platform              销售平台: amazon/ebay/shopify/shopee等
 * @param marketplace           市场站点: US/UK/DE/JP等
 * @param marketplaceListingId  平台侧Listing ID
 * @param qualityScore          质量评分(0-100)
 * @param status                状态: DRAFT/ACTIVE/INACTIVE/ARCHIVED
 * @param createdAt             创建时间
 * @param updatedAt             更新时间
 * @author ERP系统
 */
public record Listing(String listingId, String tenantId, String productId, String storeId,
                      String title, String description, BigDecimal price, BigDecimal originalPrice,
                      String platform, String marketplace, String marketplaceListingId,
                      BigDecimal qualityScore, ListingStatus status, Instant createdAt, Instant updatedAt) {

    /** 是否可上架(草稿或下架状态) */
    public boolean canPublish() {
        return status == ListingStatus.DRAFT || status == ListingStatus.INACTIVE;
    }

    /** 是否上架中 */
    public boolean isActive() {
        return status == ListingStatus.ACTIVE;
    }

    /** 质量评分是否过低(<30分) */
    public boolean hasLowQuality() {
        return qualityScore != null && qualityScore.compareTo(BigDecimal.valueOf(30)) < 0;
    }
}
