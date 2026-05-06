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
public class TikTokConnector implements PlatformConnector {

    private static final Logger log = LoggerFactory.getLogger(TikTokConnector.class);
    private static final String PLATFORM_CODE = "TIKTOK_SHOP";
    private static final String PLATFORM_NAME = "TikTok Shop";

    @Override
    public String getPlatformCode() { return PLATFORM_CODE; }

    @Override
    public String getPlatformName() { return PLATFORM_NAME; }

    @Override
    public String authorize(String storeId, Map<String, String> authParams) {
        String code = authParams.get("authorization_code");
        if (code == null || code.isBlank()) {
            throw new BizException("TIKTOK_AUTH_FAILED", "authorization_code不能为空");
        }
        log.info("TikTok Shop authorization initiated for store={}", storeId);
        return "tiktok_token_" + storeId;
    }

    @Override
    public boolean validateToken(String storeId, String accessToken) {
        return accessToken != null && !accessToken.isBlank();
    }

    @Override
    public boolean refreshToken(String storeId, String refreshToken) {
        log.info("Refreshing TikTok Shop token for store={}", storeId);
        return refreshToken != null && !refreshToken.isBlank();
    }

    @Override
    public Map<String, Object> fetchOrders(String storeId, String accessToken, Map<String, String> params) {
        log.info("Fetching TikTok Shop orders for store={}", storeId);
        requireValidToken(storeId, accessToken);
        Map<String, Object> result = new HashMap<>();
        result.put("platform", PLATFORM_CODE);
        result.put("storeId", storeId);
        result.put("orders", java.util.List.of());
        return result;
    }

    @Override
    public Map<String, Object> fetchListings(String storeId, String accessToken, Map<String, String> params) {
        log.info("Fetching TikTok Shop products for store={}", storeId);
        requireValidToken(storeId, accessToken);
        Map<String, Object> result = new HashMap<>();
        result.put("platform", PLATFORM_CODE);
        result.put("storeId", storeId);
        result.put("products", java.util.List.of());
        return result;
    }

    @Override
    public boolean updateListing(String storeId, String accessToken, String listingId, Map<String, Object> data) {
        log.info("Updating TikTok Shop product={} for store={}", listingId, storeId);
        requireValidToken(storeId, accessToken);
        return true;
    }

    @Override
    public boolean updateInventory(String storeId, String accessToken, String sku, int quantity) {
        log.info("Updating TikTok Shop inventory for sku={} store={}", sku, storeId);
        requireValidToken(storeId, accessToken);
        return true;
    }

    @Override
    public boolean markShipped(String storeId, String accessToken, String orderId, Map<String, String> shipmentInfo) {
        log.info("Shipping TikTok Shop order={} for store={}", orderId, storeId);
        requireValidToken(storeId, accessToken);
        return true;
    }

    @Override
    public Map<String, Object> fetchReviews(String storeId, String accessToken, Map<String, String> params) {
        log.info("Fetching TikTok Shop reviews for store={}", storeId);
        requireValidToken(storeId, accessToken);
        Map<String, Object> result = new HashMap<>();
        result.put("platform", PLATFORM_CODE);
        result.put("storeId", storeId);
        result.put("reviews", java.util.List.of());
        return result;
    }

    private void requireValidToken(String storeId, String accessToken) {
        if (!validateToken(storeId, accessToken)) {
            throw new BizException("TIKTOK_TOKEN_INVALID", "TikTok Shop token无效，请重新授权");
        }
    }
}
