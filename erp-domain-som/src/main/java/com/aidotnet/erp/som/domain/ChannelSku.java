package com.aidotnet.erp.som.domain;

import java.time.Instant;

/**
 * 渠道SKU映射领域模型
 * <p>
 * 描述: 产品SKU与渠道SKU的映射关系，用于多渠道分发时统一管理。
 *       同一产品SKU可映射到不同渠道的不同SKU编码。
 * </p>
 *
 * @param channelSkuId  渠道SKU映射唯一标识
 * @param tenantId      租户ID
 * @param productSku    内部产品SKU编码
 * @param channel       渠道标识: amazon/ebay/shopify等
 * @param channelSku    渠道侧SKU编码
 * @param storeId       关联店铺ID
 * @param marketplaceId 市场站点ID
 * @param status        状态: ACTIVE/INACTIVE
 * @param createdAt     创建时间
 * @param updatedAt     更新时间
 * @author ERP系统
 */
public record ChannelSku(String channelSkuId, String tenantId, String productSku, String channel,
                         String channelSku, String storeId, String marketplaceId,
                         ChannelSkuStatus status, Instant createdAt, Instant updatedAt) {}
