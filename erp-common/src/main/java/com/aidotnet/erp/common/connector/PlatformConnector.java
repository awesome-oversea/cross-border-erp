package com.aidotnet.erp.common.connector;

import java.util.Map;

public interface PlatformConnector {

    String getPlatformCode();

    String getPlatformName();

    String authorize(String storeId, Map<String, String> authParams);

    boolean validateToken(String storeId, String accessToken);

    boolean refreshToken(String storeId, String refreshToken);

    Map<String, Object> fetchOrders(String storeId, String accessToken, Map<String, String> params);

    Map<String, Object> fetchListings(String storeId, String accessToken, Map<String, String> params);

    boolean updateListing(String storeId, String accessToken, String listingId, Map<String, Object> data);

    boolean updateInventory(String storeId, String accessToken, String sku, int quantity);

    boolean markShipped(String storeId, String accessToken, String orderId, Map<String, String> shipmentInfo);

    Map<String, Object> fetchReviews(String storeId, String accessToken, Map<String, String> params);
}
