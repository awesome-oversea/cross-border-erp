package com.aidotnet.erp.common.connector;

import java.util.Map;

public interface PmsSmartConnector {

    String getPmsCode();

    String getPmsName();

    Map<String, Object> analyzeProduct(String tenantId, String sku, Map<String, Object> context);

    Map<String, Object> generateListing(String tenantId, Map<String, Object> productData, String targetPlatform);

    Map<String, Object> optimizePrice(String tenantId, String sku, Map<String, Object> marketData);

    Map<String, Object> forecastDemand(String tenantId, String sku, Map<String, String> params);

    Map<String, Object> detectAnomaly(String tenantId, String sku, Map<String, Object> metrics);
}
