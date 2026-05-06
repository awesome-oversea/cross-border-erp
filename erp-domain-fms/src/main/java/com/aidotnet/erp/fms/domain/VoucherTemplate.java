package com.aidotnet.erp.fms.domain;

import java.time.Instant;

public record VoucherTemplate(
        String templateId,
        String tenantId,
        String templateName,
        String businessType,
        String debitAccount,
        String creditAccount,
        String description,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    public enum BusinessType {
        PURCHASE_INBOUND,
        SALES_OUTBOUND,
        RETURN_INBOUND,
        RETURN_OUTBOUND,
        INVENTORY_GAIN,
        INVENTORY_LOSS,
        TRANSFER_IN,
        TRANSFER_OUT,
        ADJUSTMENT,
        COST_SETTLEMENT
    }
}
