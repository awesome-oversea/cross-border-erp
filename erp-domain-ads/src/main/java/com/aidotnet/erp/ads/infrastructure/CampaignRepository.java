package com.aidotnet.erp.ads.infrastructure;

import com.aidotnet.erp.ads.domain.AdCampaign;
import com.aidotnet.erp.ads.domain.AdGroup;
import com.aidotnet.erp.ads.domain.AdKeyword;
import com.aidotnet.erp.ads.domain.CampaignPerformance;
import com.aidotnet.erp.ads.domain.CampaignStatus;
import com.aidotnet.erp.ads.domain.KeywordBid;
import com.aidotnet.erp.ads.infrastructure.data.AdCampaignDO;
import com.aidotnet.erp.ads.infrastructure.data.AdGroupDO;
import com.aidotnet.erp.ads.infrastructure.data.AdKeywordDO;
import com.aidotnet.erp.ads.infrastructure.data.CampaignPerformanceDO;
import com.aidotnet.erp.ads.infrastructure.data.KeywordBidDO;
import com.aidotnet.erp.ads.infrastructure.mapper.CampaignMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 广告活动存储库，管理广告活动、关键词竞价、效果数据、广告组和广告关键词的持久化操作。
 * <p>
 * 描述: 广告管理域核心数据存储层，封装广告活动(AdCampaign)、关键词竞价(KeywordBid)、
 *       效果数据(CampaignPerformance)、广告组(AdGroup)和广告关键词(AdKeyword)
 *       的完整CRUD操作。
 * </p>
 * <p>
 * 核心能力:
 *   1. 广告活动CRUD - 活动的创建/查询/更新/删除/按状态查询
 *   2. 关键词竞价CRUD - 竞价的创建/查询/更新/删除/按关键词查询
 *   3. 效果数据 - 记录/查询/按时间段筛选/汇总统计
 *   4. 广告组CRUD - 组的创建/查询/更新/删除
 *   5. 广告关键词CRUD - 关键词的创建/查询/更新/删除
 * </p>
 *
 * @author ERP系统
 * @see CampaignMapper
 */
@Repository
public class CampaignRepository {

    private final CampaignMapper mapper;

