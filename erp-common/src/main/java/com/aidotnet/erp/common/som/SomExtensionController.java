package com.aidotnet.erp.common.som;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/som/api/v1")
public class SomExtensionController {

    private final SomExtensionService somExtService;

    public SomExtensionController(SomExtensionService somExtService) {
        this.somExtService = somExtService;
    }

    @PostMapping("/listings")
    public Result<SomExtensionService.Listing> createListing(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attrs = (Map<String, Object>) request.getOrDefault("attributes", Map.of());
        return Result.ok(somExtService.createListing(
                (String) request.get("tenantId"),
                (String) request.get("storeId"),
                (String) request.get("platform"),
                (String) request.get("channelSku"),
                (String) request.get("internalSku"),
                (String) request.get("title"),
                new BigDecimal(request.get("price").toString()),
                (String) request.getOrDefault("currency", "USD"),
                (String) request.getOrDefault("status", "DRAFT"),
                attrs
        ));
    }

    @PatchMapping("/listings/{listingId}")
    public Result<SomExtensionService.Listing> updateListing(@PathVariable String listingId,
                                                               @RequestBody Map<String, Object> request) {
        BigDecimal price = request.containsKey("price") ? new BigDecimal(request.get("price").toString()) : null;
        String status = (String) request.get("status");
        return Result.ok(somExtService.updateListing(listingId, price, status, null));
    }

    @GetMapping("/listings")
    public Result<List<SomExtensionService.Listing>> listListings(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String platform) {
        return Result.ok(somExtService.listListings(tenantId, storeId, platform));
    }

    @GetMapping("/listings/by-channel-sku")
    public Result<SomExtensionService.Listing> getListingByChannelSku(
            @RequestParam String tenantId, @RequestParam String platform, @RequestParam String channelSku) {
        return Result.ok(somExtService.getListingByChannelSku(tenantId, platform, channelSku));
    }

    @PostMapping("/channel-sku-mappings")
    public Result<SomExtensionService.ChannelSkuMapping> createChannelSkuMapping(@RequestBody Map<String, Object> request) {
        return Result.ok(somExtService.createChannelSkuMapping(
                (String) request.get("tenantId"),
                (String) request.get("platform"),
                (String) request.get("channelSku"),
                (String) request.get("internalSku"),
                (String) request.getOrDefault("channelAsin", ""),
                (String) request.getOrDefault("productName", "")
        ));
    }

    @PatchMapping("/channel-sku-mappings/{mappingId}")
    public Result<SomExtensionService.ChannelSkuMapping> updateChannelSkuMapping(
            @PathVariable String mappingId, @RequestBody Map<String, Object> request) {
        String internalSku = (String) request.get("internalSku");
        Boolean active = (Boolean) request.getOrDefault("active", true);
        return Result.ok(somExtService.updateChannelSkuMapping(mappingId, internalSku, active));
    }

    @GetMapping("/channel-sku-mappings")
    public Result<List<SomExtensionService.ChannelSkuMapping>> listChannelSkuMappings(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String platform) {
        return Result.ok(somExtService.listChannelSkuMappings(tenantId, platform));
    }

    @GetMapping("/channel-sku-mappings/resolve")
    public Result<SomExtensionService.ChannelSkuMapping> resolveInternalSku(
            @RequestParam String tenantId, @RequestParam String platform, @RequestParam String channelSku) {
        return Result.ok(somExtService.resolveInternalSku(tenantId, platform, channelSku));
    }

    @PostMapping("/price-rules")
    public Result<SomExtensionService.PriceRule> createPriceRule(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> conditions = (Map<String, Object>) request.getOrDefault("conditions", Map.of());
        return Result.ok(somExtService.createPriceRule(
                (String) request.get("tenantId"),
                (String) request.get("ruleName"),
                (String) request.getOrDefault("ruleType", "MARKUP"),
                (String) request.get("platform"),
                (String) request.getOrDefault("marketplaceId", ""),
                request.containsKey("basePrice") ? new BigDecimal(request.get("basePrice").toString()) : BigDecimal.ZERO,
                request.containsKey("minPrice") ? new BigDecimal(request.get("minPrice").toString()) : null,
                request.containsKey("maxPrice") ? new BigDecimal(request.get("maxPrice").toString()) : null,
                conditions
        ));
    }

    @PostMapping("/price-rules/{ruleId}/calculate")
    public Result<SomExtensionService.PriceCalculationResult> calculatePrice(
            @PathVariable String ruleId, @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> factors = (Map<String, BigDecimal>) request.getOrDefault("factors", Map.of());
        return Result.ok(somExtService.calculatePrice(
                ruleId, new BigDecimal(request.get("costPrice").toString()), factors));
    }

    @GetMapping("/price-rules")
    public Result<List<SomExtensionService.PriceRule>> listPriceRules(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String platform) {
        return Result.ok(somExtService.listPriceRules(tenantId, platform));
    }
}
