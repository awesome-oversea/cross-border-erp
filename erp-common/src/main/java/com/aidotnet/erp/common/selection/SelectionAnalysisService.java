package com.aidotnet.erp.common.selection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SelectionAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(SelectionAnalysisService.class);
    private final Map<String, MarketTrend> marketTrends = new ConcurrentHashMap<>();
    private final Map<String, List<CompetitorData>> competitorDataByCategory = new ConcurrentHashMap<>();
    private final Map<String, SelectionScore> selectionScores = new ConcurrentHashMap<>();
    private final Map<String, AiRecommendation> aiRecommendations = new ConcurrentHashMap<>();

    public MarketTrend analyzeMarketTrend(String category, String market, String period,
                                            Map<String, BigDecimal> indicators) {
        BigDecimal searchVolume = indicators.getOrDefault("searchVolume", BigDecimal.ZERO);
        BigDecimal growthRate = indicators.getOrDefault("growthRate", BigDecimal.ZERO);
        BigDecimal competitionIndex = indicators.getOrDefault("competitionIndex", BigDecimal.valueOf(50));
        BigDecimal avgPrice = indicators.getOrDefault("avgPrice", BigDecimal.ZERO);
        BigDecimal reviewCount = indicators.getOrDefault("reviewCount", BigDecimal.ZERO);

        String trendDirection;
        if (growthRate.compareTo(BigDecimal.valueOf(20)) > 0 && competitionIndex.compareTo(BigDecimal.valueOf(40)) < 0) {
            trendDirection = "RISING_OPPORTUNITY";
        } else if (growthRate.compareTo(BigDecimal.valueOf(20)) > 0 && competitionIndex.compareTo(BigDecimal.valueOf(70)) > 0) {
            trendDirection = "RISING_COMPETITIVE";
        } else if (growthRate.compareTo(BigDecimal.ZERO) < 0) {
            trendDirection = "DECLINING";
        } else {
            trendDirection = "STABLE";
        }

        BigDecimal opportunityScore = calculateOpportunityScore(growthRate, competitionIndex, searchVolume);

        MarketTrend trend = new MarketTrend("MT-" + System.currentTimeMillis(), category, market,
                period, searchVolume, growthRate, competitionIndex, avgPrice, reviewCount,
                trendDirection, opportunityScore, Instant.now());
        marketTrends.put(trend.trendId(), trend);

        log.info("Market trend analyzed: category={}, market={}, direction={}, opportunity={}",
                category, market, trendDirection, opportunityScore);
        return trend;
    }

    public CompetitorData addCompetitorData(String category, String competitorName, String asin,
                                              BigDecimal price, BigDecimal monthlySales,
                                              BigDecimal reviewCount, BigDecimal rating,
                                              Map<String, Object> extraData) {
        CompetitorData data = new CompetitorData("CD-" + System.currentTimeMillis(), category,
                competitorName, asin, price, monthlySales, reviewCount, rating, extraData, Instant.now());
        competitorDataByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(data);

        log.debug("Added competitor data: category={}, competitor={}, asin={}", category, competitorName, asin);
        return data;
    }

    public CompetitorAnalysis analyzeCompetitors(String category) {
        List<CompetitorData> competitors = competitorDataByCategory.getOrDefault(category, List.of());
        if (competitors.isEmpty()) {
            return new CompetitorAnalysis(category, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of());
        }

        BigDecimal avgPrice = competitors.stream().map(CompetitorData::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(competitors.size()), 2, RoundingMode.HALF_UP);
        BigDecimal avgSales = competitors.stream().map(CompetitorData::monthlySales)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(competitors.size()), 2, RoundingMode.HALF_UP);
        BigDecimal avgReviewCount = competitors.stream().map(CompetitorData::reviewCount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(competitors.size()), 0, RoundingMode.HALF_UP);
        BigDecimal avgRating = competitors.stream().map(CompetitorData::rating)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(competitors.size()), 1, RoundingMode.HALF_UP);

        BigDecimal marketConcentration = calculateMarketConcentration(competitors);

        return new CompetitorAnalysis(category, competitors.size(), avgPrice, avgSales,
                avgReviewCount, avgRating, marketConcentration, competitors);
    }

    public SelectionScore scoreProduct(String sku, String category, String market,
                                        BigDecimal marketOpportunity, BigDecimal competitionLevel,
                                        BigDecimal profitPotential, BigDecimal supplyDifficulty,
                                        BigDecimal seasonalityRisk) {
        BigDecimal marketWeight = new BigDecimal("0.30");
        BigDecimal competitionWeight = new BigDecimal("0.25");
        BigDecimal profitWeight = new BigDecimal("0.25");
        BigDecimal supplyWeight = new BigDecimal("0.10");
        BigDecimal seasonWeight = new BigDecimal("0.10");

        BigDecimal competitionScore = BigDecimal.valueOf(100).subtract(competitionLevel);
        BigDecimal supplyScore = BigDecimal.valueOf(100).subtract(supplyDifficulty);
        BigDecimal seasonScore = BigDecimal.valueOf(100).subtract(seasonalityRisk);

        BigDecimal totalScore = marketOpportunity.multiply(marketWeight)
                .add(competitionScore.multiply(competitionWeight))
                .add(profitPotential.multiply(profitWeight))
                .add(supplyScore.multiply(supplyWeight))
                .add(seasonScore.multiply(seasonWeight))
                .setScale(1, RoundingMode.HALF_UP);

        String grade;
        if (totalScore.compareTo(new BigDecimal("80")) >= 0) {
            grade = "A";
        } else if (totalScore.compareTo(new BigDecimal("65")) >= 0) {
            grade = "B";
        } else if (totalScore.compareTo(new BigDecimal("50")) >= 0) {
            grade = "C";
        } else {
            grade = "D";
        }

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        if (marketOpportunity.compareTo(new BigDecimal("70")) >= 0) strengths.add("High market opportunity");
        else weaknesses.add("Low market opportunity");
        if (competitionLevel.compareTo(new BigDecimal("40")) <= 0) strengths.add("Low competition");
        else weaknesses.add("High competition");
        if (profitPotential.compareTo(new BigDecimal("60")) >= 0) strengths.add("Good profit potential");
        else weaknesses.add("Limited profit potential");
        if (seasonalityRisk.compareTo(new BigDecimal("30")) > 0) weaknesses.add("High seasonality risk");

        SelectionScore score = new SelectionScore(sku, category, market, marketOpportunity,
                competitionLevel, profitPotential, supplyDifficulty, seasonalityRisk,
                totalScore, grade, strengths, weaknesses, Instant.now());
        selectionScores.put(sku, score);

        log.info("Product scored: sku={}, score={}, grade={}", sku, totalScore, grade);
        return score;
    }

    public AiRecommendation generateAiRecommendation(String category, String market,
                                                       Map<String, Object> constraints) {
        BigDecimal budget = constraints.containsKey("budget")
                ? new BigDecimal(constraints.get("budget").toString())
                : new BigDecimal("10000");
        String riskLevel = (String) constraints.getOrDefault("riskLevel", "MEDIUM");

        List<MarketTrend> relevantTrends = marketTrends.values().stream()
                .filter(t -> t.category().equals(category) && t.market().equals(market))
                .toList();

        List<String> recommendations = new ArrayList<>();
        List<Map<String, Object>> suggestedProducts = new ArrayList<>();
        Map<String, Object> analysisData = new HashMap<>();

        analysisData.put("category", category);
        analysisData.put("market", market);
        analysisData.put("budget", budget);
        analysisData.put("riskLevel", riskLevel);
        analysisData.put("trendCount", relevantTrends.size());

        if (!relevantTrends.isEmpty()) {
            MarketTrend bestTrend = relevantTrends.stream()
                    .max(java.util.Comparator.comparing(MarketTrend::opportunityScore))
                    .orElse(null);
            if (bestTrend != null) {
                analysisData.put("bestTrendDirection", bestTrend.trendDirection());
                analysisData.put("bestOpportunityScore", bestTrend.opportunityScore());

                if ("RISING_OPPORTUNITY".equals(bestTrend.trendDirection())) {
                    recommendations.add("Strong opportunity detected in " + category + " for " + market);
                    recommendations.add("Recommended to enter market with competitive pricing");
                    suggestedProducts.add(Map.of(
                            "strategy", "FIRST_MOVER",
                            "suggestedBudget", budget.multiply(new BigDecimal("0.6")),
                            "reason", "Rising trend with low competition"
                    ));
                } else if ("RISING_COMPETITIVE".equals(bestTrend.trendDirection())) {
                    recommendations.add("Growing market but high competition in " + category);
                    recommendations.add("Focus on differentiation and niche positioning");
                    suggestedProducts.add(Map.of(
                            "strategy", "DIFFERENTIATION",
                            "suggestedBudget", budget.multiply(new BigDecimal("0.4")),
                            "reason", "Competitive market requires focused investment"
                    ));
                } else if ("DECLINING".equals(bestTrend.trendDirection())) {
                    recommendations.add("Market declining in " + category + " - proceed with caution");
                    if ("HIGH".equals(riskLevel)) {
                        recommendations.add("High risk tolerance may allow short-term opportunities");
                    } else {
                        recommendations.add("Consider alternative categories or markets");
                    }
                }
            }
        } else {
            recommendations.add("Insufficient trend data for " + category + " in " + market);
            recommendations.add("Recommend gathering more market intelligence before proceeding");
        }

        CompetitorAnalysis competitorAnalysis = analyzeCompetitors(category);
        analysisData.put("competitorCount", competitorAnalysis.competitorCount());
        analysisData.put("avgCompetitorPrice", competitorAnalysis.avgPrice());

        if (competitorAnalysis.competitorCount() > 10) {
            recommendations.add("High number of competitors detected - consider niche sub-categories");
        }

        AiRecommendation rec = new AiRecommendation("REC-" + System.currentTimeMillis(),
                category, market, recommendations, suggestedProducts, analysisData, Instant.now());
        aiRecommendations.put(rec.recommendationId(), rec);

        log.info("AI recommendation generated: category={}, market={}, recommendations={}",
                category, market, recommendations.size());
        return rec;
    }

    public List<MarketTrend> getMarketTrends(String category, String market) {
        return marketTrends.values().stream()
                .filter(t -> category == null || t.category().equals(category))
                .filter(t -> market == null || t.market().equals(market))
                .toList();
    }

    public SelectionScore getSelectionScore(String sku) {
        return selectionScores.get(sku);
    }

    private BigDecimal calculateOpportunityScore(BigDecimal growthRate, BigDecimal competitionIndex,
                                                   BigDecimal searchVolume) {
        BigDecimal growthScore = growthRate.compareTo(BigDecimal.ZERO) > 0
                ? growthRate.min(new BigDecimal("100"))
                : BigDecimal.ZERO;
        BigDecimal competitionScore = BigDecimal.valueOf(100).subtract(competitionIndex);
        BigDecimal volumeScore = searchVolume.compareTo(new BigDecimal("10000")) > 0
                ? new BigDecimal("80")
                : searchVolume.divide(new BigDecimal("10000"), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("80")).min(new BigDecimal("80"));

        return growthScore.multiply(new BigDecimal("0.4"))
                .add(competitionScore.multiply(new BigDecimal("0.35"))
                        .add(volumeScore.multiply(new BigDecimal("0.25"))))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMarketConcentration(List<CompetitorData> competitors) {
        if (competitors.size() < 3) return BigDecimal.valueOf(100);
        BigDecimal totalSales = competitors.stream().map(CompetitorData::monthlySales)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalSales.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        List<BigDecimal> shares = competitors.stream()
                .map(c -> c.monthlySales().divide(totalSales, 4, RoundingMode.HALF_UP))
                .toList();
        BigDecimal hhi = shares.stream()
                .map(s -> s.multiply(s).multiply(BigDecimal.valueOf(10000)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return hhi.setScale(0, RoundingMode.HALF_UP);
    }

    public record MarketTrend(String trendId, String category, String market, String period,
                               BigDecimal searchVolume, BigDecimal growthRate, BigDecimal competitionIndex,
                               BigDecimal avgPrice, BigDecimal reviewCount, String trendDirection,
                               BigDecimal opportunityScore, Instant analyzedAt) {}
    public record CompetitorData(String dataId, String category, String competitorName, String asin,
                                  BigDecimal price, BigDecimal monthlySales, BigDecimal reviewCount,
                                  BigDecimal rating, Map<String, Object> extraData, Instant collectedAt) {}
    public record CompetitorAnalysis(String category, int competitorCount, BigDecimal avgPrice,
                                      BigDecimal avgMonthlySales, BigDecimal avgReviewCount,
                                      BigDecimal avgRating, BigDecimal marketConcentration,
                                      List<CompetitorData> competitors) {}
    public record SelectionScore(String sku, String category, String market,
                                  BigDecimal marketOpportunity, BigDecimal competitionLevel,
                                  BigDecimal profitPotential, BigDecimal supplyDifficulty,
                                  BigDecimal seasonalityRisk, BigDecimal totalScore, String grade,
                                  List<String> strengths, List<String> weaknesses, Instant scoredAt) {}
    public record AiRecommendation(String recommendationId, String category, String market,
                                    List<String> recommendations,
                                    List<Map<String, Object>> suggestedProducts,
                                    Map<String, Object> analysisData, Instant generatedAt) {}
}
