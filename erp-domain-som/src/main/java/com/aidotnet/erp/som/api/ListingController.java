package com.aidotnet.erp.som.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.som.application.ListingService;
import com.aidotnet.erp.som.application.ListingService.CreateAlertCommand;
import com.aidotnet.erp.som.application.ListingService.CreateChannelSkuCommand;
import com.aidotnet.erp.som.application.ListingService.CreateListingCommand;
import com.aidotnet.erp.som.application.ListingService.CreatePriceRuleCommand;
import com.aidotnet.erp.som.application.ListingService.ReceivePmsSuggestionCommand;
import com.aidotnet.erp.som.application.ListingService.RecordOptimizationCommand;
import com.aidotnet.erp.som.application.ListingService.RecordPerformanceCommand;
import com.aidotnet.erp.som.application.ListingService.UpdateListingCommand;
import com.aidotnet.erp.som.application.ListingService.UpdatePriceRuleCommand;
import com.aidotnet.erp.som.domain.ChannelSku;
import com.aidotnet.erp.som.domain.Listing;
import com.aidotnet.erp.som.domain.ListingOptimization;
import com.aidotnet.erp.som.domain.ListingPerformance;
import com.aidotnet.erp.som.domain.ListingStatus;
import com.aidotnet.erp.som.domain.PmsListingSuggestion;
import com.aidotnet.erp.som.domain.PriceRule;
import com.aidotnet.erp.som.domain.PriceRuleType;
import com.aidotnet.erp.som.domain.SalesAlert;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Listing管理控制器
 * <p>
 * 描述: SOM域核心REST API，提供Listing、渠道SKU、价格规则、优化记录、
 *       表现数据、PMS建议、销售告警等资源的管理接口。
 *       路径前缀: /som/api/in/v1 (内部接口)
 * </p>
 * <p>
 * 接口分组:
 *   1. Listing管理 - /listings (创建/更新/上架/下架/归档/批量状态)
 *   2. 渠道SKU管理 - /channel-skus (创建/停用/列表)
 *   3. 价格规则管理 - /price-rules (创建/更新/启停/计算)
 *   4. 优化记录 - /listings/{id}/optimizations (记录/列表)
 *   5. 表现数据 - /listings/{id}/performances (记录/列表)
 *   6. PMS建议 - /pms-suggestions (接收/应用/拒绝/列表)
 *   7. 销售告警 - /alerts (创建/确认/列表)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/som/api/in/v1")
public class ListingController {

    private final ListingService listingService;

    /**
     * 构造函数 - 依赖注入Listing服务
     *
     * @param listingService Listing管理应用服务
     */
    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping("/listings")
    public Result<Listing> createListing(@Valid @RequestBody CreateListingRequest request) {
        return Result.ok(listingService.createListing(currentTenant(), new CreateListingCommand(
                request.productId(), request.storeId(), request.title(), request.description(),
                request.price(), request.originalPrice(), request.platform(), request.marketplace(),
                request.marketplaceListingId())));
    }

    @PutMapping("/listings/{listingId}")
    public Result<Listing> updateListing(@PathVariable String listingId, @Valid @RequestBody UpdateListingRequest request) {
        return Result.ok(listingService.updateListing(currentTenant(), listingId, new UpdateListingCommand(
                request.title(), request.description(), request.price(), request.originalPrice())));
    }

    @PatchMapping("/listings/{listingId}/publish")
    public Result<Listing> publishListing(@PathVariable String listingId) {
        return Result.ok(listingService.publishListing(currentTenant(), listingId));
    }

    @PatchMapping("/listings/{listingId}/unpublish")
    public Result<Listing> unpublishListing(@PathVariable String listingId) {
        return Result.ok(listingService.unpublishListing(currentTenant(), listingId));
    }

    @PatchMapping("/listings/{listingId}/archive")
    public Result<Listing> archiveListing(@PathVariable String listingId) {
        return Result.ok(listingService.archiveListing(currentTenant(), listingId));
    }

    @PatchMapping("/listings/{listingId}/quality-score")
    public Result<Listing> updateQualityScore(@PathVariable String listingId,
                                              @Valid @RequestBody UpdateQualityScoreRequest request) {
        return Result.ok(listingService.updateQualityScore(currentTenant(), listingId, request.qualityScore()));
    }

    @PostMapping("/listings/batch/status")
    public Result<List<Listing>> batchUpdateStatus(@Valid @RequestBody BatchStatusRequest request) {
        return Result.ok(listingService.batchUpdateStatus(currentTenant(), request.listingIds(),
                ListingStatus.valueOf(request.targetStatus())));
    }

