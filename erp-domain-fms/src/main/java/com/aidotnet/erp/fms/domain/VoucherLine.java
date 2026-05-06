package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;

public record VoucherLine(
        String lineId,
        String accountCode,
        String accountName,
        VoucherLineType type,
        BigDecimal amount,
        String remark
) {}
