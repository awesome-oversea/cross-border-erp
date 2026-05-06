package com.aidotnet.erp.wms.domain;

import java.time.Instant;

public record StockCheck(
        String checkId,
        String tenantId,
        String warehouseId,
        String sellerSku,
        int systemQuantity,
        int actualQuantity,
        int difference,
        CheckStatus status,
        String checkedBy,
        Instant checkedAt,
        Instant createdAt
) {
    public enum CheckStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        ADJUSTED
    }
}
