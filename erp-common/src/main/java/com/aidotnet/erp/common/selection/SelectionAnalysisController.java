package com.aidotnet.erp.common.selection;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pdm/api/v1/selection")
public class SelectionAnalysisController {

    private final SelectionAnalysisService selectionService;

    public SelectionAnalysisController(SelectionAnalysisService selectionService) {
        this.selectionService = selectionService;
    }

    @PostMapping("/market-trend/analyze")
    public Result<SelectionAnalysisService.MarketTrend> analyzeMarketTrend(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> indicators = (Map<String, BigDecimal>) request.get("indicators");
        return Result.ok(selectionService.analyzeMarketTrend(
                (String) request.get("category"),
                (String) request.get("market"),
                (String) request.getOrDefault("period", "MONTHLY"),
                indicators != null ? indicators : Map.of()
        ));
    }

    @GetMapping("/market-trend")
    public Result<List<SelectionAnalysisService.MarketTrend>> getMarketTrends(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String market) {
        return Result.ok(selectionService.getMarketTrends(category, market));
    }

    @PostMapping("/competitor/add")
    public Result<SelectionAnalysisService.CompetitorData> addCompetitorData(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> extra = (Map<String, Object>) request.getOrDefault("extraData", Map.of());
        return Result.ok(selectionService.addCompetitorData(
                (String) request.get("category"),
                (String) request.get("competitorName"),
                (String) request.get("asin"),
                new BigDecimal(request.get("price").toString()),
                new BigDecimal(request.getOrDefault("monthlySales", "0").toString()),
                new BigDecimal(request.getOrDefault("reviewCount", "0").toString()),
                new BigDecimal(request.getOrDefault("rating", "0").toString()),
                extra
        ));
    }

    @GetMapping("/competitor/analyze/{category}")
    public Result<SelectionAnalysisService.CompetitorAnalysis> analyzeCompetitors(@PathVariable String category) {
        return Result.ok(selectionService.analyzeCompetitors(category));
    }

    @PostMapping("/score")
    public Result<SelectionAnalysisService.SelectionScore> scoreProduct(@RequestBody Map<String, Object> request) {
        return Result.ok(selectionService.scoreProduct(
                (String) request.get("sku"),
                (String) request.get("category"),
                (String) request.get("market"),
                new BigDecimal(request.getOrDefault("marketOpportunity", "50").toString()),
                new BigDecimal(request.getOrDefault("competitionLevel", "50").toString()),
                new BigDecimal(request.getOrDefault("profitPotential", "50").toString()),
                new BigDecimal(request.getOrDefault("supplyDifficulty", "50").toString()),
                new BigDecimal(request.getOrDefault("seasonalityRisk", "50").toString())
        ));
    }

    @GetMapping("/score/{sku}")
    public Result<SelectionAnalysisService.SelectionScore> getSelectionScore(@PathVariable String sku) {
        return Result.ok(selectionService.getSelectionScore(sku));
    }

    @PostMapping("/ai/recommend")
    public Result<SelectionAnalysisService.AiRecommendation> aiRecommend(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> constraints = (Map<String, Object>) request.getOrDefault("constraints", Map.of());
        return Result.ok(selectionService.generateAiRecommendation(
                (String) request.get("category"),
                (String) request.get("market"),
                constraints
        ));
    }
}
