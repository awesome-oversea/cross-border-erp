package com.aidotnet.erp.ads.application;

import com.aidotnet.erp.ads.domain.AdCampaign;
import com.aidotnet.erp.ads.domain.AdGroup;
import com.aidotnet.erp.ads.domain.AdKeyword;
import com.aidotnet.erp.ads.domain.CampaignPerformance;
import com.aidotnet.erp.ads.domain.CampaignStatus;
import com.aidotnet.erp.ads.domain.KeywordBid;
import com.aidotnet.erp.ads.infrastructure.CampaignRepository;
import com.aidotnet.erp.common.exception.BizException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 广告活动管理应用服务
 * <p>
 * 描述: 广告管理域核心服务，负责广告活动的创建/激活/暂停/删除、
 *       关键词出价管理、广告组管理、广告关键词管理、
 *       广告效果数据记录与汇总等业务逻辑。支持AI优化建议的接入和应用。
 * </p>
 * <p>
 * 核心能力:
 *   1. 广告活动管理 - 创建/激活/暂停/删除广告活动，预算校验
 *   2. 关键词出价 - 创建/更新/删除关键词出价，支持手动/自动策略
 *   3. 广告组管理 - 创建/更新/删除广告组
 *   4. 广告关键词管理 - 创建/更新/删除广告关键词
 *   5. 效果数据 - 记录广告活动效果数据(花费/曝光/点击/CTR/ACOS/ROAS)，支持汇总统计
 * </p>
 * <p>
 * 业务规则:
 *   1. 激活广告活动时预算必须大于0
 *   2. 关键词出价需关联有效广告活动
 *   3. CTR = clicks / impressions，ACOS = spend / orders，ROAS = orders / spend
 *   4. 删除广告活动时需确保无关联的活跃广告组
 * </p>
 *
 * @author ERP系统
 * @see AdCampaign
 * @see CampaignRepository
 */
@Service
public class CampaignService {

    private final CampaignRepository repository;

    /**
     * 构造函数 - 依赖注入广告活动存储
     *
     * @param repository 广告活动数据存储
     */
    public CampaignService(CampaignRepository repository) {
        this.repository = repository;
    }

    /**
     * 创建广告活动
     * <p>
     * 创建新的广告活动，初始状态为DRAFT。
     * </p>
     *
     * @param tenantId 租户ID
     * @param command  创建广告活动命令
     * @return 新创建的广告活动实体
     */
    @Transactional
    public AdCampaign create(String tenantId, CreateCampaignCommand command) {
        Instant now = Instant.now();
        return repository.save(new AdCampaign(UUID.randomUUID().toString(), tenantId, command.platform(), command.campaignName(),
                command.dailyBudget(), CampaignStatus.DRAFT, false, null, now, now));
    }

    /**
     * 激活广告活动。
     * <p>
     * 激活前校验预算必须大于0。
     * </p>
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID
     * @return 激活后的广告活动
     */
    @Transactional
    public AdCampaign activate(String tenantId, String campaignId) {
        AdCampaign campaign = get(tenantId, campaignId);
        if (campaign.dailyBudget().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("CAMPAIGN_BUDGET_REQUIRED", "广告预算必须大于0");
        }
        return update(campaign, CampaignStatus.ACTIVE);
    }

    /**
     * 暂停广告活动。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID
     * @return 暂停后的广告活动
     */
    @Transactional
    public AdCampaign pause(String tenantId, String campaignId) {
        return update(get(tenantId, campaignId), CampaignStatus.PAUSED);
    }

    /**
     * 删除广告活动。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID
     */
    @Transactional
    public void delete(String tenantId, String campaignId) {
        repository.delete(tenantId, campaignId);
    }

    /**
     * 查询所有广告活动。
     *
     * @param tenantId 租户ID
     * @return 广告活动列表
     */
    public List<AdCampaign> list(String tenantId) {
        return repository.list(tenantId);
    }

    /**
     * 按状态查询广告活动列表。
     *
     * @param tenantId 租户ID
     * @param status   广告活动状态
     * @return 广告活动列表
     */
    public List<AdCampaign> listByStatus(String tenantId, CampaignStatus status) {
        return repository.listByStatus(tenantId, status);
    }

    /**
     * 创建关键词竞价。
     *
     * @param tenantId 租户ID
     * @param command  创建关键词竞价命令
     * @return 新创建的关键词竞价
     */
    @Transactional
    public KeywordBid createKeywordBid(String tenantId, CreateKeywordBidCommand command) {
        get(tenantId, command.campaignId());
        Instant now = Instant.now();
        return repository.saveKeywordBid(new KeywordBid(UUID.randomUUID().toString(), tenantId,
                command.campaignId(), command.keyword(), command.bidAmount(), command.maxBid(),
                command.strategy(), now, now));
    }

