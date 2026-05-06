package com.aidotnet.erp.ads.api;

import com.aidotnet.erp.ads.application.CampaignService;
import com.aidotnet.erp.ads.application.CampaignService.CreateAdGroupCommand;
import com.aidotnet.erp.ads.application.CampaignService.CreateAdKeywordCommand;
import com.aidotnet.erp.ads.application.CampaignService.CreateCampaignCommand;
import com.aidotnet.erp.ads.application.CampaignService.CreateKeywordBidCommand;
import com.aidotnet.erp.ads.application.CampaignService.RecordPerformanceCommand;
import com.aidotnet.erp.ads.application.CampaignService.UpdateAdGroupCommand;
import com.aidotnet.erp.ads.application.CampaignService.UpdateAdKeywordCommand;
import com.aidotnet.erp.ads.application.CampaignService.UpdateKeywordBidCommand;
import com.aidotnet.erp.ads.domain.AdCampaign;
import com.aidotnet.erp.ads.domain.AdGroup;
import com.aidotnet.erp.ads.domain.AdKeyword;
import com.aidotnet.erp.ads.domain.CampaignPerformance;
import com.aidotnet.erp.ads.domain.CampaignStatus;
import com.aidotnet.erp.ads.domain.KeywordBid;
import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
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
 * 广告活动管理REST控制器
 * <p>
 * 描述: 提供广告活动、关键词竞价、效果数据、广告组和广告关键词的RESTful API。
 * </p>
 * <p>
 * API分组:
 *   1. 广告活动 - /campaigns (CRUD + 激活/暂停)
 *   2. 关键词竞价 - /keyword-bids (CRUD)
 *   3. 效果数据 - /campaigns/{id}/performances (记录/查询/筛选/汇总)
 *   4. 广告组 - /ad-groups (CRUD)
 *   5. 广告关键词 - /ad-keywords (CRUD)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/ads/api/in/v1")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping("/campaigns")
    public Result<AdCampaign> create(@Valid @RequestBody CreateCampaignRequest request) {
        return Result.ok(campaignService.create(currentTenant(), new CreateCampaignCommand(request.platform(), request.campaignName(), request.dailyBudget())));
    }

    @PatchMapping("/campaigns/{campaignId}/activate")
    public Result<AdCampaign> activate(@PathVariable String campaignId) {
        return Result.ok(campaignService.activate(currentTenant(), campaignId));
    }

    @PatchMapping("/campaigns/{campaignId}/pause")
    public Result<AdCampaign> pause(@PathVariable String campaignId) {
        return Result.ok(campaignService.pause(currentTenant(), campaignId));
    }

    @DeleteMapping("/campaigns/{campaignId}")
    public Result<Void> deleteCampaign(@PathVariable String campaignId) {
        campaignService.delete(currentTenant(), campaignId);
        return Result.ok(null);
    }

    @GetMapping("/campaigns")
    public Result<List<AdCampaign>> list(@RequestParam(required = false) CampaignStatus status) {
        if (status != null) {
            return Result.ok(campaignService.listByStatus(currentTenant(), status));
        }
        return Result.ok(campaignService.list(currentTenant()));
    }

    @PostMapping("/campaigns/{campaignId}/keyword-bids")
    public Result<KeywordBid> createKeywordBid(@PathVariable String campaignId,
                                                @Valid @RequestBody CreateKeywordBidRequest request) {
        return Result.ok(campaignService.createKeywordBid(currentTenant(), new CreateKeywordBidCommand(
                campaignId, request.keyword(), request.bidAmount(), request.maxBid(), request.strategy())));
    }

    @PatchMapping("/keyword-bids/{bidId}")
    public Result<KeywordBid> updateKeywordBid(@PathVariable String bidId,
                                                @Valid @RequestBody UpdateKeywordBidRequest request) {
        return Result.ok(campaignService.updateKeywordBid(currentTenant(), bidId,
                new UpdateKeywordBidCommand(request.bidAmount(), request.maxBid(), request.strategy())));
    }

    @DeleteMapping("/keyword-bids/{bidId}")
    public Result<Void> deleteKeywordBid(@PathVariable String bidId) {
        campaignService.deleteKeywordBid(currentTenant(), bidId);
        return Result.ok(null);
    }

    @GetMapping("/campaigns/{campaignId}/keyword-bids")
    public Result<List<KeywordBid>> listKeywordBids(@PathVariable String campaignId) {
        return Result.ok(campaignService.listKeywordBids(currentTenant(), campaignId));
    }

    @PostMapping("/campaigns/{campaignId}/performances")
    public Result<CampaignPerformance> recordPerformance(@PathVariable String campaignId,
                                                          @Valid @RequestBody RecordPerformanceRequest request) {
        return Result.ok(campaignService.recordPerformance(currentTenant(), new RecordPerformanceCommand(
                campaignId, request.spend(), request.impressions(), request.clicks(),
                request.orders(), request.periodStart(), request.periodEnd())));
    }

    @GetMapping("/campaigns/{campaignId}/performances")
    public Result<List<CampaignPerformance>> listPerformances(@PathVariable String campaignId,
                                                               @RequestParam(required = false) Instant periodStart,
                                                               @RequestParam(required = false) Instant periodEnd) {
        if (periodStart != null || periodEnd != null) {
            return Result.ok(campaignService.listPerformancesFiltered(currentTenant(), campaignId, periodStart, periodEnd));
        }
        return Result.ok(campaignService.listPerformances(currentTenant(), campaignId));
    }

    @GetMapping("/campaigns/{campaignId}/performances/summary")
    public Result<CampaignPerformance> getPerformanceSummary(@PathVariable String campaignId,
                                                              @RequestParam(required = false) Instant periodStart,
                                                              @RequestParam(required = false) Instant periodEnd) {
        return Result.ok(campaignService.getPerformanceSummary(currentTenant(), campaignId, periodStart, periodEnd));
    }

    @PostMapping("/campaigns/{campaignId}/ad-groups")
    public Result<AdGroup> createAdGroup(@PathVariable String campaignId,
                                          @Valid @RequestBody CreateAdGroupRequest request) {
        return Result.ok(campaignService.createAdGroup(currentTenant(), new CreateAdGroupCommand(
                campaignId, request.platformGroupId(), request.name(), request.bid())));
    }

    @PatchMapping("/ad-groups/{groupId}")
    public Result<AdGroup> updateAdGroup(@PathVariable String groupId,
                                          @Valid @RequestBody UpdateAdGroupRequest request) {
        return Result.ok(campaignService.updateAdGroup(currentTenant(), groupId,
                new UpdateAdGroupCommand(request.name(), request.bid(), request.status())));
    }

    @DeleteMapping("/ad-groups/{groupId}")
    public Result<Void> deleteAdGroup(@PathVariable String groupId) {
        campaignService.deleteAdGroup(currentTenant(), groupId);
        return Result.ok(null);
    }

    @GetMapping("/campaigns/{campaignId}/ad-groups")
    public Result<List<AdGroup>> listAdGroups(@PathVariable String campaignId) {
        return Result.ok(campaignService.listAdGroups(currentTenant(), campaignId));
    }

    @PostMapping("/ad-groups/{groupId}/ad-keywords")
    public Result<AdKeyword> createAdKeyword(@PathVariable String groupId,
                                              @Valid @RequestBody CreateAdKeywordRequest request) {
        return Result.ok(campaignService.createAdKeyword(currentTenant(), new CreateAdKeywordCommand(
                groupId, request.keywordText(), request.matchType(), request.bid())));
    }

    @PatchMapping("/ad-keywords/{keywordId}")
    public Result<AdKeyword> updateAdKeyword(@PathVariable String keywordId,
                                              @Valid @RequestBody UpdateAdKeywordRequest request) {
        return Result.ok(campaignService.updateAdKeyword(currentTenant(), keywordId,
                new UpdateAdKeywordCommand(request.bid(), request.status())));
    }

    @DeleteMapping("/ad-keywords/{keywordId}")
    public Result<Void> deleteAdKeyword(@PathVariable String keywordId) {
        campaignService.deleteAdKeyword(currentTenant(), keywordId);
        return Result.ok(null);
    }

    @GetMapping("/ad-groups/{groupId}/ad-keywords")
    public Result<List<AdKeyword>> listAdKeywords(@PathVariable String groupId) {
        return Result.ok(campaignService.listAdKeywords(currentTenant(), groupId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateCampaignRequest(@NotBlank String platform, @NotBlank String campaignName,
                                        @NotNull BigDecimal dailyBudget) {}

    public record CreateKeywordBidRequest(@NotBlank String keyword, @NotNull BigDecimal bidAmount,
                                          BigDecimal maxBid, KeywordBid.BidStrategy strategy) {}

    public record UpdateKeywordBidRequest(BigDecimal bidAmount, BigDecimal maxBid,
                                          KeywordBid.BidStrategy strategy) {}

    public record RecordPerformanceRequest(@NotNull BigDecimal spend, @Positive int impressions,
                                           @Positive int clicks, @Positive int orders,
                                           Instant periodStart, Instant periodEnd) {}

    public record CreateAdGroupRequest(String platformGroupId, @NotBlank String name, BigDecimal bid) {}

    public record UpdateAdGroupRequest(String name, BigDecimal bid, AdGroup.AdGroupStatus status) {}

    public record CreateAdKeywordRequest(@NotBlank String keywordText, String matchType, BigDecimal bid) {}

    public record UpdateAdKeywordRequest(BigDecimal bid, AdKeyword.AdKeywordStatus status) {}
}
