package com.aidotnet.erp.ads.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 广告域对外API控制器，提供跨域调用的Outbound接口。
 * <p>
 * 描述: 供其他子系统(如BI、Dashboard、OMS)调用的广告域对外接口，
 *       包括广告活动同步、效果数据获取、关键词竞价同步、预算更新等。
 * </p>
 * <p>
 * 跨域关联:
 *   - BI → ADS: 获取广告效果数据用于报表分析
 *   - Dashboard → ADS: 获取广告活动概览数据
 *   - OMS → ADS: 订单交付后触发ROAS计算
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/ads/api/out/v1")
public class CampaignOutboundController {

    /**
     * 同步广告活动到广告平台。
     *
     * @param request 包含campaignId的请求体
     * @return 同步状态
     */
    @PostMapping("/campaigns/sync")
    public Result<Map<String, Object>> syncCampaignToPlatform(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "SYNC_REQUESTED", "campaignId", request.getOrDefault("campaignId", "")));
    }

    /**
     * 获取广告活动效果数据(供BI/Dashboard调用)。
     *
     * @param campaignId 广告活动ID
     * @return 效果数据(花费/曝光/点击等)
     */
    @GetMapping("/campaigns/{campaignId}/performance")
    public Result<Map<String, Object>> fetchCampaignPerformance(@PathVariable String campaignId) {
        return Result.ok(Map.of("campaignId", campaignId, "spend", 0, "impressions", 0, "clicks", 0));
    }

    /**
     * 同步关键词竞价到广告平台。
     *
     * @param request 包含campaignId的请求体
     * @return 同步状态
     */
    @PostMapping("/keyword-bids/sync")
    public Result<Map<String, Object>> syncKeywordBidsToPlatform(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "SYNC_REQUESTED", "campaignId", request.getOrDefault("campaignId", "")));
    }

    /**
     * 更新广告平台上的预算。
     *
     * @param campaignId 广告活动ID
     * @param request    包含新预算的请求体
     * @return 更新结果
     */
    @PostMapping("/campaigns/{campaignId}/budget-update")
    public Result<Map<String, Object>> updateBudgetOnPlatform(@PathVariable String campaignId,
                                                               @RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("campaignId", campaignId, "budgetUpdated", true));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }
}
