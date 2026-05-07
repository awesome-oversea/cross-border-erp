package com.aidotnet.erp.wms.domain;

/**
 * 出库包裹明细
 *
 * 描述: 记录包裹中每个出库行的装箱数量，支撑多包裹拆分发货。
 */
public record OutboundPackageLine(
        String packageLineId,
        String packageId,
        String orderLineId,
        String sellerSku,
        int quantity,
        String batchNo) {}
