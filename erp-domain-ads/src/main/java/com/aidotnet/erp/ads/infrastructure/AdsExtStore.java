package com.aidotnet.erp.ads.infrastructure;

import com.aidotnet.erp.ads.domain.AdStrategy;
import com.aidotnet.erp.ads.domain.AdStrategy.StrategyStatus;
import com.aidotnet.erp.ads.domain.AdStrategy.StrategyType;
import com.aidotnet.erp.ads.domain.PmsActionLog;
import com.aidotnet.erp.ads.domain.PmsActionLog.ActionType;
import com.aidotnet.erp.ads.domain.SearchTermAnalysis;
import com.aidotnet.erp.ads.domain.SearchTermAnalysis.SearchTermPerformance;
import com.aidotnet.erp.ads.infrastructure.data.AdStrategyDO;
import com.aidotnet.erp.ads.infrastructure.data.PmsActionLogDO;
import com.aidotnet.erp.ads.infrastructure.data.SearchTermAnalysisDO;
import com.aidotnet.erp.ads.infrastructure.mapper.AdsExtMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * 广告策略扩展存储，管理广告策略、搜索词分析和PMS操作日志的持久化操作。
 * <p>
 * 描述: 广告管理域扩展数据存储层，封装广告策略(AdStrategy)、
 *       搜索词分析(SearchTermAnalysis)和PMS操作日志(PmsActionLog)
 *       的完整CRUD操作，包括新增、查询、更新、删除和批量操作。
 * </p>
 * <p>
 * 核心能力:
 *   1. 广告策略CRUD - 策略的创建/查询/更新/删除/批量创建/按活动查询
 *   2. 搜索词分析CRUD - 分析记录的创建/查询/删除
 *   3. PMS操作日志CRUD - 日志的创建/查询/更新(回滚)/按条件筛选
 * </p>
 * <p>
 * 业务规则:
 *   1. 所有查询必须带tenantId实现多租户隔离
 *   2. 策略保存时自动判断新增/更新(按主键是否存在)
 *   3. PMS操作日志支持回滚状态更新
 * </p>
 *
 * @author ERP系统
 * @see AdsExtMapper
 */
@Repository
public class AdsExtStore {

