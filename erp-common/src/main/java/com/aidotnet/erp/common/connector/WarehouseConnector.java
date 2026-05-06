package com.aidotnet.erp.common.connector;

import java.util.Map;

public interface WarehouseConnector {

    String getWarehouseCode();

    String getWarehouseName();

    Map<String, Object> createInboundOrder(Map<String, Object> orderParams);

    Map<String, Object> queryInventory(String sku, String warehouseId);

    boolean createOutboundOrder(String orderId, Map<String, Object> outboundParams);

    Map<String, Object> getInboundStatus(String inboundOrderId);
}
