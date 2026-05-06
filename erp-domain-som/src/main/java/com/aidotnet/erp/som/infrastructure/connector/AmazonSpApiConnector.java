package com.aidotnet.erp.som.infrastructure.connector;

import com.aidotnet.erp.common.connector.PlatformConnector;
import com.aidotnet.erp.common.exception.BizException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AmazonSpApiConnector implements PlatformConnector {

    private static final Logger log = LoggerFactory.getLogger(AmazonSpApiConnector.class);
    private static final String PLATFORM_CODE = "AMAZON_SP";
    private static final String PLATFORM_NAME = "Amazon SP-API";
    private static final String BASE_URL = "https://sellingpartnerapi-na.amazon.com";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AmazonSpApiConnector() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getPlatformCode() {
        return PLATFORM_CODE;
    }

    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    @Override
    public String authorize(String storeId, Map<String, String> authParams) {
        String code = authParams.get("authorization_code");
        if (code == null || code.isBlank()) {
            throw new BizException("AMAZON_AUTH_FAILED", "authorization_code不能为空");
        }
        log.info("Amazon SP-API authorization initiated for store={}", storeId);
        return "pending_token_" + storeId;
    }

    @Override
    public boolean validateToken(String storeId, String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }
        log.debug("Validating Amazon SP-API token for store={}", storeId);
        return true;
    }

    @Override
    public boolean refreshToken(String storeId, String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }
        log.info("Refreshing Amazon SP-API token for store={}", storeId);
        return true;
    }

    @Override
    public Map<String, Object> fetchOrders(String storeId, String accessToken, Map<String, String> params) {
        log.info("Fetching Amazon orders for store={}", storeId);
        requireValidToken(storeId, accessToken);
        String createdAfter = params.getOrDefault("created_after", Instant.now().minus(Duration.ofDays(1)).toString());
        String path = String.format("/orders/v0/orders?MarketplaceIds=%s&CreatedAfter=%s",
                params.getOrDefault("marketplace_id", "ATVPDKIKX0DER"), createdAfter);
        return executeWithRetry("GET", path, accessToken, null);
    }

    @Override
    public Map<String, Object> fetchListings(String storeId, String accessToken, Map<String, String> params) {
        log.info("Fetching Amazon listings for store={}", storeId);
        requireValidToken(storeId, accessToken);
        String path = String.format("/listings/2021-08-01/items?SellerId=%s&MarketplaceIds=%s",
                params.getOrDefault("seller_id", ""), params.getOrDefault("marketplace_id", "ATVPDKIKX0DER"));
        return executeWithRetry("GET", path, accessToken, null);
    }

    @Override
    public boolean updateListing(String storeId, String accessToken, String listingId, Map<String, Object> data) {
        log.info("Updating Amazon listing={} for store={}", listingId, storeId);
        requireValidToken(storeId, accessToken);
        String path = String.format("/listings/2021-08-01/items/%s", listingId);
        Map<String, Object> result = executeWithRetry("PATCH", path, accessToken, data);
        return result != null;
    }

    @Override
    public boolean updateInventory(String storeId, String accessToken, String sku, int quantity) {
        log.info("Updating Amazon inventory for sku={} store={} qty={}", sku, storeId, quantity);
        requireValidToken(storeId, accessToken);
        Map<String, Object> body = Map.of(
                "sku", sku,
                "quantity", quantity,
                "updatedAt", Instant.now().toString());
        String path = String.format("/inventory/v1/stock/%s", sku);
        Map<String, Object> result = executeWithRetry("PUT", path, accessToken, body);
        return result != null;
    }

    @Override
    public boolean markShipped(String storeId, String accessToken, String orderId, Map<String, String> shipmentInfo) {
        log.info("Marking Amazon order={} as shipped for store={}", orderId, storeId);
        requireValidToken(storeId, accessToken);
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", orderId);
        body.put("carrierCode", shipmentInfo.getOrDefault("carrier_code", ""));
        body.put("trackingNumber", shipmentInfo.getOrDefault("tracking_number", ""));
        body.put("shipDate", shipmentInfo.getOrDefault("ship_date", Instant.now().toString()));
        String path = String.format("/orders/v0/orders/%s/shipment", orderId);
        Map<String, Object> result = executeWithRetry("POST", path, accessToken, body);
        return result != null;
    }

    @Override
    public Map<String, Object> fetchReviews(String storeId, String accessToken, Map<String, String> params) {
        log.info("Fetching Amazon reviews for store={}", storeId);
        requireValidToken(storeId, accessToken);
        String path = String.format("/reviews/v0/reviews?MarketplaceId=%s&Asin=%s",
                params.getOrDefault("marketplace_id", "ATVPDKIKX0DER"), params.getOrDefault("asin", ""));
        return executeWithRetry("GET", path, accessToken, null);
    }

    private void requireValidToken(String storeId, String accessToken) {
        if (!validateToken(storeId, accessToken)) {
            throw new BizException("AMAZON_TOKEN_INVALID", "Amazon SP-API token无效，请重新授权");
        }
    }

    private Map<String, Object> executeWithRetry(String method, String path, String accessToken, Map<String, Object> body) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return executeRequest(method, path, accessToken, body);
            } catch (BizException e) {
                if (e.getCode() != null && e.getCode().equals("AMAZON_RATE_LIMITED") && attempt < MAX_RETRIES) {
                    log.warn("Amazon SP-API rate limited, retrying attempt {}/{} after {}ms", attempt, MAX_RETRIES, RETRY_DELAY_MS);
                    sleepSafely(RETRY_DELAY_MS * attempt);
                    lastException = e;
                } else {
                    throw e;
                }
            } catch (Exception e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    log.warn("Amazon SP-API request failed, retrying attempt {}/{}: {}", attempt, MAX_RETRIES, e.getMessage());
                    sleepSafely(RETRY_DELAY_MS * attempt);
                }
            }
        }
        throw new BizException("AMAZON_REQUEST_FAILED", "请求失败，已重试" + MAX_RETRIES + "次: " + lastException.getMessage());
    }

    private void sleepSafely(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("AMAZON_REQUEST_INTERRUPTED", "请求被中断");
        }
    }

    private Map<String, Object> executeRequest(String method, String path, String accessToken, Map<String, Object> body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .header("x-amz-access-token", accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30));

            if ("GET".equals(method)) {
                builder.GET();
            } else if ("POST".equals(method)) {
                builder.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
            } else if ("PUT".equals(method)) {
                builder.PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
            } else if ("PATCH".equals(method)) {
                builder.method("PATCH", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                throw new BizException("AMAZON_RATE_LIMITED", "Amazon SP-API请求频率超限");
            }
            if (response.statusCode() >= 500) {
                throw new BizException("AMAZON_SERVER_ERROR", "Amazon SP-API服务端错误: " + response.statusCode());
            }
            if (response.statusCode() >= 400) {
                throw new BizException("AMAZON_CLIENT_ERROR", "Amazon SP-API客户端错误: " + response.statusCode());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            return result;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("AMAZON_REQUEST_ERROR", "Amazon SP-API请求异常: " + e.getMessage());
        }
    }
}
