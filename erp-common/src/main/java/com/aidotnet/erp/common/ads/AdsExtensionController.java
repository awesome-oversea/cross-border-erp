package com.aidotnet.erp.common.ads;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ads/api/v1")
public class AdsExtensionController {

    private final AdsExtensionService adsExtService;

    public AdsExtensionController(AdsExtensionService adsExtService) {
        this.adsExtService = adsExtService;
    }

    @PostMapping("/ad-groups")
    public Result<AdsExtensionService.AdGroup> createAdGroup(@RequestBody Map<String, Object> request) {
        BigDecimal dailyBudget = request.containsKey("dailyBudget") ? new BigDecimal(request.get("dailyBudget").toString()) : BigDecimal.ZERO;
        return Result.ok(adsExtService.createAdGroup(
                (String) request.get("tenantId"),
                (String) request.getOrDefault("campaignId", ""),
                (String) request.get("groupName"),
                (String) request.getOrDefault("targetingType", "MANUAL"),
                dailyBudget,
                (String) request.getOrDefault("status", "ENABLED")
        ));
    }

    @PutMapping("/ad-groups/{groupId}")
    public Result<AdsExtensionService.AdGroup> updateAdGroup(
            @PathVariable String groupId, @RequestBody Map<String, Object> request) {
        BigDecimal dailyBudget = request.containsKey("dailyBudget") ? new BigDecimal(request.get("dailyBudget").toString()) : null;
        return Result.ok(adsExtService.updateAdGroup(groupId, dailyBudget, (String) request.get("status")));
    }

    @GetMapping("/ad-groups")
    public Result<List<AdsExtensionService.AdGroup>> listAdGroups(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String campaignId) {
        return Result.ok(adsExtService.listAdGroups(tenantId, campaignId));
    }

    @PostMapping("/keywords")
    public Result<AdsExtensionService.Keyword> createKeyword(@RequestBody Map<String, Object> request) {
        BigDecimal bid = request.containsKey("bid") ? new BigDecimal(request.get("bid").toString()) : BigDecimal.ZERO;
        return Result.ok(adsExtService.createKeyword(
                (String) request.get("tenantId"),
                (String) request.get("adGroupId"),
                (String) request.get("keywordText"),
                (String) request.getOrDefault("matchType", "BROAD"),
                bid,
                (String) request.getOrDefault("status", "ENABLED")
        ));
    }

    @PatchMapping("/keywords/{keywordId}/bid")
    public Result<AdsExtensionService.Keyword> updateKeywordBid(
            @PathVariable String keywordId, @RequestBody Map<String, Object> request) {
        BigDecimal bid = request.containsKey("bid") ? new BigDecimal(request.get("bid").toString()) : null;
        return Result.ok(adsExtService.updateKeywordBid(keywordId, bid, (String) request.get("status")));
    }

    @GetMapping("/keywords")
    public Result<List<AdsExtensionService.Keyword>> listKeywords(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String adGroupId) {
        return Result.ok(adsExtService.listKeywords(tenantId, adGroupId));
    }

    @PostMapping("/bidding-strategies")
    public Result<AdsExtensionService.BiddingStrategy> createBiddingStrategy(@RequestBody Map<String, Object> request) {
        BigDecimal targetValue = request.containsKey("targetValue") ? new BigDecimal(request.get("targetValue").toString()) : BigDecimal.ZERO;
        BigDecimal maxBid = request.containsKey("maxBid") ? new BigDecimal(request.get("maxBid").toString()) : null;
        BigDecimal minBid = request.containsKey("minBid") ? new BigDecimal(request.get("minBid").toString()) : null;
        return Result.ok(adsExtService.createBiddingStrategy(
                (String) request.get("tenantId"),
                (String) request.get("strategyName"),
                (String) request.getOrDefault("strategyType", "DYNAMIC"),
                (String) request.getOrDefault("targetMetric", "ACOS"),
                targetValue, maxBid, minBid
        ));
    }

    @GetMapping("/bidding-strategies")
    public Result<List<AdsExtensionService.BiddingStrategy>> listBiddingStrategies(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String strategyType) {
        return Result.ok(adsExtService.listBiddingStrategies(tenantId, strategyType));
    }

    @PostMapping("/performance-reports")
    public Result<AdsExtensionService.PerformanceReport> generatePerformanceReport(@RequestBody Map<String, Object> request) {
        return Result.ok(adsExtService.generatePerformanceReport(
                (String) request.get("tenantId"),
                (String) request.getOrDefault("adGroupId", ""),
                (String) request.getOrDefault("dateRange", "LAST_7_DAYS"),
                new BigDecimal(request.getOrDefault("impressions", "0").toString()),
                new BigDecimal(request.getOrDefault("clicks", "0").toString()),
                new BigDecimal(request.getOrDefault("spend", "0").toString()),
                new BigDecimal(request.getOrDefault("revenue", "0").toString()),
                new BigDecimal(request.getOrDefault("orders", "0").toString())
        ));
    }

    @GetMapping("/performance-reports")
    public Result<List<AdsExtensionService.PerformanceReport>> listPerformanceReports(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String adGroupId) {
        return Result.ok(adsExtService.listPerformanceReports(tenantId, adGroupId));
    }

    @PostMapping("/search-terms")
    public Result<AdsExtensionService.SearchTerm> recordSearchTerm(@RequestBody Map<String, Object> request) {
        return Result.ok(adsExtService.recordSearchTerm(
                (String) request.get("tenantId"),
                (String) request.getOrDefault("adGroupId", ""),
                (String) request.get("searchTerm"),
                new BigDecimal(request.getOrDefault("impressions", "0").toString()),
                new BigDecimal(request.getOrDefault("clicks", "0").toString()),
                new BigDecimal(request.getOrDefault("spend", "0").toString()),
                new BigDecimal(request.getOrDefault("revenue", "0").toString()),
                ((Number) request.getOrDefault("orders", 0)).intValue()
        ));
    }

    @GetMapping("/search-terms")
    public Result<List<AdsExtensionService.SearchTerm>> listSearchTerms(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String adGroupId) {
        return Result.ok(adsExtService.listSearchTerms(tenantId, adGroupId));
    }
}
