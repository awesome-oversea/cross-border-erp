package com.aidotnet.erp.wms.domain;

import java.math.BigDecimal;

public record TransferOrderLine(
        String lineId,
        String transferId,
        String sellerSku,
        int transferQuantity,
        int receivedQuantity,
        BigDecimal unitCost,
        String batchNo
) {}
