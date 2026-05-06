package com.aidotnet.erp.oms.domain;

import java.time.Instant;

/**
 * 原始订单快照
 *
 * @param snapshotId       快照ID
 * @param tenantId         租户ID
 * @param platform         平台
 * @param marketplace      市场站点
 * @param storeId          店铺ID
 * @param platformOrderNo  平台订单号
 * @param buyerName        买家名称
 * @param countryCode      国家
 * @param shippingAddress  地址
 * @param currency         币种
 * @param importMode       导入方式(API/IMPORT)
 * @param rawPayload       原始负载
 * @param processingStatus 处理状态(RECEIVED/IMPORTED/DUPLICATED/REJECTED)
 * @param standardOrderId  标准订单ID
 * @param duplicateOrderId 重复命中的订单ID
 * @param createdAt        创建时间
 * @param updatedAt        更新时间
 */
public record OriginalOrderSnapshot(
        String snapshotId,
        String tenantId,
        String platform,
        String marketplace,
        String storeId,
        String platformOrderNo,
        String buyerName,
        String countryCode,
        String shippingAddress,
        String currency,
        String importMode,
        String rawPayload,
        String processingStatus,
        String standardOrderId,
        String duplicateOrderId,
        Instant createdAt,
        Instant updatedAt
) {
}
