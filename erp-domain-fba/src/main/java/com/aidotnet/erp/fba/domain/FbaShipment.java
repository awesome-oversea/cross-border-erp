package com.aidotnet.erp.fba.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * FBA发货单领域模型
 * <p>
 * 描述: FBA发货单，记录向亚马逊FBA仓库发货的详细信息，包括亚马逊发货ID、
 *       目的FBA仓、承运商、追踪号、计划/接收数量和箱数。
 * </p>
 *
 * @author ERP系统
 */
public record FbaShipment(String fbaShipmentId, String tenantId, String amazonShipmentId, String destinationFc,
                          String planId, String carrier, String trackingNo, int plannedQuantity,
                          int receivedQuantity, int cartonCount, BigDecimal totalWeight,
                          FbaShipmentStatus status, Instant packedAt, Instant shippedAt,
                          Instant createdAt, Instant updatedAt) {}
