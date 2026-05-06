package com.aidotnet.erp.ads.infrastructure.mapper;

import com.aidotnet.erp.ads.infrastructure.data.AdStrategyDO;
import com.aidotnet.erp.ads.infrastructure.data.PmsActionLogDO;
import com.aidotnet.erp.ads.infrastructure.data.SearchTermAnalysisDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 广告策略扩展Mapper，定义广告策略、搜索词分析和PMS操作日志的数据库操作。
 * <p>
 * 对应XML映射文件: 无(CampaignMapper.xml中包含策略相关SQL)
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface AdsExtMapper {

    /** 新增广告策略。 */
    void insertStrategy(AdStrategyDO data);

    /** 更新广告策略。 */
    void updateStrategy(AdStrategyDO data);

    /** 按租户和策略ID查询广告策略。 */
    AdStrategyDO selectStrategy(@Param("tenantId") String tenantId, @Param("strategyId") String strategyId);

    /** 按租户和策略类型查询广告策略列表。 */
    List<AdStrategyDO> selectStrategies(@Param("tenantId") String tenantId, @Param("strategyType") String strategyType);

    /** 按租户和策略ID删除广告策略。 */
    void deleteStrategy(@Param("tenantId") String tenantId, @Param("strategyId") String strategyId);

    /** 批量新增广告策略。 */
    void batchInsertStrategies(@Param("list") List<AdStrategyDO> list);

    /** 按租户和目标广告活动ID查询策略列表。 */
    List<AdStrategyDO> selectStrategiesByCampaign(@Param("tenantId") String tenantId, @Param("campaignId") String campaignId);

    /** 新增搜索词分析记录。 */
    void insertSearchTermAnalysis(SearchTermAnalysisDO data);

    /** 按租户和广告活动ID查询搜索词分析列表。 */
    List<SearchTermAnalysisDO> selectSearchTermAnalyses(@Param("tenantId") String tenantId, @Param("campaignId") String campaignId);

    /** 按租户和分析ID删除搜索词分析记录。 */
    void deleteSearchTermAnalysis(@Param("tenantId") String tenantId, @Param("analysisId") String analysisId);

    /** 新增PMS操作日志。 */
    void insertActionLog(PmsActionLogDO data);

    /** 更新PMS操作日志(回滚状态等)。 */
    void updateActionLog(PmsActionLogDO data);

    /** 按租户和日志ID查询PMS操作日志。 */
    PmsActionLogDO selectActionLog(@Param("tenantId") String tenantId, @Param("logId") String logId);

    /** 按租户查询PMS操作日志列表，支持按广告活动和操作类型筛选。 */
    List<PmsActionLogDO> selectActionLogs(@Param("tenantId") String tenantId, @Param("campaignId") String campaignId, @Param("actionType") String actionType);
}
