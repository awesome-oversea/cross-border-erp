package com.aidotnet.erp.tms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 发货单领域模型
 * <p>
 * 描述: TMS域核心实体，表示一次物流发货。关联订单、仓库、承运商和物流渠道，
 *       包含包裹尺寸、追踪事件和预计/实际送达时间。
 * </p>
 *
 * @author ERP系统
 */
public record Shipment(
        String shipmentId,
        String tenantId,
        String orderId,
        String warehouseId,
        String carrierId,
        String shippingMethodId,
        String trackingNo,
        String destinationCountry,
        BigDecimal weight,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        Instant estimatedDelivery,
        Instant actualDelivery,
        ShipmentStatus status,
        List<TrackingEvent> trackingEvents,
        Instant createdAt,
        Instant updatedAt
) {
    /** 计算包裹体积(长×宽×高) */
    public BigDecimal getVolume() {
        if (length != null && width != null && height != null) {
            return length.multiply(width).multiply(height);
        }
        return BigDecimal.ZERO;
    }
}
