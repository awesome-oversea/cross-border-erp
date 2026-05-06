package com.aidotnet.erp.wms.domain;

public record StockCheckOrderLine(
        String lineId,
        String checkOrderId,
        String sellerSku,
        String locationId,
        int systemQuantity,
        int actualQuantity,
        int difference,
        LineStatus status
) {
    public enum LineStatus {
        PENDING,
        COUNTED,
        ADJUSTED
    }
}
