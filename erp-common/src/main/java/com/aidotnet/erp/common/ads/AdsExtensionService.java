package com.aidotnet.erp.common.ads;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AdsExtensionService {

    private static final Logger log = LoggerFactory.getLogger(AdsExtensionService.class);
    private final Map<String, AdGroup> adGroups = new ConcurrentHashMap<>();
    private final Map<String, Keyword> keywords = new ConcurrentHashMap<>();
    private final Map<String, BiddingStrategy> biddingStrategies = new ConcurrentHashMap<>();
    private final Map<String, PerformanceReport> performanceReports = new ConcurrentHashMap<>();
    private final Map<String, SearchTerm> searchTerms = new ConcurrentHashMap<>();

    public AdGroup createAdGroup(String tenantId, String campaignId, String groupName,
                                  String targetingType, BigDecimal dailyBudget, String status) {
        String groupId = "ADG-" + System.currentTimeMillis();
        AdGroup group = new AdGroup(groupId, tenantId, campaignId, groupName,
                targetingType, dailyBudget, status, Instant.now(), Instant.now());
        adGroups.put(groupId, group);
        log.info("Created ad group: id={}, name={}, campaign={}", groupId, groupName, campaignId);
        return group;
    }

    public AdGroup updateAdGroup(String groupId, BigDecimal dailyBudget, String status) {
        AdGroup existing = adGroups.get(groupId);
        if (existing == null) throw new IllegalArgumentException("Ad group not found: " + groupId);
        AdGroup updated = new AdGroup(groupId, existing.tenantId(), existing.campaignId(),
                existing.groupName(), existing.targetingType(),
                dailyBudget != null ? dailyBudget : existing.dailyBudget(),
                status != null ? status : existing.status(),
                existing.createdAt(), Instant.now());
        adGroups.put(groupId, updated);
        log.info("Updated ad group: id={}", groupId);
        return updated;
    }

    public List<AdGroup> listAdGroups(String tenantId, String campaignId) {
        return adGroups.values().stream()
                .filter(g -> tenantId == null || tenantId.equals(g.tenantId()))
                .filter(g -> campaignId == null || campaignId.equals(g.campaignId()))
                .toList();
    }

    public Keyword createKeyword(String tenantId, String adGroupId, String keywordText,
                                  String matchType, BigDecimal bid, String status) {
        String keywordId = "KW-" + System.currentTimeMillis();
        Keyword kw = new Keyword(keywordId, tenantId, adGroupId, keywordText,
                matchType, bid, status, Instant.now());
        keywords.put(keywordId, kw);
        log.info("Created keyword: id={}, text={}, matchType={}", keywordId, keywordText, matchType);
        return kw;
    }

    public Keyword updateKeywordBid(String keywordId, BigDecimal bid, String status) {
        Keyword existing = keywords.get(keywordId);
        if (existing == null) throw new IllegalArgumentException("Keyword not found: " + keywordId);
        Keyword updated = new Keyword(keywordId, existing.tenantId(), existing.adGroupId(),
                existing.keywordText(), existing.matchType(),
                bid != null ? bid : existing.bid(),
                status != null ? status : existing.status(),
                existing.createdAt());
        keywords.put(keywordId, updated);
        log.info("Updated keyword bid: id={}, bid={}", keywordId, bid);
        return updated;
    }

    public List<Keyword> listKeywords(String tenantId, String adGroupId) {
        return keywords.values().stream()
                .filter(k -> tenantId == null || tenantId.equals(k.tenantId()))
                .filter(k -> adGroupId == null || adGroupId.equals(k.adGroupId()))
                .toList();
    }

    public BiddingStrategy createBiddingStrategy(String tenantId, String strategyName, String strategyType,
                                                   String targetMetric, BigDecimal targetValue,
                                                   BigDecimal maxBid, BigDecimal minBid) {
        String strategyId = "BID-" + System.currentTimeMillis();
        BiddingStrategy strategy = new BiddingStrategy(strategyId, tenantId, strategyName, strategyType,
                targetMetric, targetValue, maxBid, minBid, true, Instant.now());
        biddingStrategies.put(strategyId, strategy);
        log.info("Created bidding strategy: id={}, name={}, type={}", strategyId, strategyName, strategyType);
        return strategy;
    }

    public List<BiddingStrategy> listBiddingStrategies(String tenantId, String strategyType) {
        return biddingStrategies.values().stream()
                .filter(s -> tenantId == null || tenantId.equals(s.tenantId()))
                .filter(s -> strategyType == null || strategyType.equals(s.strategyType()))
                .toList();
    }

    public PerformanceReport generatePerformanceReport(String tenantId, String adGroupId,
                                                        String dateRange, BigDecimal impressions,
                                                        BigDecimal clicks, BigDecimal spend,
                                                        BigDecimal revenue, BigDecimal orders) {
        BigDecimal ctr = impressions.compareTo(BigDecimal.ZERO) > 0
                ? clicks.divide(impressions, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
        BigDecimal cpc = clicks.compareTo(BigDecimal.ZERO) > 0
                ? spend.divide(clicks, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal acos = revenue.compareTo(BigDecimal.ZERO) > 0
                ? spend.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
        BigDecimal roas = spend.compareTo(BigDecimal.ZERO) > 0
                ? revenue.divide(spend, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal conversionRate = clicks.compareTo(BigDecimal.ZERO) > 0
                ? orders.divide(clicks, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        String reportId = "RPT-" + System.currentTimeMillis();
        PerformanceReport report = new PerformanceReport(reportId, tenantId, adGroupId, dateRange,
                impressions, clicks, spend, revenue, orders,
                ctr, cpc, acos, roas, conversionRate, Instant.now());
        performanceReports.put(reportId, report);
        log.info("Generated performance report: id={}, adGroup={}, ACOS={}%, ROAS={}", reportId, adGroupId, acos, roas);
        return report;
    }

    public List<PerformanceReport> listPerformanceReports(String tenantId, String adGroupId) {
        return performanceReports.values().stream()
                .filter(r -> tenantId == null || tenantId.equals(r.tenantId()))
                .filter(r -> adGroupId == null || adGroupId.equals(r.adGroupId()))
                .toList();
    }

    public SearchTerm recordSearchTerm(String tenantId, String adGroupId, String searchTerm,
                                        BigDecimal impressions, BigDecimal clicks, BigDecimal spend,
                                        BigDecimal revenue, int orders) {
        String termId = "ST-" + System.currentTimeMillis();
        SearchTerm term = new SearchTerm(termId, tenantId, adGroupId, searchTerm,
                impressions, clicks, spend, revenue, orders, Instant.now());
        searchTerms.put(termId, term);
        log.info("Recorded search term: id={}, term={}, clicks={}", termId, searchTerm, clicks);
        return term;
    }

    public List<SearchTerm> listSearchTerms(String tenantId, String adGroupId) {
        return searchTerms.values().stream()
                .filter(st -> tenantId == null || tenantId.equals(st.tenantId()))
                .filter(st -> adGroupId == null || adGroupId.equals(st.adGroupId()))
                .toList();
    }

    public record AdGroup(String groupId, String tenantId, String campaignId, String groupName,
                           String targetingType, BigDecimal dailyBudget, String status,
                           Instant createdAt, Instant updatedAt) {}
    public record Keyword(String keywordId, String tenantId, String adGroupId, String keywordText,
                           String matchType, BigDecimal bid, String status, Instant createdAt) {}
    public record BiddingStrategy(String strategyId, String tenantId, String strategyName, String strategyType,
                                   String targetMetric, BigDecimal targetValue,
                                   BigDecimal maxBid, BigDecimal minBid, boolean active, Instant createdAt) {}
    public record PerformanceReport(String reportId, String tenantId, String adGroupId, String dateRange,
                                     BigDecimal impressions, BigDecimal clicks, BigDecimal spend,
                                     BigDecimal revenue, BigDecimal orders,
                                     BigDecimal ctr, BigDecimal cpc, BigDecimal acos,
                                     BigDecimal roas, BigDecimal conversionRate, Instant generatedAt) {}
    public record SearchTerm(String termId, String tenantId, String adGroupId, String searchTerm,
                              BigDecimal impressions, BigDecimal clicks, BigDecimal spend,
                              BigDecimal revenue, int orders, Instant recordedAt) {}
}
