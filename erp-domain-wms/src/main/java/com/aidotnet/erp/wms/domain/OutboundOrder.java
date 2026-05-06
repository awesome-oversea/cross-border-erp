package com.aidotnet.erp.wms.domain;

import java.time.Instant;

/**
 * 出库单领域模型
 * <p>
 * 描述: 出库单据，关联销售订单或调拨单，记录出库状态。
 * </p>
 *
 * @author ERP系统
 */
public record OutboundOrder(String orderId, String tenantId, String warehouseId, String referenceType,
                            String referenceId, OutboundOrderStatus status, String remark,
                            Instant createdAt, Instant updatedAt) {}
