package com.aidotnet.erp.oms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 履约包裹领域模型
 * <p>
 * 描述: 订单履约中的单个包裹，包含仓库、物流渠道、发货信息和平台同步状态。
 *       一个订单可拆分为多个包裹，分别从不同仓库发货。
 * </p>
 *
 * @author ERP系统
 */
public record OrderFulfillmentPackage(
        String packageId,
        String planId,
        String warehouseId,
        String warehouseCode,
        String carrierId,
        String carrierCode,
        String carrierName,
        String destinationCountry,
        String serviceLevel,
        FulfillmentPackageStatus status,
        int totalQuantity,
        BigDecimal totalAmount,
        BigDecimal estimatedShippingCost,
        Integer estimatedDeliveryDays,
        String note,
        String shipmentId,
        String trackingNo,
        Instant shippedAt,
        PlatformShipmentSyncStatus platformSyncStatus,
        int platformSyncAttempts,
        String platformSyncError,
        Instant platformSyncedAt,
        List<OrderFulfillmentPackageLine> lines
) {}
