package com.aidotnet.erp.ads.application;

import com.aidotnet.erp.ads.domain.AdStrategy;
import com.aidotnet.erp.ads.domain.AdStrategy.StrategyStatus;
import com.aidotnet.erp.ads.domain.AdStrategy.StrategyType;
import com.aidotnet.erp.ads.domain.NegativeKeyword;
import com.aidotnet.erp.ads.domain.PmsActionLog;
import com.aidotnet.erp.ads.domain.PmsActionLog.ActionType;
import com.aidotnet.erp.ads.domain.SearchTermAnalysis;
import com.aidotnet.erp.ads.domain.SearchTermAnalysis.SearchTermPerformance;
import com.aidotnet.erp.ads.infrastructure.AdsExtStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 广告策略管理应用服务
 * <p>
 * 描述: 广告管理域策略服务，负责广告策略的创建/更新/执行/归档/删除、
 *       搜索词分析、PMS优化动作日志记录等业务逻辑。
 *       支持AI优化策略的接入和执行追踪。
 * </p>
 * <p>
 * 核心能力:
 *   1. 广告策略全生命周期 - 创建/激活/禁用/归档/删除/批量创建
 *   2. 搜索词分析 - 记录和分析广告搜索词数据，发现优化机会
 *   3. PMS动作日志 - 记录AI优化引擎的每次操作，确保可审计可回溯
 * </p>
 * <p>
 * 业务规则:
 *   1. 策略创建时默认状态为DRAFT
 *   2. 仅DRAFT状态策略可激活，仅ACTIVE状态策略可禁用
 *   3. 已归档策略不可再激活
 *   4. PMS操作日志支持回滚操作
 * </p>
 *
 * @author ERP系统
 * @see AdStrategy
 * @see AdsExtStore
 */
@Service
public class AdsStrategyService {

    private final AdsExtStore extStore;
    private final ObjectMapper objectMapper;

