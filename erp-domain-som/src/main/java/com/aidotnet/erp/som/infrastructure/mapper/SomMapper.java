package com.aidotnet.erp.som.infrastructure.mapper;

import com.aidotnet.erp.som.infrastructure.data.ChannelSkuDO;
import com.aidotnet.erp.som.infrastructure.data.ListingDO;
import com.aidotnet.erp.som.infrastructure.data.ListingOptimizationDO;
import com.aidotnet.erp.som.infrastructure.data.ListingPerformanceDO;
import com.aidotnet.erp.som.infrastructure.data.PmsListingSuggestionDO;
import com.aidotnet.erp.som.infrastructure.data.PriceRuleDO;
import com.aidotnet.erp.som.infrastructure.data.SalesAlertDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * SOM域Listing数据MyBatis映射器
 * <p>
 * 描述: 销售运营域核心数据访问层，提供Listing、渠道SKU、价格规则、
 *       优化记录、表现数据、PMS建议、销售告警等表的数据操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface SomMapper {

    // ---- Listing ----
    /** 新增Listing */
    void insertListing(ListingDO listing);
    /** 更新Listing */
    void updateListing(ListingDO listing);
    /** 按租户ID+Listing ID查询 */
    ListingDO selectListing(@Param("tenantId") String tenantId, @Param("listingId") String listingId);
    /** 按租户ID+平台+平台Listing ID查询(唯一性校验) */
    ListingDO selectListingByMarketplaceId(@Param("tenantId") String tenantId, @Param("platform") String platform, @Param("marketplaceListingId") String marketplaceListingId);
    /** 按租户ID查询Listing列表 */
    List<ListingDO> selectListings(@Param("tenantId") String tenantId);
    /** 按租户ID+店铺ID查询Listing列表 */
    List<ListingDO> selectListingsByStore(@Param("tenantId") String tenantId, @Param("storeId") String storeId);
    /** 按租户ID+状态查询Listing列表 */
    List<ListingDO> selectListingsByStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    // ---- 渠道SKU ----
    /** 新增渠道SKU映射 */
    void insertChannelSku(ChannelSkuDO channelSku);
    /** 更新渠道SKU映射 */
    void updateChannelSku(ChannelSkuDO channelSku);
    /** 按租户ID+渠道SKU ID查询 */
    ChannelSkuDO selectChannelSku(@Param("tenantId") String tenantId, @Param("channelSkuId") String channelSkuId);
    /** 按映射关系查询(唯一性校验) */
    ChannelSkuDO selectChannelSkuByMapping(@Param("tenantId") String tenantId, @Param("productSku") String productSku, @Param("channel") String channel, @Param("channelSku") String channelSku);
    /** 按外部Seller SKU作用域查询 */
    ChannelSkuDO selectChannelSkuByExternalSku(@Param("tenantId") String tenantId, @Param("channel") String channel,
                                               @Param("storeId") String storeId, @Param("marketplaceId") String marketplaceId,
                                               @Param("channelSku") String channelSku);
    /** 按租户ID查询渠道SKU列表 */
    List<ChannelSkuDO> selectChannelSkus(@Param("tenantId") String tenantId);
    /** 按租户ID+产品SKU查询渠道SKU列表 */
    List<ChannelSkuDO> selectChannelSkusByProductSku(@Param("tenantId") String tenantId, @Param("productSku") String productSku);

    // ---- 价格规则 ----
    /** 新增价格规则 */
    void insertPriceRule(PriceRuleDO priceRule);
    /** 更新价格规则 */
    void updatePriceRule(PriceRuleDO priceRule);
    /** 按租户ID+规则ID查询 */
    PriceRuleDO selectPriceRule(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);
    /** 按租户ID查询价格规则列表 */
    List<PriceRuleDO> selectPriceRules(@Param("tenantId") String tenantId);
    /** 按租户ID查询启用中的价格规则 */
    List<PriceRuleDO> selectActivePriceRules(@Param("tenantId") String tenantId);

    // ---- 优化记录 ----
    /** 新增Listing优化记录 */
    void insertListingOptimization(ListingOptimizationDO optimization);
    /** 按ID查询优化记录 */
    ListingOptimizationDO selectListingOptimization(@Param("tenantId") String tenantId, @Param("optimizationId") String optimizationId);
    /** 按租户ID+Listing ID查询优化记录列表 */
    List<ListingOptimizationDO> selectListingOptimizations(@Param("tenantId") String tenantId, @Param("listingId") String listingId);

    // ---- 表现数据 ----
    /** 新增Listing表现数据 */
    void insertListingPerformance(ListingPerformanceDO performance);
    /** 按租户ID+Listing ID查询表现数据列表 */
    List<ListingPerformanceDO> selectListingPerformances(@Param("tenantId") String tenantId, @Param("listingId") String listingId);
    /** 按租户ID+店铺ID查询表现数据列表 */
    List<ListingPerformanceDO> selectPerformancesByStore(@Param("tenantId") String tenantId, @Param("storeId") String storeId);

    // ---- PMS建议 ----
    /** 新增PMS建议 */
    void insertPmsSuggestion(PmsListingSuggestionDO suggestion);
    /** 按租户ID+建议ID查询 */
    PmsListingSuggestionDO selectPmsSuggestion(@Param("tenantId") String tenantId, @Param("suggestionId") String suggestionId);
    /** 按幂等键查询(防重复) */
    PmsListingSuggestionDO selectPmsSuggestionByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
    /** 按租户ID+Listing ID查询PMS建议列表 */
    List<PmsListingSuggestionDO> selectPmsSuggestionsByListing(@Param("tenantId") String tenantId, @Param("listingId") String listingId);
    /** 更新PMS建议状态 */
    void updatePmsSuggestionStatus(@Param("suggestionId") String suggestionId, @Param("status") String status);

    // ---- 销售告警 ----
    /** 新增销售告警 */
    void insertSalesAlert(SalesAlertDO alert);
    /** 按租户ID+店铺ID查询告警列表 */
    List<SalesAlertDO> selectSalesAlerts(@Param("tenantId") String tenantId, @Param("storeId") String storeId);
    /** 查询租户下所有未确认告警 */
    List<SalesAlertDO> selectUnacknowledgedAlerts(@Param("tenantId") String tenantId);
    /** 确认告警 */
    void acknowledgeAlert(@Param("alertId") String alertId, @Param("tenantId") String tenantId);
}