    /**
     * 构造函数 - 依赖注入广告活动Mapper。
     *
     * @param mapper 广告活动Mapper
     */
    public CampaignRepository(CampaignMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存广告活动(新增或更新)。
     *
     * @param campaign 广告活动领域对象
     * @return 保存后的广告活动领域对象
     */
    public AdCampaign save(AdCampaign campaign) {
        AdCampaignDO existing = mapper.selectCampaign(campaign.tenantId(), campaign.campaignId());
        AdCampaignDO data = toCampaignData(campaign);
        if (existing == null) {
            mapper.insertCampaign(data);
        } else {
            mapper.updateCampaign(data);
        }
        return campaign;
    }

    /**
     * 按租户和活动ID查询广告活动。
     *
     * @param tenantId   租户ID
     * @param campaignId 活动ID
     * @return 广告活动(可能为空)
     */
    public Optional<AdCampaign> find(String tenantId, String campaignId) {
        return Optional.ofNullable(mapper.selectCampaign(tenantId, campaignId)).map(this::toCampaignDomain);
    }

    /**
     * 按租户查询所有广告活动。
     *
     * @param tenantId 租户ID
     * @return 广告活动列表
     */
    public List<AdCampaign> list(String tenantId) {
        return mapper.selectCampaigns(tenantId).stream().map(this::toCampaignDomain).toList();
    }

    /**
     * 按租户和状态查询广告活动列表。
     *
     * @param tenantId 租户ID
     * @param status   活动状态
     * @return 广告活动列表
     */
    public List<AdCampaign> listByStatus(String tenantId, CampaignStatus status) {
        return mapper.selectCampaignsByStatus(tenantId, status.name()).stream().map(this::toCampaignDomain).toList();
    }

    /**
     * 删除广告活动。
     *
     * @param tenantId   租户ID
     * @param campaignId 活动ID
     */
    public void delete(String tenantId, String campaignId) {
        mapper.deleteCampaign(tenantId, campaignId);
    }

    /**
     * 保存关键词竞价(新增或更新)。
     *
     * @param bid 关键词竞价领域对象
     * @return 保存后的关键词竞价领域对象
     */
    public KeywordBid saveKeywordBid(KeywordBid bid) {
        KeywordBidDO existing = mapper.selectKeywordBid(bid.tenantId(), bid.bidId());
        KeywordBidDO data = toKeywordBidData(bid);
        if (existing == null) {
            mapper.insertKeywordBid(data);
        } else {
            mapper.updateKeywordBid(data);
        }
        return bid;
    }

    /**
     * 按租户和竞价ID查询关键词竞价。
     *
     * @param tenantId 租户ID
     * @param bidId    竞价ID
     * @return 关键词竞价(可能为空)
     */
    public Optional<KeywordBid> findKeywordBid(String tenantId, String bidId) {
        return Optional.ofNullable(mapper.selectKeywordBid(tenantId, bidId)).map(this::toKeywordBidDomain);
    }

    /**
     * 按租户和活动ID查询关键词竞价列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 活动ID
     * @return 关键词竞价列表
     */
    public List<KeywordBid> listKeywordBids(String tenantId, String campaignId) {
        return mapper.selectKeywordBids(tenantId, campaignId).stream().map(this::toKeywordBidDomain).toList();
    }

    /**
     * 删除关键词竞价。
     *
     * @param tenantId 租户ID
     * @param bidId    竞价ID
     */
    public void deleteKeywordBid(String tenantId, String bidId) {
        mapper.deleteKeywordBid(tenantId, bidId);
    }

    /**
     * 按租户、活动ID和关键词查询竞价记录。
     *
     * @param tenantId   租户ID
     * @param campaignId 活动ID
     * @param keyword    关键词
     * @return 关键词竞价(可能为空)
     */
    public Optional<KeywordBid> findKeywordBidByCampaignAndKeyword(String tenantId, String campaignId, String keyword) {
        return Optional.ofNullable(mapper.selectKeywordBidByCampaignAndKeyword(tenantId, campaignId, keyword))
                .map(this::toKeywordBidDomain);
    }

    /**
     * 保存效果数据。
     *
     * @param perf 效果数据领域对象
     * @return 保存后的效果数据领域对象
     */
    public CampaignPerformance savePerformance(CampaignPerformance perf) {
        mapper.insertPerformance(toPerformanceData(perf));
        return perf;
    }

    /**
     * 按租户和活动ID查询效果数据列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 活动ID
     * @return 效果数据列表
     */
    public List<CampaignPerformance> listPerformances(String tenantId, String campaignId) {
        return mapper.selectPerformances(tenantId, campaignId).stream().map(this::toPerformanceDomain).toList();
    }

    /**
     * 按条件筛选查询效果数据列表。
     *
     * @param tenantId    租户ID
     * @param campaignId  活动ID(可为空)
     * @param periodStart 周期起始时间(可为空)
     * @param periodEnd   周期结束时间(可为空)
     * @return 效果数据列表
     */
    public List<CampaignPerformance> listPerformancesFiltered(String tenantId, String campaignId, Instant periodStart, Instant periodEnd) {
        return mapper.selectPerformancesFiltered(tenantId, campaignId, periodStart, periodEnd).stream()
                .map(this::toPerformanceDomain).toList();
    }

    /**
     * 按条件汇总效果数据(总花费/总点击/总订单)。
     *
     * @param tenantId    租户ID
     * @param campaignId  活动ID
     * @param periodStart 周期起始时间(可为空)
     * @param periodEnd   周期结束时间(可为空)
     * @return 汇总效果数据(可能为空)
     */
    public Optional<CampaignPerformance> findPerformanceSummary(String tenantId, String campaignId, Instant periodStart, Instant periodEnd) {
        return Optional.ofNullable(mapper.selectPerformanceSummary(tenantId, campaignId, periodStart, periodEnd))
                .map(this::toPerformanceDomain);
    }

    /**
     * 保存广告组(新增或更新)。
     *
     * @param group 广告组领域对象
     * @return 保存后的广告组领域对象
     */
    public AdGroup saveAdGroup(AdGroup group) {
        AdGroupDO existing = mapper.selectAdGroup(group.tenantId(), group.groupId());
        AdGroupDO data = toAdGroupData(group);
        if (existing == null) {
            mapper.insertAdGroup(data);
        } else {
            mapper.updateAdGroup(data);
        }
        return group;
    }

    /**
     * 按租户和组ID查询广告组。
     *
     * @param tenantId 租户ID
     * @param groupId  组ID
     * @return 广告组(可能为空)
     */
    public Optional<AdGroup> findAdGroup(String tenantId, String groupId) {
        return Optional.ofNullable(mapper.selectAdGroup(tenantId, groupId)).map(this::toAdGroupDomain);
    }

    /**
     * 按租户和活动ID查询广告组列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 活动ID
     * @return 广告组列表
     */
    public List<AdGroup> listAdGroups(String tenantId, String campaignId) {
        return mapper.selectAdGroupsByCampaign(tenantId, campaignId).stream().map(this::toAdGroupDomain).toList();
    }

    /**
     * 删除广告组。
     *
     * @param tenantId 租户ID
     * @param groupId  组ID
     */
    public void deleteAdGroup(String tenantId, String groupId) {
        mapper.deleteAdGroup(tenantId, groupId);
    }

    /**
     * 保存广告关键词(新增或更新)。
     *
     * @param keyword 广告关键词领域对象
     * @return 保存后的广告关键词领域对象
     */
    public AdKeyword saveAdKeyword(AdKeyword keyword) {
        AdKeywordDO existing = mapper.selectAdKeyword(keyword.tenantId(), keyword.keywordId());
        AdKeywordDO data = toAdKeywordData(keyword);
        if (existing == null) {
            mapper.insertAdKeyword(data);
        } else {
            mapper.updateAdKeyword(data);
        }
        return keyword;
    }

    /**
     * 按租户和关键词ID查询广告关键词。
     *
     * @param tenantId  租户ID
     * @param keywordId 关键词ID
     * @return 广告关键词(可能为空)
     */
    public Optional<AdKeyword> findAdKeyword(String tenantId, String keywordId) {
        return Optional.ofNullable(mapper.selectAdKeyword(tenantId, keywordId)).map(this::toAdKeywordDomain);
    }

    /**
     * 按租户和组ID查询广告关键词列表。
     *
     * @param tenantId 租户ID
     * @param groupId  组ID
     * @return 广告关键词列表
     */
    public List<AdKeyword> listAdKeywords(String tenantId, String groupId) {
        return mapper.selectAdKeywordsByGroup(tenantId, groupId).stream().map(this::toAdKeywordDomain).toList();
    }

    /**
     * 删除广告关键词。
     *
     * @param tenantId  租户ID
     * @param keywordId 关键词ID
     */
    public void deleteAdKeyword(String tenantId, String keywordId) {
        mapper.deleteAdKeyword(tenantId, keywordId);
    }

    /**
     * 将广告活动领域对象转换为数据库对象，保证状态与PMS优化配置可落库。
     */
    private AdCampaignDO toCampaignData(AdCampaign c) {
        AdCampaignDO d = new AdCampaignDO();
        d.setCampaignId(c.campaignId());
        d.setTenantId(c.tenantId());
        d.setPlatform(c.platform());
        d.setCampaignName(c.campaignName());
        d.setDailyBudget(c.dailyBudget());
        d.setStatus(c.status().name());
        d.setPmsOptimizationEnabled(c.pmsOptimizationEnabled());
        d.setPmsOptimizationScope(c.pmsOptimizationScope());
        d.setCreatedAt(c.createdAt());
        d.setUpdatedAt(c.updatedAt());
        return d;
    }

    /**
     * 从数据库对象恢复广告活动领域对象，保持自动优化配置在查询后不丢失。
     */
    private AdCampaign toCampaignDomain(AdCampaignDO d) {
        return new AdCampaign(d.getCampaignId(), d.getTenantId(), d.getPlatform(), d.getCampaignName(),
                d.getDailyBudget(), CampaignStatus.valueOf(d.getStatus()), Boolean.TRUE.equals(d.getPmsOptimizationEnabled()),
                d.getPmsOptimizationScope(), d.getCreatedAt(), d.getUpdatedAt());
    }

    /**
     * 将关键词竞价领域对象转换为数据库对象。
     */
    private KeywordBidDO toKeywordBidData(KeywordBid b) {
        KeywordBidDO d = new KeywordBidDO();
        d.setBidId(b.bidId());
        d.setTenantId(b.tenantId());
        d.setCampaignId(b.campaignId());
        d.setKeyword(b.keyword());
        d.setBidAmount(b.bidAmount());
        d.setMaxBid(b.maxBid());
        d.setStrategy(b.strategy().name());
        d.setCreatedAt(b.createdAt());
        d.setUpdatedAt(b.updatedAt());
        return d;
    }

    /**
     * 将关键词竞价数据库对象转换为领域对象。
     */
    private KeywordBid toKeywordBidDomain(KeywordBidDO d) {
        return new KeywordBid(d.getBidId(), d.getTenantId(), d.getCampaignId(), d.getKeyword(),
                d.getBidAmount(), d.getMaxBid(), KeywordBid.BidStrategy.valueOf(d.getStrategy()),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    /**
     * 将效果数据领域对象转换为数据库对象。
     */
    private CampaignPerformanceDO toPerformanceData(CampaignPerformance p) {
        CampaignPerformanceDO d = new CampaignPerformanceDO();
        d.setPerformanceId(p.performanceId());
        d.setTenantId(p.tenantId());
        d.setCampaignId(p.campaignId());
        d.setSpend(p.spend());
        d.setImpressions(p.impressions());
        d.setClicks(p.clicks());
        d.setCtr(p.ctr());
        d.setOrders(p.orders());
        d.setAcos(p.acos());
        d.setRoas(p.roas());
        d.setPeriodStart(p.periodStart());
        d.setPeriodEnd(p.periodEnd());
        d.setCreatedAt(p.createdAt());
        return d;
    }

    /**
     * 将效果数据数据库对象转换为领域对象。
     */
    private CampaignPerformance toPerformanceDomain(CampaignPerformanceDO d) {
        return new CampaignPerformance(d.getPerformanceId(), d.getTenantId(), d.getCampaignId(),
                d.getSpend(), d.getImpressions(), d.getClicks(), d.getCtr(), d.getOrders(),
                d.getAcos(), d.getRoas(), d.getPeriodStart(), d.getPeriodEnd(), d.getCreatedAt());
    }

    /**
     * 将广告组领域对象转换为数据库对象。
     */
    private AdGroupDO toAdGroupData(AdGroup g) {
        AdGroupDO d = new AdGroupDO();
        d.setGroupId(g.groupId());
        d.setTenantId(g.tenantId());
        d.setCampaignId(g.campaignId());
        d.setPlatformGroupId(g.platformGroupId());
        d.setName(g.name());
        d.setBid(g.bid());
        d.setStatus(g.status().name());
        d.setCreatedAt(g.createdAt());
        d.setUpdatedAt(g.updatedAt());
        return d;
    }

    /**
     * 将广告组数据库对象转换为领域对象。
     */
    private AdGroup toAdGroupDomain(AdGroupDO d) {
        return new AdGroup(d.getGroupId(), d.getTenantId(), d.getCampaignId(), d.getPlatformGroupId(),
                d.getName(), d.getBid(), AdGroup.AdGroupStatus.valueOf(d.getStatus()),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    /**
     * 将广告关键词领域对象转换为数据库对象。
     */
    private AdKeywordDO toAdKeywordData(AdKeyword k) {
        AdKeywordDO d = new AdKeywordDO();
        d.setKeywordId(k.keywordId());
        d.setTenantId(k.tenantId());
        d.setGroupId(k.groupId());
        d.setKeywordText(k.keywordText());
        d.setMatchType(k.matchType());
        d.setBid(k.bid());
        d.setStatus(k.status().name());
        d.setImpressions(k.impressions());
        d.setClicks(k.clicks());
        d.setSpend(k.spend());
        d.setSales(k.sales());
        d.setAcos(k.acos());
        d.setCreatedAt(k.createdAt());
        d.setUpdatedAt(k.updatedAt());
        return d;
    }

    /**
     * 将广告关键词数据库对象转换为领域对象。
     */
    private AdKeyword toAdKeywordDomain(AdKeywordDO d) {
        return new AdKeyword(d.getKeywordId(), d.getTenantId(), d.getGroupId(), d.getKeywordText(),
                d.getMatchType(), d.getBid(), AdKeyword.AdKeywordStatus.valueOf(d.getStatus()),
                d.getImpressions(), d.getClicks(), d.getSpend(), d.getSales(), d.getAcos(),
                d.getCreatedAt(), d.getUpdatedAt());
    }
}
