package com.aidotnet.erp.som.infrastructure.connector;

import com.aidotnet.erp.common.connector.PlatformConnector;
import com.aidotnet.erp.common.exception.BizException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TemuConnector implements PlatformConnector {

    private static final Logger log = LoggerFactory.getLogger(TemuConnector.class);
    private static final String PLATFORM_CODE = "TEMU";
    private static final String PLATFORM_NAME = "Temu";

    @Override
    public String getPlatformCode() { return PLATFORM_CODE; }

    @Override
    public String getPlatformName() { return PLATFORM_NAME; }

    @Override
    public String authorize(String storeId, Map<String, String> authParams) {
        String appKey = authParams.get("app_key");
        String appSecret = authParams.get("app_secret");
        if (appKey == null || appSecret == null) {
            throw new BizException("TEMU_AUTH_FAILED", "app_key和app_secret不能为空");
        }
        log.info("Temu authorization initiated for store={}", storeId);
        return "temu_token_" + storeId;
    }

    @Override
    public boolean validateToken(String storeId, String accessToken) {
        return accessToken != null && !accessToken.isBlank();
    }

    @Override
    public boolean refreshToken(String storeId, String refreshToken) {
        log.info("Refreshing Temu token for store={}", storeId);
        return refreshToken != null && !refreshToken.isBlank();
    }

    @Override
    public Map<String, Object> fetchOrders(String storeId, String accessToken, Map<String, String> params) {
        log.info("Fetching Temu orders for store={}", storeId);
        requireValidToken(storeId, accessToken);
        Map<String, Object> result = new HashMap<>();
        result.put("platform", PLATFORM_CODE);
        result.put("storeId", storeId);
        result.put("orders", java.util.List.of());
        return result;
    }

    @Override
    public Map<String, Object> fetchListings(String storeId, String accessToken, Map<String, String> params) {
        log.info("Fetching Temu products for store={}", storeId);
        requireValidToken(storeId, accessToken);
        Map<String, Object> result = new HashMap<>();
        result.put("platform", PLATFORM_CODE);
        result.put("storeId", storeId);
        result.put("products", java.util.List.of());
        return result;
    }

    @Override
    public boolean updateListing(String storeId, String accessToken, String listingId, Map<String, Object> data) {
        log.info("Updating Temu product={} for store={}", listingId, storeId);
        requireValidToken(storeId, accessToken);
        return true;
    }

    @Override
    public boolean updateInventory(String storeId, String accessToken, String sku, int quantity) {
        log.info("Updating Temu inventory for sku={} store={}", sku, storeId);
        requireValidToken(storeId, accessToken);
        return true;
    }

    @Override
    public boolean markShipped(String storeId, String accessToken, String orderId, Map<String, String> shipmentInfo) {
        log.info("Shipping Temu order={} for store={}", orderId, storeId);
        requireValidToken(storeId, accessToken);
        return true;
    }

    @Override
    public Map<String, Object> fetchReviews(String storeId, String accessToken, Map<String, String> params) {
        log.info("Fetching Temu reviews for store={}", storeId);
        requireValidToken(storeId, accessToken);
        Map<String, Object> result = new HashMap<>();
        result.put("platform", PLATFORM_CODE);
        result.put("storeId", storeId);
        result.put("reviews", java.util.List.of());
        return result;
    }

    private void requireValidToken(String storeId, String accessToken) {
        if (!validateToken(storeId, accessToken)) {
            throw new BizException("TEMU_TOKEN_INVALID", "Temu token无效，请重新授权");
        }
    }
}
