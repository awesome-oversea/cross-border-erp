package com.aidotnet.erp.wms.domain;

public record OutboundOrderLine(String lineId, String orderId, String sellerSku, String locationId,
                                int requiredQuantity, int pickedQuantity, String batchNo) {}
