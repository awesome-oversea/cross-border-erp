package com.aidotnet.erp.wms.domain;

import java.time.Instant;

/**
 * 库存移动领域模型
 * <p>
 * 描述: 库存在库位间的移动记录，记录源库位、目标库位和关联单据。
 * </p>
 *
 * @author ERP系统
 */
public record InventoryMovement(String movementId, String tenantId, String warehouseId, String sellerSku,
                                String fromLocationId, String toLocationId, int quantity,
                                MovementType type, String referenceType, String referenceId,
                                Instant movedAt) {}
