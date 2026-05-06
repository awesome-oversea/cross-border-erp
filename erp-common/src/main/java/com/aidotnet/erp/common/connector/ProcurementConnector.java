package com.aidotnet.erp.common.connector;

import java.util.Map;

public interface ProcurementConnector {

    String getProcurementCode();

    String getProcurementName();

    Map<String, Object> searchProducts(String keyword, Map<String, String> params);

    Map<String, Object> getProductDetail(String productId);

    Map<String, Object> createPurchaseOrder(Map<String, Object> orderParams);

    Map<String, Object> queryOrderStatus(String orderId);

    Map<String, Object> getLogisticsInfo(String orderId);

    Map<String, Object> confirmReceipt(String orderId, Map<String, Object> receiptParams);
}
