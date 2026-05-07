package com.aidotnet.erp.wms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 出库包裹领域模型
 *
 * 描述: 记录出库单对应的打包、称重、承运和发货状态。
 */
public record OutboundPackage(
        String packageId,
        String tenantId,
        String orderId,
        String warehouseId,
        String carrierCode,
        String trackingNo,
        BigDecimal weightKg,
        OutboundPackageStatus status,
        String remark,
        Instant packedAt,
        Instant shippedAt,
        Instant createdAt,
        Instant updatedAt) {}
