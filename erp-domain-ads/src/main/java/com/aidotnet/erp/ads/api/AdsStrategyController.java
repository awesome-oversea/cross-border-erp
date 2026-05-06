package com.aidotnet.erp.ads.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.ads.application.AdsStrategyService;
import com.aidotnet.erp.ads.application.AdsStrategyService.AnalyzeSearchTermCommand;
import com.aidotnet.erp.ads.application.AdsStrategyService.CreateStrategyCommand;
import com.aidotnet.erp.ads.application.AdsStrategyService.RecordPmsActionCommand;
import com.aidotnet.erp.ads.domain.AdStrategy;
import com.aidotnet.erp.ads.domain.AdStrategy.StrategyType;
import com.aidotnet.erp.ads.domain.PmsActionLog;
import com.aidotnet.erp.ads.domain.PmsActionLog.ActionType;
import com.aidotnet.erp.ads.domain.SearchTermAnalysis;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 广告策略管理REST控制器
 * <p>
 * 描述: 提供广告策略、搜索词分析和PMS操作日志的RESTful API。
 * </p>
 * <p>
 * API分组:
 *   1. 策略管理 - /strategies (CRUD + 激活/禁用/归档/执行)
 *   2. 搜索词分析 - /strategies/search-term-analysis
 *   3. PMS操作日志 - /strategies/pms-action-logs
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/ads/api/in/v1/strategies")
public class AdsStrategyController {

    private final AdsStrategyService service;

    public AdsStrategyController(AdsStrategyService service) {
        this.service = service;
    }

    @PostMapping
    public Result<AdStrategy> createStrategy(@Valid @RequestBody CreateStrategyRequest request) {
        CreateStrategyCommand command = new CreateStrategyCommand(request.strategyCode(), request.strategyName(),
                request.strategyType(), request.targetCampaignId(), request.conditions(),
                request.actions(), request.pmsGenerated(), request.scheduleExpression());
        return Result.ok(service.createStrategy(currentTenant(), command));
    }

    @PatchMapping("/{strategyId}/activate")
    public Result<AdStrategy> activateStrategy(@PathVariable String strategyId) {
        return Result.ok(service.activateStrategy(currentTenant(), strategyId));
    }

    @PatchMapping("/{strategyId}/disable")
    public Result<AdStrategy> disableStrategy(@PathVariable String strategyId) {
        return Result.ok(service.disableStrategy(currentTenant(), strategyId));
    }

    @PatchMapping("/{strategyId}/archive")
    public Result<AdStrategy> archiveStrategy(@PathVariable String strategyId) {
        return Result.ok(service.archiveStrategy(currentTenant(), strategyId));
    }

    @PatchMapping("/{strategyId}/execute")
    public Result<AdStrategy> executeStrategy(@PathVariable String strategyId) {
        return Result.ok(service.executeStrategy(currentTenant(), strategyId));
    }

    @DeleteMapping("/{strategyId}")
    public Result<Void> deleteStrategy(@PathVariable String strategyId) {
        service.deleteStrategy(currentTenant(), strategyId);
        return Result.ok(null);
    }

    @GetMapping
    public Result<List<AdStrategy>> listStrategies(@RequestParam(required = false) StrategyType type) {
        return Result.ok(service.listStrategies(currentTenant(), type));
    }

    @GetMapping("/{strategyId}")
    public Result<AdStrategy> getStrategy(@PathVariable String strategyId) {
        return Result.ok(service.getStrategy(currentTenant(), strategyId));
    }

    @GetMapping("/by-campaign/{campaignId}")
    public Result<List<AdStrategy>> listStrategiesByCampaign(@PathVariable String campaignId) {
        return Result.ok(service.listStrategiesByCampaign(currentTenant(), campaignId));
    }

    @PostMapping("/search-term-analysis")
    public Result<SearchTermAnalysis> analyzeSearchTerm(@Valid @RequestBody AnalyzeSearchTermRequest request) {
        AnalyzeSearchTermCommand command = new AnalyzeSearchTermCommand(request.campaignId(), request.searchTerm(),
                request.impressions(), request.clicks(), request.spend(), request.revenue(), request.orders());
        return Result.ok(service.analyzeSearchTerm(currentTenant(), command));
    }

    @GetMapping("/search-term-analysis")
    public Result<List<SearchTermAnalysis>> listSearchTermAnalyses(@RequestParam String campaignId) {
        return Result.ok(service.listSearchTermAnalyses(currentTenant(), campaignId));
    }

    @GetMapping("/search-term-analysis/high-converting")
    public Result<List<SearchTermAnalysis>> getHighConvertingTerms(@RequestParam String campaignId) {
        return Result.ok(service.getHighConvertingTerms(currentTenant(), campaignId));
    }

    @DeleteMapping("/search-term-analysis/{analysisId}")
    public Result<Void> deleteSearchTermAnalysis(@PathVariable String analysisId) {
        service.deleteSearchTermAnalysis(currentTenant(), analysisId);
        return Result.ok(null);
    }

    @PostMapping("/pms-action-logs")
    public Result<PmsActionLog> recordPmsAction(@Valid @RequestBody RecordPmsActionRequest request) {
        RecordPmsActionCommand command = new RecordPmsActionCommand(request.campaignId(), request.bidId(),
                request.actionType(), request.beforeValue(), request.afterValue(),
                request.pmsReason(), request.canRollback());
        return Result.ok(service.recordPmsAction(currentTenant(), command));
    }

    @PatchMapping("/pms-action-logs/{logId}/rollback")
    public Result<PmsActionLog> rollbackPmsAction(@PathVariable String logId) {
        return Result.ok(service.rollbackPmsAction(currentTenant(), logId));
    }

    @GetMapping("/pms-action-logs")
    public Result<List<PmsActionLog>> listPmsActionLogs(
            @RequestParam(required = false) String campaignId,
            @RequestParam(required = false) ActionType actionType) {
        return Result.ok(service.listPmsActionLogs(currentTenant(), campaignId, actionType));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateStrategyRequest(@NotBlank String strategyCode, @NotBlank String strategyName,
                                         @NotBlank StrategyType strategyType, String targetCampaignId,
                                         Object conditions, Object actions, boolean pmsGenerated,
                                         String scheduleExpression) {}
    public record AnalyzeSearchTermRequest(@NotBlank String campaignId, @NotBlank String searchTerm,
                                            int impressions, int clicks, double spend, double revenue, int orders) {}
    public record RecordPmsActionRequest(@NotBlank String campaignId, String bidId,
                                          @NotBlank ActionType actionType, String beforeValue,
                                          String afterValue, String pmsReason, boolean canRollback) {}
}