    @GetMapping("/listings")
    public Result<List<Listing>> listListings(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return Result.ok(listingService.listListingsByStatus(currentTenant(), ListingStatus.valueOf(status)));
        }
        return Result.ok(listingService.listListings(currentTenant()));
    }

    @GetMapping("/stores/{storeId}/listings")
    public Result<List<Listing>> listListingsByStore(@PathVariable String storeId) {
        return Result.ok(listingService.listListingsByStore(currentTenant(), storeId));
    }

    @GetMapping("/listings/{listingId}")
    public Result<Listing> getListing(@PathVariable String listingId) {
        return Result.ok(listingService.getListing(currentTenant(), listingId));
    }

    @GetMapping("/listings/performance")
    public Result<List<ListingPerformance>> listPerformances(@RequestParam(required = false) String listingId,
                                                             @RequestParam(required = false) String storeId) {
        if (listingId != null && !listingId.isBlank()) {
            return Result.ok(listingService.listPerformances(currentTenant(), listingId));
        }
        if (storeId != null && !storeId.isBlank()) {
            return Result.ok(listingService.listPerformancesByStore(currentTenant(), storeId));
        }
        return Result.ok(List.of());
    }

    @PostMapping("/listings/{listingId}/performance")
    public Result<ListingPerformance> recordPerformance(@PathVariable String listingId,
                                                        @Valid @RequestBody RecordPerformanceRequest request) {
        return Result.ok(listingService.recordPerformance(currentTenant(), new RecordPerformanceCommand(
                listingId, request.storeId(), request.platform(), request.marketplace(),
                request.impressions(), request.clicks(), request.ctr(), request.spend(), request.sales(),
                request.acos(), request.orders(), request.conversionRate(),
                request.periodStart(), request.periodEnd())));
    }

    @PostMapping("/channel-skus")
    public Result<ChannelSku> createChannelSku(@Valid @RequestBody CreateChannelSkuRequest request) {
        return Result.ok(listingService.createChannelSku(currentTenant(), new CreateChannelSkuCommand(
                request.productSku(), request.channel(), request.channelSku(), request.storeId(), request.marketplaceId())));
    }

    @PatchMapping("/channel-skus/{channelSkuId}/deactivate")
    public Result<ChannelSku> deactivateChannelSku(@PathVariable String channelSkuId) {
        return Result.ok(listingService.deactivateChannelSku(currentTenant(), channelSkuId));
    }

    @GetMapping("/channel-skus")
    public Result<List<ChannelSku>> listChannelSkus() {
        return Result.ok(listingService.listChannelSkus(currentTenant()));
    }

    @GetMapping("/channel-skus/by-product-sku")
    public Result<List<ChannelSku>> listChannelSkusByProductSku(@NotBlank String productSku) {
        return Result.ok(listingService.listChannelSkusByProductSku(currentTenant(), productSku));
    }

    @PostMapping("/price-rules")
    public Result<PriceRule> createPriceRule(@Valid @RequestBody CreatePriceRuleRequest request) {
        return Result.ok(listingService.createPriceRule(currentTenant(), new CreatePriceRuleCommand(
                request.name(), request.type(), request.conditions(), request.actions(),
                request.minPrice(), request.maxPrice())));
    }

    @PutMapping("/price-rules/{ruleId}")
    public Result<PriceRule> updatePriceRule(@PathVariable String ruleId, @Valid @RequestBody UpdatePriceRuleRequest request) {
        return Result.ok(listingService.updatePriceRule(currentTenant(), ruleId, new UpdatePriceRuleCommand(
                request.name(), request.type(), request.conditions(), request.actions(),
                request.minPrice(), request.maxPrice())));
    }

    @PatchMapping("/price-rules/{ruleId}/activate")
    public Result<PriceRule> activatePriceRule(@PathVariable String ruleId) {
        return Result.ok(listingService.activatePriceRule(currentTenant(), ruleId));
    }

    @PatchMapping("/price-rules/{ruleId}/deactivate")
    public Result<PriceRule> deactivatePriceRule(@PathVariable String ruleId) {
        return Result.ok(listingService.deactivatePriceRule(currentTenant(), ruleId));
    }

    @GetMapping("/price-rules")
    public Result<List<PriceRule>> listPriceRules() {
        return Result.ok(listingService.listPriceRules(currentTenant()));
    }

    @GetMapping("/price-rules/active")
    public Result<List<PriceRule>> listActivePriceRules() {
        return Result.ok(listingService.listActivePriceRules(currentTenant()));
    }

    @PostMapping("/price-rules/calculate")
    public Result<ListingService.PriceQuote> calculatePrice(@Valid @RequestBody CalculatePriceRequest request) {
        return Result.ok(listingService.calculatePriceQuote(currentTenant(), new ListingService.CalculatePriceCommand(
                request.platform(), request.marketplace(), request.procurementCost(), request.shippingFee(),
                request.otherCost(), request.commissionRate(), request.targetProfitRate())));
    }

    @PostMapping("/listings/{listingId}/optimizations")
    public Result<ListingOptimization> recordOptimization(@PathVariable String listingId,
                                                          @Valid @RequestBody RecordOptimizationRequest request) {
        return Result.ok(listingService.recordOptimization(currentTenant(), listingId,
                new RecordOptimizationCommand(request.type(), request.beforeValue(), request.afterValue(),
                        request.operator(), request.result())));
    }

    @GetMapping("/listings/{listingId}/optimizations")
    public Result<List<ListingOptimization>> listOptimizations(@PathVariable String listingId) {
        return Result.ok(listingService.listOptimizations(currentTenant(), listingId));
    }

    @PostMapping("/pms/suggestions")
    public Result<PmsListingSuggestion> receivePmsSuggestion(@Valid @RequestBody ReceivePmsSuggestionRequest request) {
        return Result.ok(listingService.receivePmsSuggestion(currentTenant(), new ReceivePmsSuggestionCommand(
                request.listingId(), request.suggestionType(), request.titleSuggestion(),
                request.descriptionSuggestion(), request.bulletPointsSuggestion(), request.priceSuggestion(),
                request.reason(), request.confidence(), request.traceId(), request.idempotencyKey())));
    }

    @PatchMapping("/pms/suggestions/{suggestionId}/apply")
    public Result<Listing> applyPmsSuggestion(@PathVariable String suggestionId) {
        return Result.ok(listingService.applyPmsSuggestion(currentTenant(), suggestionId));
    }

    @PatchMapping("/pms/suggestions/{suggestionId}/reject")
    public Result<Void> rejectPmsSuggestion(@PathVariable String suggestionId) {
        listingService.rejectPmsSuggestion(currentTenant(), suggestionId);
        return Result.ok(null);
    }

    @GetMapping("/listings/{listingId}/pms/suggestions")
    public Result<List<PmsListingSuggestion>> listPmsSuggestions(@PathVariable String listingId) {
        return Result.ok(listingService.listPmsSuggestions(currentTenant(), listingId));
    }

    @PostMapping("/alerts")
    public Result<SalesAlert> createAlert(@Valid @RequestBody CreateAlertRequest request) {
        return Result.ok(listingService.createAlert(currentTenant(), new CreateAlertCommand(
                request.storeId(), request.alertType(), request.severity(), request.message(),
                request.relatedSku(), request.metricValue(), request.thresholdValue())));
    }

    @GetMapping("/stores/{storeId}/alerts")
    public Result<List<SalesAlert>> listAlerts(@PathVariable String storeId) {
        return Result.ok(listingService.listAlerts(currentTenant(), storeId));
    }

    @GetMapping("/alerts/unacknowledged")
    public Result<List<SalesAlert>> listUnacknowledgedAlerts() {
        return Result.ok(listingService.listUnacknowledgedAlerts(currentTenant()));
    }

    @PatchMapping("/alerts/{alertId}/acknowledge")
    public Result<Void> acknowledgeAlert(@PathVariable String alertId) {
        listingService.acknowledgeAlert(currentTenant(), alertId);
        return Result.ok(null);
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateListingRequest(@NotBlank String productId, String storeId, @NotBlank String title,
                                       String description, @Positive BigDecimal price, BigDecimal originalPrice,
                                       @NotBlank String platform, String marketplace, String marketplaceListingId) {}

    public record UpdateListingRequest(String title, String description, BigDecimal price, BigDecimal originalPrice) {}

    public record UpdateQualityScoreRequest(@Positive BigDecimal qualityScore) {}

    public record BatchStatusRequest(List<String> listingIds, @NotBlank String targetStatus) {}

    public record CreateChannelSkuRequest(@NotBlank String productSku, @NotBlank String channel,
                                          @NotBlank String channelSku, String storeId, String marketplaceId) {}

    public record CreatePriceRuleRequest(@NotBlank String name, PriceRuleType type, String conditions,
                                         String actions, BigDecimal minPrice, BigDecimal maxPrice) {}

    public record UpdatePriceRuleRequest(String name, PriceRuleType type, String conditions,
                                         String actions, BigDecimal minPrice, BigDecimal maxPrice) {}

    public record CalculatePriceRequest(@NotBlank String platform, String marketplace,
                                        @Positive BigDecimal procurementCost, @Positive BigDecimal shippingFee,
                                        BigDecimal otherCost, @Positive BigDecimal commissionRate,
                                        @Positive BigDecimal targetProfitRate) {}

    public record RecordOptimizationRequest(com.aidotnet.erp.som.domain.OptimizationType type,
                                            String beforeValue, String afterValue, String operator, String result) {}

    public record RecordPerformanceRequest(String storeId, String platform, String marketplace,
                                           int impressions, int clicks, BigDecimal ctr, BigDecimal spend,
                                           BigDecimal sales, BigDecimal acos, int orders, BigDecimal conversionRate,
                                           Instant periodStart, Instant periodEnd) {}

    public record ReceivePmsSuggestionRequest(String listingId, @NotBlank String suggestionType,
                                              String titleSuggestion, String descriptionSuggestion,
                                              String bulletPointsSuggestion, BigDecimal priceSuggestion,
                                              String reason, String confidence, String traceId,
                                              @NotBlank String idempotencyKey) {}

    public record CreateAlertRequest(@NotBlank String storeId, @NotBlank String alertType,
                                     @NotBlank String severity, @NotBlank String message,
                                     String relatedSku, BigDecimal metricValue, BigDecimal thresholdValue) {}
}
