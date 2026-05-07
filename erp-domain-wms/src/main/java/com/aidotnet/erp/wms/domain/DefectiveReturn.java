package com.aidotnet.erp.wms.domain;

import java.time.Instant;

public record DefectiveReturn(
        String returnId,
        String tenantId,
        String warehouseId,
        String poId,
        String supplierId,
        String sellerSku,
        int quantity,
        String reason,
        DefectiveSupplierReply supplierReply,
        DefectiveReturnStatus status,
        String processedBy,
        String remark,
        Instant processedAt,
        Instant createdAt,
        Instant updatedAt) {}
