package com.aidotnet.erp.fms.domain;

import java.time.Instant;
import java.util.Map;

public record ExternalFinanceVoucher(
        String voucherId,
        String tenantId,
        String financeSystem,
        String voucherType,
        String voucherNumber,
        String erpReferenceType,
        String erpReferenceId,
        Map<String, Object> voucherData,
        String syncStatus,
        String syncError,
        Instant syncedAt,
        Instant createdAt,
        Instant updatedAt) {}
