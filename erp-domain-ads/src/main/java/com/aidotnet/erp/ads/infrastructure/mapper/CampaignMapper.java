package com.aidotnet.erp.ads.infrastructure.mapper;

import com.aidotnet.erp.ads.infrastructure.data.AdCampaignDO;
import com.aidotnet.erp.ads.infrastructure.data.AdGroupDO;
import com.aidotnet.erp.ads.infrastructure.data.AdKeywordDO;
import com.aidotnet.erp.ads.infrastructure.data.CampaignPerformanceDO;
import com.aidotnet.erp.ads.infrastructure.data.KeywordBidDO;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 广告活动Mapper，定义广告活动、关键词竞价、效果数据、广告组和广告关键词的数据库操作。
 * <p>
 * 对应XML映射文件: mapper/CampaignMapper.xml
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface CampaignMapper {

    /** 新增广告活动。 */
    void insertCampaign(AdCampaignDO campaign);

    /** 更新广告活动。 */
    void updateCampaign(AdCampaignDO campaign);

    /** 按租户和活动ID查询广告活动。 */
    AdCampaignDO selectCampaign(@Param("tenantId") String tenantId, @Param("campaignId") String campaignId);

    /** 按租户查询广告活动列表。 */
    List<AdCampaignDO> selectCampaigns(@Param("tenantId") String tenantId);

    /** 按租户和活动ID删除广告活动。 */
    void deleteCampaign(@Param("tenantId") String tenantId, @Param("campaignId") String campaignId);

    /** 按状态查询广告活动列表。 */
    List<AdCampaignDO> selectCampaignsByStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    /** 新增关键词竞价。 */
    void insertKeywordBid(KeywordBidDO bid);

    /** 更新关键词竞价。 */
    void updateKeywordBid(KeywordBidDO bid);

    /** 按租户和竞价ID查询关键词竞价。 */
    KeywordBidDO selectKeywordBid(@Param("tenantId") String tenantId, @Param("bidId") String bidId);

    /** 按租户和活动ID查询关键词竞价列表。 */
    List<KeywordBidDO> selectKeywordBids(@Param("tenantId") String tenantId, @Param("campaignId") String campaignId);

    /** 按租户和竞价ID删除关键词竞价。 */
    void deleteKeywordBid(@Param("tenantId") String tenantId, @Param("bidId") String bidId);

    /** 按租户、活动ID和关键词查询竞价记录。 */
    KeywordBidDO selectKeywordBidByCampaignAndKeyword(@Param("tenantId") String tenantId, @Param("campaignId") String campaignId, @Param("keyword") String keyword);

    /** 新增广告活动效果数据。 */
    void insertPerformance(CampaignPerformanceDO perf);

    /** 按租户和活动ID查询效果数据列表。 */
    List<CampaignPerformanceDO> selectPerformances(@Param("tenantId") String tenantId, @Param("campaignId") String campaignId);

    /** 按条件筛选查询效果数据列表。 */
    List<CampaignPerformanceDO> selectPerformancesFiltered(@Param("tenantId") String tenantId, @Param("campaignId") String campaignId, @Param("periodStart") Instant periodStart, @Param("periodEnd") Instant periodEnd);

    /** 按租户和活动ID汇总效果数据(总花费/总点击/总订单)。 */
    CampaignPerformanceDO selectPerformanceSummary(@Param("tenantId") String tenantId, @Param("campaignId") String campaignId, @Param("periodStart") Instant periodStart, @Param("periodEnd") Instant periodEnd);

    /** 新增广告组。 */
    void insertAdGroup(AdGroupDO group);

    /** 更新广告组。 */
    void updateAdGroup(AdGroupDO group);

    /** 按租户和组ID查询广告组。 */
    AdGroupDO selectAdGroup(@Param("tenantId") String tenantId, @Param("groupId") String groupId);

    /** 按租户和活动ID查询广告组列表。 */
    List<AdGroupDO> selectAdGroupsByCampaign(@Param("tenantId") String tenantId, @Param("campaignId") String campaignId);

    /** 按租户和组ID删除广告组。 */
    void deleteAdGroup(@Param("tenantId") String tenantId, @Param("groupId") String groupId);

    /** 新增广告关键词。 */
    void insertAdKeyword(AdKeywordDO keyword);

    /** 更新广告关键词。 */
    void updateAdKeyword(AdKeywordDO keyword);

    /** 按租户和关键词ID查询广告关键词。 */
    AdKeywordDO selectAdKeyword(@Param("tenantId") String tenantId, @Param("keywordId") String keywordId);

    /** 按租户和组ID查询广告关键词列表。 */
    List<AdKeywordDO> selectAdKeywordsByGroup(@Param("tenantId") String tenantId, @Param("groupId") String groupId);

    /** 按租户和关键词ID删除广告关键词。 */
    void deleteAdKeyword(@Param("tenantId") String tenantId, @Param("keywordId") String keywordId);
}
