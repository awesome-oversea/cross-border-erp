package com.aidotnet.erp.som.infrastructure.connector;

import com.aidotnet.erp.common.connector.PlatformConnector;
import com.aidotnet.erp.common.exception.BizException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShopifyConnector implements PlatformConnector {

    private static final Logger log = LoggerFactory.getLogger(ShopifyConnector.class);
    private static final String PLATFORM_CODE = "SHOPIFY";
    private static final String PLATFORM_NAME = "Shopify";

    @Override
    public String getPlatformCode() { return PLATFORM_CODE; }

    @Override
    public String getPlatformName() { return PLATFORM_NAME; }

    @Override
    public String authorize(String storeId, Map<String, String> authParams) {
        String shopDomain = authParams.get("shop_domain");
        String code = authParams.get("authorization_code");
        if (shopDomain == null || code == null) {
            throw new BizException("SHOPIFY_AUTH_FAILED", "shop_domain和authorization_code不能为空");
        }
        log.info("Shopify authorization initiated for store={}", storeId);
        return "shopify_token_" + storeId;
    }

    @Override
    public boolean validateToken(String storeId, String accessToken) {
        return accessToken != null && accessToken.startsWith("shpat_");
    }

    @Override
    public boolean refreshToken(String storeId, String refreshToken) {
        log.info("Shopify tokens do not expire for store={}", storeId);
        return true;
    }

    @Override
    public Map<String, Object> fetchOrders(String storeId, String accessToken, Map<String, String> params) {
        log.info("Fetching Shopify orders for store={}", storeId);
        requireValidToken(storeId, accessToken);
        Map<String, Object> result = new HashMap<>();
        result.put("platform", PLATFORM_CODE);
        result.put("storeId", storeId);
        result.put("orders", java.util.List.of());
        return result;
    }

    @Override
    public Map<String, Object> fetchListings(String storeId, String accessToken, Map<String, String> params) {
        log.info("Fetching Shopify products for store={}", storeId);
        requireValidToken(storeId, accessToken);
        Map<String, Object> result = new HashMap<>();
        result.put("platform", PLATFORM_CODE);
        result.put("storeId", storeId);
        result.put("products", java.util.List.of());
        return result;
    }

    @Override
    public boolean updateListing(String storeId, String accessToken, String listingId, Map<String, Object> data) {
        log.info("Updating Shopify product={} for store={}", listingId, storeId);
        requireValidToken(storeId, accessToken);
        return true;
    }

    @Override
    public boolean updateInventory(String storeId, String accessToken, String sku, int quantity) {
        log.info("Updating Shopify inventory for sku={} store={}", sku, storeId);
        requireValidToken(storeId, accessToken);
        return true;
    }

    @Override
    public boolean markShipped(String storeId, String accessToken, String orderId, Map<String, String> shipmentInfo) {
        log.info("Fulfilling Shopify order={} for store={}", orderId, storeId);
        requireValidToken(storeId, accessToken);
        return true;
    }

    @Override
    public Map<String, Object> fetchReviews(String storeId, String accessToken, Map<String, String> params) {
        log.info("Shopify does not have native reviews API for store={}", storeId);
        Map<String, Object> result = new HashMap<>();
        result.put("platform", PLATFORM_CODE);
        result.put("storeId", storeId);
        result.put("reviews", java.util.List.of());
        return result;
    }

    private void requireValidToken(String storeId, String accessToken) {
        if (!validateToken(storeId, accessToken)) {
            throw new BizException("SHOPIFY_TOKEN_INVALID", "Shopify token无效，请重新授权");
        }
    }
}
