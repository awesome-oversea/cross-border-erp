package com.aidotnet.erp.common.connector;

import java.util.Map;

public interface CarrierConnector {

    String getCarrierCode();

    String getCarrierName();

    String createShipment(String storeId, String accessToken, Map<String, Object> shipmentParams);

    Map<String, Object> getTrackingInfo(String trackingNumber);

    Map<String, Object> estimateFreight(Map<String, Object> freightParams);

    boolean cancelShipment(String shipmentId);

    String getLabelUrl(String shipmentId);
}