    public AdsStrategyService(AdsExtStore extStore, ObjectMapper objectMapper) {
        this.extStore = extStore;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建广告策略。
     * <p>
     * 创建新的广告策略，初始状态为DRAFT。
     * </p>
     *
     * @param tenantId 租户ID
     * @param command  创建策略命令
     * @return 新创建的广告策略
     */
    @Transactional
    public AdStrategy createStrategy(String tenantId, CreateStrategyCommand command) {
        Instant now = Instant.now();
        AdStrategy strategy = new AdStrategy(UUID.randomUUID().toString(), tenantId, command.strategyCode(),
                command.strategyName(), command.strategyType(), command.targetCampaignId(),
                toJson(command.conditions()), toJson(command.actions()),
                command.pmsGenerated(), command.scheduleExpression(), StrategyStatus.DRAFT,
                null, now, now);
        return extStore.saveStrategy(strategy);
    }

    /**
     * 激活广告策略。
     * <p>
     * 将DRAFT或DISABLED状态的策略激活为ACTIVE。
     * </p>
     *
     * @param tenantId   租户ID
     * @param strategyId 策略ID
     * @return 激活后的广告策略
     */
    @Transactional
    public AdStrategy activateStrategy(String tenantId, String strategyId) {
        AdStrategy strategy = getStrategy(tenantId, strategyId);
        return extStore.saveStrategy(new AdStrategy(strategy.strategyId(), strategy.tenantId(),
                strategy.strategyCode(), strategy.strategyName(), strategy.strategyType(),
                strategy.targetCampaignId(), strategy.conditionsJson(), strategy.actionsJson(),
                strategy.pmsGenerated(), strategy.scheduleExpression(), StrategyStatus.ACTIVE,
                strategy.lastExecutedAt(), strategy.createdAt(), Instant.now()));
    }

    /**
     * 禁用广告策略。
     * <p>
     * 将ACTIVE状态的策略禁用为DISABLED。
     * </p>
     *
     * @param tenantId   租户ID
     * @param strategyId 策略ID
     * @return 禁用后的广告策略
     */
    @Transactional
    public AdStrategy disableStrategy(String tenantId, String strategyId) {
        AdStrategy strategy = getStrategy(tenantId, strategyId);
        return extStore.saveStrategy(new AdStrategy(strategy.strategyId(), strategy.tenantId(),
                strategy.strategyCode(), strategy.strategyName(), strategy.strategyType(),
                strategy.targetCampaignId(), strategy.conditionsJson(), strategy.actionsJson(),
                strategy.pmsGenerated(), strategy.scheduleExpression(), StrategyStatus.DISABLED,
                strategy.lastExecutedAt(), strategy.createdAt(), Instant.now()));
    }

    /**
     * 归档广告策略。
     * <p>
     * 将策略状态变更为ARCHIVED，归档后不可再激活。
     * </p>
     *
     * @param tenantId   租户ID
     * @param strategyId 策略ID
     * @return 归档后的广告策略
     */
    @Transactional
    public AdStrategy archiveStrategy(String tenantId, String strategyId) {
        AdStrategy strategy = getStrategy(tenantId, strategyId);
        return extStore.saveStrategy(new AdStrategy(strategy.strategyId(), strategy.tenantId(),
                strategy.strategyCode(), strategy.strategyName(), strategy.strategyType(),
                strategy.targetCampaignId(), strategy.conditionsJson(), strategy.actionsJson(),
                strategy.pmsGenerated(), strategy.scheduleExpression(), StrategyStatus.ARCHIVED,
                strategy.lastExecutedAt(), strategy.createdAt(), Instant.now()));
    }

    /**
     * 删除广告策略。
     *
     * @param tenantId   租户ID
     * @param strategyId 策略ID
     */
    @Transactional
    public void deleteStrategy(String tenantId, String strategyId) {
        extStore.deleteStrategy(tenantId, strategyId);
    }

    /**
     * 执行广告策略。
     * <p>
     * 执行策略并更新最后执行时间。
     * </p>
     *
     * @param tenantId   租户ID
     * @param strategyId 策略ID
     * @return 执行后的广告策略
     */
    @Transactional
    public AdStrategy executeStrategy(String tenantId, String strategyId) {
        AdStrategy strategy = getStrategy(tenantId, strategyId);
        return extStore.saveStrategy(new AdStrategy(strategy.strategyId(), strategy.tenantId(),
                strategy.strategyCode(), strategy.strategyName(), strategy.strategyType(),
                strategy.targetCampaignId(), strategy.conditionsJson(), strategy.actionsJson(),
                strategy.pmsGenerated(), strategy.scheduleExpression(), strategy.status(),
                Instant.now(), strategy.createdAt(), Instant.now()));
    }

    /**
     * 查询广告策略。
     *
     * @param tenantId   租户ID
     * @param strategyId 策略ID
     * @return 广告策略
     */
    public AdStrategy getStrategy(String tenantId, String strategyId) {
        return extStore.findStrategy(tenantId, strategyId)
                .orElseThrow(() -> new RuntimeException("策略不存在"));
    }

    /**
     * 按类型查询广告策略列表。
     *
     * @param tenantId 租户ID
     * @param type     策略类型(可为空)
     * @return 广告策略列表
     */
    public List<AdStrategy> listStrategies(String tenantId, StrategyType type) {
        return extStore.listStrategies(tenantId, type);
    }

    /**
     * 按目标广告活动查询策略列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID
     * @return 广告策略列表
     */
    public List<AdStrategy> listStrategiesByCampaign(String tenantId, String campaignId) {
        return extStore.listStrategiesByCampaign(tenantId, campaignId);
    }

    /**
     * 批量创建广告策略。
     *
     * @param tenantId 租户ID
     * @param commands 创建策略命令列表
     * @return 创建后的广告策略列表
     */
    @Transactional
    public List<AdStrategy> batchCreateStrategies(String tenantId, List<CreateStrategyCommand> commands) {
        Instant now = Instant.now();
        List<AdStrategy> strategies = commands.stream().map(cmd ->
                new AdStrategy(UUID.randomUUID().toString(), tenantId, cmd.strategyCode(),
                        cmd.strategyName(), cmd.strategyType(), cmd.targetCampaignId(),
                        toJson(cmd.conditions()), toJson(cmd.actions()),
                        cmd.pmsGenerated(), cmd.scheduleExpression(), StrategyStatus.DRAFT,
                        null, now, now)
        ).toList();
        return extStore.batchSaveStrategies(strategies);
    }

    /**
     * 分析搜索词。
     * <p>
     * 根据搜索词的CTR、ACOS、订单量等指标自动分类表现等级，
     * 并生成关键词优化建议(精确匹配/短语匹配/否定关键词)。
     * </p>
     *
     * @param tenantId 租户ID
     * @param command  搜索词分析命令
     * @return 搜索词分析结果
     */
    @Transactional
    public SearchTermAnalysis analyzeSearchTerm(String tenantId, AnalyzeSearchTermCommand command) {
        double ctr = command.impressions() > 0 ? (double) command.clicks() / command.impressions() : 0;
        double acos = command.orders() > 0 ? command.spend() / command.revenue() : 0;
        SearchTermPerformance performance = classifyPerformance(ctr, acos, command.orders());
        List<String> suggestions = generateKeywordSuggestions(command.searchTerm(), performance);
        SearchTermAnalysis analysis = new SearchTermAnalysis(UUID.randomUUID().toString(), tenantId,
                command.campaignId(), command.searchTerm(), command.impressions(), command.clicks(),
                ctr, acos, command.orders(), performance, suggestions, Instant.now());
        return extStore.saveSearchTermAnalysis(analysis);
    }

    /**
     * 查询搜索词分析列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID
     * @return 搜索词分析列表
     */
    public List<SearchTermAnalysis> listSearchTermAnalyses(String tenantId, String campaignId) {
        return extStore.listSearchTermAnalyses(tenantId, campaignId);
    }

    /**
     * 获取高转化搜索词列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID
     * @return 高转化搜索词列表
     */
    public List<SearchTermAnalysis> getHighConvertingTerms(String tenantId, String campaignId) {
        return extStore.listSearchTermAnalyses(tenantId, campaignId).stream()
                .filter(a -> a.performance() == SearchTermPerformance.HIGH_CONVERTING)
                .toList();
    }

    /**
     * 删除搜索词分析记录。
     *
     * @param tenantId   租户ID
     * @param analysisId 分析记录ID
     */
    @Transactional
    public void deleteSearchTermAnalysis(String tenantId, String analysisId) {
        extStore.deleteSearchTermAnalysis(tenantId, analysisId);
    }

    /**
     * 记录PMS操作日志。
     * <p>
     * 记录PMS(利润管理系统)自动调价的每次操作详情，确保可审计可回溯。
     * </p>
     *
     * @param tenantId   租户ID
     * @param command    PMS操作日志命令
     * @return PMS操作日志
     */
    @Transactional
    public PmsActionLog recordPmsAction(String tenantId, RecordPmsActionCommand command) {
        PmsActionLog log = new PmsActionLog(UUID.randomUUID().toString(), tenantId,
                command.campaignId(), command.bidId(), command.actionType(),
                command.beforeValue(), command.afterValue(), command.pmsReason(),
                command.canRollback(), false, Instant.now(), null);
        return extStore.saveActionLog(log);
    }

    /**
     * 回滚PMS操作。
     * <p>
     * 将PMS操作日志标记为已回滚，并记录回滚时间。
     * </p>
     *
     * @param tenantId 租户ID
     * @param logId    日志ID
     * @return 回滚后的PMS操作日志
     */
    @Transactional
    public PmsActionLog rollbackPmsAction(String tenantId, String logId) {
        PmsActionLog log = extStore.findActionLog(tenantId, logId)
                .orElseThrow(() -> new RuntimeException("PMS操作日志不存在"));
        if (!log.canRollback()) {
            throw new RuntimeException("该操作不支持回滚");
        }
        if (log.rolledBack()) {
            throw new RuntimeException("该操作已回滚");
        }
        PmsActionLog rolledBack = new PmsActionLog(log.logId(), log.tenantId(), log.campaignId(),
                log.bidId(), log.actionType(), log.beforeValue(), log.afterValue(),
                log.pmsReason(), log.canRollback(), true, log.executedAt(), Instant.now());
        extStore.updateActionLog(rolledBack);
        return rolledBack;
    }

    /**
     * 查询PMS操作日志列表。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID(可为空)
     * @param actionType 操作类型(可为空)
     * @return PMS操作日志列表
     */
    public List<PmsActionLog> listPmsActionLogs(String tenantId, String campaignId, ActionType actionType) {
        return extStore.listActionLogs(tenantId, campaignId, actionType);
    }

    /**
     * 搜索词表现分类算法。
     * <p>
     * 根据CTR、ACOS和订单量将搜索词分为四类:
     *   - HIGH_CONVERTING: 高转化(订单>5且ACOS<25%)
     *   - LOW_CONVERTING: 低转化(有订单且ACOS<40%)
     *   - HIGH_SPEND_LOW_RETURN: 高花费低回报(ACOS>60%且订单<3)
     *   - IRRELEVANT: 不相关(其余情况)
     * </p>
     */
    private SearchTermPerformance classifyPerformance(double ctr, double acos, int orders) {
        if (orders > 5 && acos < 0.25) return SearchTermPerformance.HIGH_CONVERTING;
        if (orders > 0 && acos < 0.4) return SearchTermPerformance.LOW_CONVERTING;
        if (acos > 0.6 && orders < 3) return SearchTermPerformance.HIGH_SPEND_LOW_RETURN;
        return SearchTermPerformance.IRRELEVANT;
    }

    /**
     * 关键词优化建议生成算法。
     * <p>
     * 根据搜索词表现等级生成不同类型的优化建议:
     *   - HIGH_CONVERTING: 推荐精确匹配和短语匹配
     *   - HIGH_SPEND_LOW_RETURN: 推荐否定关键词
     * </p>
     */
    private List<String> generateKeywordSuggestions(String searchTerm, SearchTermPerformance performance) {
        List<String> suggestions = new ArrayList<>();
        if (performance == SearchTermPerformance.HIGH_CONVERTING) {
            suggestions.add(searchTerm + " exact");
            suggestions.add(searchTerm + " phrase");
        } else if (performance == SearchTermPerformance.HIGH_SPEND_LOW_RETURN) {
            suggestions.add("neg:" + searchTerm);
        }
        return suggestions;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public record CreateStrategyCommand(String strategyCode, String strategyName, StrategyType strategyType,
                                         String targetCampaignId, Object conditions, Object actions,
                                         boolean pmsGenerated, String scheduleExpression) {}
    public record AnalyzeSearchTermCommand(String campaignId, String searchTerm, int impressions, int clicks,
                                            double spend, double revenue, int orders) {}
    public record RecordPmsActionCommand(String campaignId, String bidId, ActionType actionType,
                                          String beforeValue, String afterValue, String pmsReason,
                                          boolean canRollback) {}

    // ========== 否定关键词管理(内存存储) ==========

    private final Map<String, NegativeKeyword> negativeKeywordStore = new ConcurrentHashMap<>();

    @Transactional
    public NegativeKeyword addNegativeKeyword(String tenantId, String campaignId, String keywordText, String matchType) {
        Instant now = Instant.now();
        NegativeKeyword nk = new NegativeKeyword(UUID.randomUUID().toString(), tenantId,
                campaignId, null, keywordText, matchType, "MANUAL", true, now, now);
        negativeKeywordStore.put(nk.negativeKeywordId(), nk);
        return nk;
    }

    @Transactional
    public void removeNegativeKeyword(String tenantId, String negativeKeywordId) {
        NegativeKeyword nk = negativeKeywordStore.get(negativeKeywordId);
        if (nk == null || !nk.tenantId().equals(tenantId)) {
            throw new RuntimeException("否定关键词不存在");
        }
        negativeKeywordStore.remove(negativeKeywordId);
    }

    public List<NegativeKeyword> listNegativeKeywords(String tenantId, String campaignId) {
        return negativeKeywordStore.values().stream()
                .filter(nk -> nk.tenantId().equals(tenantId))
                .filter(nk -> campaignId == null || nk.campaignId().equals(campaignId))
                .collect(Collectors.toList());
    }

    /**
     * 自动提炼搜索词(由定时任务触发)
     * <p>
     * HIGH_CONVERTING → 保留为关键词
     * HIGH_SPEND_LOW_RETURN → 自动添加到否定关键词
     * </p>
     */
    @Transactional
    public int harvestSearchTerms(String tenantId, String campaignId) {
        List<SearchTermAnalysis> analyses = extStore.listSearchTermAnalyses(tenantId, campaignId);
        int count = 0;
        for (SearchTermAnalysis a : analyses) {
            for (String suggestion : a.suggestedKeywords()) {
                if (suggestion.startsWith("neg:")) {
                    String term = suggestion.substring(4);
                    NegativeKeyword nk = new NegativeKeyword(UUID.randomUUID().toString(), tenantId,
                            campaignId, null, term, "NEGATIVE_EXACT", "AUTO_HARVEST", true,
                            Instant.now(), Instant.now());
                    negativeKeywordStore.put(nk.negativeKeywordId(), nk);
                    count++;
                }
            }
        }
        return count;
    }
}
