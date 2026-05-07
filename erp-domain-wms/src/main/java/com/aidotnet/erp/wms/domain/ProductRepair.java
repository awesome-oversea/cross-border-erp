package com.aidotnet.erp.wms.domain;

import java.time.Instant;

public record ProductRepair(
        String repairId,
        String tenantId,
        String warehouseId,
        String supplierId,
        String sellerSku,
        int outboundQuantity,
        int inboundQuantity,
        String reason,
        ProductRepairStatus status,
        QualityCheckResult qcResult,
        String processedBy,
        String remark,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt) {}
