package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.util.List;

public record BillingSimulationResult(
        String tenantId,
        String platform,
        BigDecimal orderAmount,
        BigDecimal platformCommission,
        BigDecimal warehouseFee,
        BigDecimal freightFee,
        BigDecimal packagingCost,
        BigDecimal fbaHeadCost,
        BigDecimal serviceFee,
        BigDecimal totalFee,
        BigDecimal netAmount,
        List<FeeDetail> feeDetails
) {
    public record FeeDetail(
            String feeType,
            String ruleName,
            BigDecimal rate,
            BigDecimal amount,
            String calculationMethod
    ) {}
}
