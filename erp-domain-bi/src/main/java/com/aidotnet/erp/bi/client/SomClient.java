package com.aidotnet.erp.bi.client;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * SOM 客户端
 * <p>
 * 为 BI 提供 Listing、运营告警、买盒与跟卖监控等只读信号。
 * </p>
 */
@FeignClient(name = "erp-app", contextId = "bi-som-client", path = "/som/api/in/v1")
public interface SomClient {

    @GetMapping("/listings")
    Result<List<ListingResponse>> listListings(@RequestParam(required = false) String status);

    @GetMapping("/listings/performance")
    Result<List<ListingPerformanceResponse>> listPerformances(@RequestParam(required = false) String listingId,
                                                              @RequestParam(required = false) String storeId);

    @GetMapping("/channel-skus/by-product-sku")
    Result<List<ChannelSkuResponse>> listChannelSkusByProductSku(@RequestParam String productSku);

    @GetMapping("/alerts/unacknowledged")
    Result<List<SalesAlertResponse>> listUnacknowledgedAlerts();

    @GetMapping("/buybox-monitors")
    Result<List<BuyboxMonitorResponse>> listBuyboxMonitors(@RequestParam(required = false) String listingId);

    @GetMapping("/hijack-alerts")
    Result<List<HijackAlertResponse>> listHijackAlerts(@RequestParam(required = false) String listingId,
                                                       @RequestParam(defaultValue = "true") boolean includeResolved);

    record ListingResponse(String listingId, String tenantId, String productId, String storeId,
                           String title, String description, BigDecimal price, BigDecimal originalPrice,
                           String platform, String marketplace, String marketplaceListingId,
                           BigDecimal qualityScore, String status, Instant createdAt, Instant updatedAt,
                           boolean active) {}

    record ListingPerformanceResponse(String performanceId, String tenantId, String listingId,
                                      String storeId, String platform, String marketplace,
                                      int impressions, int clicks, BigDecimal ctr,
                                      BigDecimal spend, BigDecimal sales, BigDecimal acos,
                                      int orders, BigDecimal conversionRate,
                                      Instant periodStart, Instant periodEnd, Instant createdAt) {}

    record ChannelSkuResponse(String channelSkuId, String tenantId, String productSku, String channel,
                              String channelSku, String storeId, String marketplaceId,
                              String status, Instant createdAt, Instant updatedAt) {}

    record SalesAlertResponse(String alertId, String tenantId, String storeId, String alertType,
                              String severity, String message, String relatedSku,
                              BigDecimal metricValue, BigDecimal thresholdValue,
                              boolean acknowledged, Instant createdAt) {}

    record BuyboxMonitorResponse(String monitorId, String tenantId, String listingId, String platform,
                                 String marketplace, String winnerName, BigDecimal winnerPrice,
                                 BigDecimal ourPrice, boolean priceAdjustSuggested, BigDecimal suggestedPrice,
                                 int hijackerCount, Instant collectedAt, Instant createdAt) {}

    record HijackAlertResponse(String alertId, String tenantId, String listingId, String platform,
                               String marketplace, String hijackerName, BigDecimal hijackerPrice,
                               BigDecimal ourPrice, String severity, String status, String handledBy,
                               String handleNote, Instant detectedAt, Instant handledAt,
                               Instant createdAt, Instant updatedAt) {}
}