    private final AdsExtMapper mapper;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数 - 依赖注入Mapper和JSON序列化工具。
     *
     * @param mapper       广告策略扩展Mapper
     * @param objectMapper JSON序列化工具
     */
    public AdsExtStore(AdsExtMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 保存广告策略(新增或更新)。
     * <p>
     * 根据主键是否存在自动判断执行INSERT或UPDATE操作。
     * </p>
     *
     * @param s 广告策略领域对象
     * @return 保存后的广告策略领域对象
     */
    public AdStrategy saveStrategy(AdStrategy s) {
        AdStrategyDO existing = mapper.selectStrategy(s.tenantId(), s.strategyId());
        AdStrategyDO data = toStrategyData(s);
        if (existing == null) {
            mapper.insertStrategy(data);
        } else {
            mapper.updateStrategy(data);
        }
        return s;
    }

    /**
     * 按租户和策略ID查询广告策略。
     *
     * @param tenantId   租户ID
     * @param strategyId 策略ID
     * @return 广告策略(可能为空)
     */
    public Optional<AdStrategy> findStrategy(String tenantId, String strategyId) {
        return Optional.ofNullable(mapper.selectStrategy(tenantId, strategyId)).map(this::toStrategyDomain);
    }

    /**
     * 按租户和策略类型查询广告策略列表。
     *
     * @param tenantId 租户ID
     * @param type     策略类型(可为空，为空时查询所有类型)
     * @return 广告策略列表
     */
    public List<AdStrategy> listStrategies(String tenantId, StrategyType type) {
        return mapper.selectStrategies(tenantId, type != null ? type.name() : null).stream()
                .map(this::toStrategyDomain).collect(Collectors.toList());
    }

    /**
     * 按租户和目标广告活动ID查询策略列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 目标广告活动ID
     * @return 广告策略列表
     */
    public List<AdStrategy> listStrategiesByCampaign(String tenantId, String campaignId) {
        return mapper.selectStrategiesByCampaign(tenantId, campaignId).stream()
                .map(this::toStrategyDomain).collect(Collectors.toList());
    }

    /**
     * 删除广告策略。
     *
     * @param tenantId   租户ID
     * @param strategyId 策略ID
     */
    public void deleteStrategy(String tenantId, String strategyId) {
        mapper.deleteStrategy(tenantId, strategyId);
    }

    /**
     * 批量保存广告策略。
     * <p>
     * 仅执行批量INSERT，适用于初始化场景。若策略已存在则需逐条更新。
     * </p>
     *
     * @param strategies 广告策略列表
     * @return 保存后的广告策略列表
     */
    public List<AdStrategy> batchSaveStrategies(List<AdStrategy> strategies) {
        List<AdStrategyDO> dataList = strategies.stream().map(this::toStrategyData).collect(Collectors.toList());
        mapper.batchInsertStrategies(dataList);
        return strategies;
    }

    /**
     * 保存搜索词分析记录。
     *
     * @param a 搜索词分析领域对象
     * @return 保存后的搜索词分析领域对象
     */
    public SearchTermAnalysis saveSearchTermAnalysis(SearchTermAnalysis a) {
        SearchTermAnalysisDO data = toAnalysisData(a);
        mapper.insertSearchTermAnalysis(data);
        return a;
    }

    /**
     * 按租户和广告活动ID查询搜索词分析列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID
     * @return 搜索词分析列表
     */
    public List<SearchTermAnalysis> listSearchTermAnalyses(String tenantId, String campaignId) {
        return mapper.selectSearchTermAnalyses(tenantId, campaignId).stream()
                .map(this::toAnalysisDomain).collect(Collectors.toList());
    }

    /**
     * 删除搜索词分析记录。
     *
     * @param tenantId   租户ID
     * @param analysisId 分析记录ID
     */
    public void deleteSearchTermAnalysis(String tenantId, String analysisId) {
        mapper.deleteSearchTermAnalysis(tenantId, analysisId);
    }

    /**
     * 保存PMS操作日志。
     *
     * @param log PMS操作日志领域对象
     * @return 保存后的PMS操作日志领域对象
     */
    public PmsActionLog saveActionLog(PmsActionLog log) {
        mapper.insertActionLog(toActionLogData(log));
        return log;
    }

    /**
     * 更新PMS操作日志(用于回滚状态更新)。
     *
     * @param log PMS操作日志领域对象
     */
    public void updateActionLog(PmsActionLog log) {
        mapper.updateActionLog(toActionLogData(log));
    }

    /**
     * 按租户和日志ID查询PMS操作日志。
     *
     * @param tenantId 租户ID
     * @param logId    日志ID
     * @return PMS操作日志(可能为空)
     */
    public Optional<PmsActionLog> findActionLog(String tenantId, String logId) {
        return Optional.ofNullable(mapper.selectActionLog(tenantId, logId)).map(this::toActionLogDomain);
    }

    /**
     * 按条件查询PMS操作日志列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID(可为空)
     * @param actionType 操作类型(可为空)
     * @return PMS操作日志列表
     */
    public List<PmsActionLog> listActionLogs(String tenantId, String campaignId, ActionType actionType) {
        return mapper.selectActionLogs(tenantId, campaignId, actionType != null ? actionType.name() : null).stream()
                .map(this::toActionLogDomain).collect(Collectors.toList());
    }

    /**
     * 将广告策略领域对象转换为持久化对象。
     */
    private AdStrategyDO toStrategyData(AdStrategy s) {
        AdStrategyDO data = new AdStrategyDO();
        data.setStrategyId(s.strategyId());
        data.setTenantId(s.tenantId());
        data.setStrategyCode(s.strategyCode());
        data.setStrategyName(s.strategyName());
        data.setStrategyType(s.strategyType().name());
        data.setTargetCampaignId(s.targetCampaignId());
        data.setConditionsJson(s.conditionsJson());
        data.setActionsJson(s.actionsJson());
        data.setPmsGenerated(s.pmsGenerated() ? Boolean.TRUE : Boolean.FALSE);
        data.setScheduleExpression(s.scheduleExpression());
        data.setStatus(s.status().name());
        data.setLastExecutedAt(s.lastExecutedAt());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        data.setUpdatedAt(s.updatedAt() != null ? s.updatedAt() : Instant.now());
        return data;
    }

    /**
     * 将广告策略持久化对象转换为领域对象。
     */
    private AdStrategy toStrategyDomain(AdStrategyDO d) {
        return new AdStrategy(d.getStrategyId(), d.getTenantId(), d.getStrategyCode(), d.getStrategyName(),
                StrategyType.valueOf(d.getStrategyType()), d.getTargetCampaignId(), d.getConditionsJson(),
                d.getActionsJson(), d.getPmsGenerated(), d.getScheduleExpression(), StrategyStatus.valueOf(d.getStatus()),
                d.getLastExecutedAt(), d.getCreatedAt(), d.getUpdatedAt());
    }

    /**
     * 将搜索词分析领域对象转换为持久化对象。
     */
    private SearchTermAnalysisDO toAnalysisData(SearchTermAnalysis a) {
        SearchTermAnalysisDO data = new SearchTermAnalysisDO();
        data.setAnalysisId(a.analysisId());
        data.setTenantId(a.tenantId());
        data.setCampaignId(a.campaignId());
        data.setSearchTerm(a.searchTerm());
        data.setImpressions(a.impressions());
        data.setClicks(a.clicks());
        data.setCtr(a.ctr());
        data.setAcos(a.acos());
        data.setOrders(a.orders());
        data.setPerformance(a.performance().name());
        try {
            data.setSuggestedKeywords(objectMapper.writeValueAsString(a.suggestedKeywords()));
        } catch (JsonProcessingException e) {
            data.setSuggestedKeywords("[]");
        }
        data.setAnalyzedAt(a.analyzedAt());
        return data;
    }

    /**
     * 将搜索词分析持久化对象转换为领域对象。
     */
    private SearchTermAnalysis toAnalysisDomain(SearchTermAnalysisDO d) {
        List<String> suggestions = List.of();
        try {
            suggestions = objectMapper.readValue(d.getSuggestedKeywords(), new TypeReference<>() {});
        } catch (JsonProcessingException ignored) {}
        return new SearchTermAnalysis(d.getAnalysisId(), d.getTenantId(), d.getCampaignId(), d.getSearchTerm(),
                d.getImpressions(), d.getClicks(), d.getCtr(), d.getAcos(), d.getOrders(),
                SearchTermPerformance.valueOf(d.getPerformance()), suggestions, d.getAnalyzedAt());
    }

    /**
     * 将PMS操作日志领域对象转换为持久化对象。
     */
    private PmsActionLogDO toActionLogData(PmsActionLog l) {
        PmsActionLogDO data = new PmsActionLogDO();
        data.setLogId(l.logId());
        data.setTenantId(l.tenantId());
        data.setCampaignId(l.campaignId());
        data.setBidId(l.bidId());
        data.setActionType(l.actionType().name());
        data.setBeforeValue(l.beforeValue());
        data.setAfterValue(l.afterValue());
        data.setPmsReason(l.pmsReason());
        data.setCanRollback(l.canRollback());
        data.setRolledBack(l.rolledBack());
        data.setExecutedAt(l.executedAt());
        data.setRolledBackAt(l.rolledBackAt());
        return data;
    }

    /**
     * 将PMS操作日志持久化对象转换为领域对象。
     */
    private PmsActionLog toActionLogDomain(PmsActionLogDO d) {
        return new PmsActionLog(d.getLogId(), d.getTenantId(), d.getCampaignId(), d.getBidId(),
                ActionType.valueOf(d.getActionType()), d.getBeforeValue(), d.getAfterValue(),
                d.getPmsReason(), Boolean.TRUE.equals(d.getCanRollback()), Boolean.TRUE.equals(d.getRolledBack()),
                d.getExecutedAt(), d.getRolledBackAt());
    }
}