    /**
     * 更新关键词竞价。
     *
     * @param tenantId 租户ID
     * @param bidId    竞价ID
     * @param command  更新关键词竞价命令
     * @return 更新后的关键词竞价
     */
    @Transactional
    public KeywordBid updateKeywordBid(String tenantId, String bidId, UpdateKeywordBidCommand command) {
        KeywordBid bid = repository.findKeywordBid(tenantId, bidId)
                .orElseThrow(() -> new BizException("KEYWORD_BID_NOT_FOUND", "关键词出价不存在"));
        Instant now = Instant.now();
        return repository.saveKeywordBid(new KeywordBid(bid.bidId(), bid.tenantId(), bid.campaignId(),
                bid.keyword(), command.bidAmount() != null ? command.bidAmount() : bid.bidAmount(),
                command.maxBid() != null ? command.maxBid() : bid.maxBid(),
                command.strategy() != null ? command.strategy() : bid.strategy(),
                bid.createdAt(), now));
    }

    /**
     * 删除关键词竞价。
     *
     * @param tenantId 租户ID
     * @param bidId    竞价ID
     */
    @Transactional
    public void deleteKeywordBid(String tenantId, String bidId) {
        repository.deleteKeywordBid(tenantId, bidId);
    }

    /**
     * 查询关键词竞价列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID
     * @return 关键词竞价列表
     */
    public List<KeywordBid> listKeywordBids(String tenantId, String campaignId) {
        return repository.listKeywordBids(tenantId, campaignId);
    }

    /**
     * 记录效果数据。
     * <p>
     * 自动计算CTR、ACOS、ROAS指标。
     * </p>
     *
     * @param tenantId 租户ID
     * @param command  记录效果数据命令
     * @return 新记录的效果数据
     */
    @Transactional
    public CampaignPerformance recordPerformance(String tenantId, RecordPerformanceCommand command) {
        get(tenantId, command.campaignId());
        BigDecimal ctr = command.impressions() > 0
                ? BigDecimal.valueOf(command.clicks()).divide(BigDecimal.valueOf(command.impressions()), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal acos = command.orders() > 0 && command.spend().compareTo(BigDecimal.ZERO) > 0
                ? command.spend().divide(BigDecimal.valueOf(command.orders()), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal roas = command.spend().compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(command.orders()).divide(command.spend(), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return repository.savePerformance(new CampaignPerformance(UUID.randomUUID().toString(), tenantId,
                command.campaignId(), command.spend(), command.impressions(), command.clicks(),
                ctr, command.orders(), acos, roas, command.periodStart(), command.periodEnd(), Instant.now()));
    }

    /**
     * 查询效果数据列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID
     * @return 效果数据列表
     */
    public List<CampaignPerformance> listPerformances(String tenantId, String campaignId) {
        return repository.listPerformances(tenantId, campaignId);
    }

    /**
     * 按时间段筛选查询效果数据列表。
     *
     * @param tenantId    租户ID
     * @param campaignId  广告活动ID(可为空)
     * @param periodStart 周期起始时间(可为空)
     * @param periodEnd   周期结束时间(可为空)
     * @return 效果数据列表
     */
    public List<CampaignPerformance> listPerformancesFiltered(String tenantId, String campaignId, Instant periodStart, Instant periodEnd) {
        return repository.listPerformancesFiltered(tenantId, campaignId, periodStart, periodEnd);
    }

    /**
     * 查询效果数据汇总。
     *
     * @param tenantId    租户ID
     * @param campaignId  广告活动ID
     * @param periodStart 周期起始时间(可为空)
     * @param periodEnd   周期结束时间(可为空)
     * @return 汇总效果数据
     */
    public CampaignPerformance getPerformanceSummary(String tenantId, String campaignId, Instant periodStart, Instant periodEnd) {
        return repository.findPerformanceSummary(tenantId, campaignId, periodStart, periodEnd)
                .orElseThrow(() -> new BizException("PERFORMANCE_NOT_FOUND", "效果数据不存在"));
    }

    /**
     * 创建广告组。
     *
     * @param tenantId 租户ID
     * @param command  创建广告组命令
     * @return 新创建的广告组
     */
    @Transactional
    public AdGroup createAdGroup(String tenantId, CreateAdGroupCommand command) {
        get(tenantId, command.campaignId());
        Instant now = Instant.now();
        return repository.saveAdGroup(new AdGroup(UUID.randomUUID().toString(), tenantId,
                command.campaignId(), command.platformGroupId(), command.name(), command.bid(),
                AdGroup.AdGroupStatus.ACTIVE, now, now));
    }

    /**
     * 更新广告组。
     *
     * @param tenantId 租户ID
     * @param groupId  组ID
     * @param command  更新广告组命令
     * @return 更新后的广告组
     */
    @Transactional
    public AdGroup updateAdGroup(String tenantId, String groupId, UpdateAdGroupCommand command) {
        AdGroup group = repository.findAdGroup(tenantId, groupId)
                .orElseThrow(() -> new BizException("AD_GROUP_NOT_FOUND", "广告组不存在"));
        Instant now = Instant.now();
        return repository.saveAdGroup(new AdGroup(group.groupId(), group.tenantId(), group.campaignId(),
                group.platformGroupId(), command.name() != null ? command.name() : group.name(),
                command.bid() != null ? command.bid() : group.bid(),
                command.status() != null ? command.status() : group.status(),
                group.createdAt(), now));
    }

    /**
     * 删除广告组。
     *
     * @param tenantId 租户ID
     * @param groupId  组ID
     */
    @Transactional
    public void deleteAdGroup(String tenantId, String groupId) {
        repository.deleteAdGroup(tenantId, groupId);
    }

    /**
     * 查询广告组列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID
     * @return 广告组列表
     */
    public List<AdGroup> listAdGroups(String tenantId, String campaignId) {
        return repository.listAdGroups(tenantId, campaignId);
    }

    /**
     * 创建广告关键词。
     *
     * @param tenantId 租户ID
     * @param command  创建广告关键词命令
     * @return 新创建的广告关键词
     */
    @Transactional
    public AdKeyword createAdKeyword(String tenantId, CreateAdKeywordCommand command) {
        repository.findAdGroup(tenantId, command.groupId())
                .orElseThrow(() -> new BizException("AD_GROUP_NOT_FOUND", "广告组不存在"));
        Instant now = Instant.now();
        return repository.saveAdKeyword(new AdKeyword(UUID.randomUUID().toString(), tenantId,
                command.groupId(), command.keywordText(), command.matchType(), command.bid(),
                AdKeyword.AdKeywordStatus.ACTIVE, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                now, now));
    }

    /**
     * 更新广告关键词。
     *
     * @param tenantId  租户ID
     * @param keywordId 关键词ID
     * @param command   更新广告关键词命令
     * @return 更新后的广告关键词
     */
    @Transactional
    public AdKeyword updateAdKeyword(String tenantId, String keywordId, UpdateAdKeywordCommand command) {
        AdKeyword keyword = repository.findAdKeyword(tenantId, keywordId)
                .orElseThrow(() -> new BizException("AD_KEYWORD_NOT_FOUND", "广告关键词不存在"));
        Instant now = Instant.now();
        return repository.saveAdKeyword(new AdKeyword(keyword.keywordId(), keyword.tenantId(), keyword.groupId(),
                keyword.keywordText(), keyword.matchType(),
                command.bid() != null ? command.bid() : keyword.bid(),
                command.status() != null ? command.status() : keyword.status(),
                keyword.impressions(), keyword.clicks(), keyword.spend(), keyword.sales(), keyword.acos(),
                keyword.createdAt(), now));
    }

    /**
     * 删除广告关键词。
     *
     * @param tenantId  租户ID
     * @param keywordId 关键词ID
     */
    @Transactional
    public void deleteAdKeyword(String tenantId, String keywordId) {
        repository.deleteAdKeyword(tenantId, keywordId);
    }

    /**
     * 查询广告关键词列表。
     *
     * @param tenantId 租户ID
     * @param groupId  组ID
     * @return 广告关键词列表
     */
    public List<AdKeyword> listAdKeywords(String tenantId, String groupId) {
        return repository.listAdKeywords(tenantId, groupId);
    }

    private AdCampaign get(String tenantId, String campaignId) {
        return repository.find(tenantId, campaignId).orElseThrow(() -> new BizException("CAMPAIGN_NOT_FOUND", "广告活动不存在"));
    }

    private AdCampaign update(AdCampaign campaign, CampaignStatus status) {
        return repository.save(new AdCampaign(campaign.campaignId(), campaign.tenantId(), campaign.platform(),
                campaign.campaignName(), campaign.dailyBudget(), status, campaign.pmsOptimizationEnabled(),
                campaign.pmsOptimizationScope(), campaign.createdAt(), Instant.now()));
    }

    public record CreateCampaignCommand(String platform, String campaignName, BigDecimal dailyBudget) {}

    public record CreateKeywordBidCommand(String campaignId, String keyword, BigDecimal bidAmount,
                                          BigDecimal maxBid, KeywordBid.BidStrategy strategy) {}

    public record UpdateKeywordBidCommand(BigDecimal bidAmount, BigDecimal maxBid,
                                          KeywordBid.BidStrategy strategy) {}

    public record RecordPerformanceCommand(String campaignId, BigDecimal spend, int impressions,
                                           int clicks, int orders, Instant periodStart, Instant periodEnd) {}

    public record CreateAdGroupCommand(String campaignId, String platformGroupId, String name,
                                       BigDecimal bid) {}

    public record UpdateAdGroupCommand(String name, BigDecimal bid, AdGroup.AdGroupStatus status) {}

    public record CreateAdKeywordCommand(String groupId, String keywordText, String matchType,
                                         BigDecimal bid) {}

    public record UpdateAdKeywordCommand(BigDecimal bid, AdKeyword.AdKeywordStatus status) {}
}
