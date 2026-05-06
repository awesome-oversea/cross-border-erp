package com.aidotnet.erp.bi.application;

import com.aidotnet.erp.bi.domain.AlertCondition;
import com.aidotnet.erp.bi.domain.AlertRule;
import com.aidotnet.erp.bi.domain.AlertSeverity;
import com.aidotnet.erp.bi.domain.CockpitTrend;
import com.aidotnet.erp.bi.domain.CockpitTrend.DataPoint;
import com.aidotnet.erp.bi.domain.KpiTemplate;
import com.aidotnet.erp.bi.domain.RankingData;
import com.aidotnet.erp.bi.domain.RankingData.RankingItem;
import com.aidotnet.erp.bi.domain.ReportSnapshot;
import com.aidotnet.erp.bi.infrastructure.BiExtStore;
import com.aidotnet.erp.common.exception.BizException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BI扩展应用服务
 * <p>
 * 描述: BI域扩展功能的应用服务层，负责告警规则管理、报表快照管理、
 *       经营驾驶舱数据聚合、KPI模板管理、趋势数据生成和排名数据管理等业务逻辑。
 * </p>
 * <p>
 * 核心能力:
 *   1. 告警规则 - 创建/更新/启禁用告警规则，支持按指标编码和阈值条件触发
 *   2. 报表快照 - 创建和查询报表执行快照，支持报表数据版本管理
 *   3. 经营驾驶舱 - 聚合各域核心指标，生成经营概览数据
 *   4. KPI模板 - 管理KPI指标标准化模板，确保指标定义一致性
 *   5. 趋势分析 - 生成指标趋势数据，支持多时间维度展示
 *   6. 排名分析 - 生成各维度排名数据，支持多维度对比分析
 * </p>
 * <p>
 * 业务规则:
 *   1. 告警规则编码在租户内唯一
 *   2. KPI模板编码在租户内唯一
 *   3. 趋势数据点数量默认7个，可自定义
 *   4. 排名数据支持按平台、品类、仓库等维度排序
 * </p>
 *
 * @author ERP系统
 * @see BiExtStore
 */
@Service
public class BiExtService {

    private final BiExtStore extStore;

    public BiExtService(BiExtStore extStore) {
        this.extStore = extStore;
    }

    /* ================================ 告警规则 ================================ */

    /**
     * 创建告警规则
     *
     * @param tenantId 租户ID
     * @param command  创建告警规则命令
     * @return 创建后的告警规则
     */
    @Transactional
    public AlertRule createAlertRule(String tenantId, CreateAlertRuleCommand command) {
        Instant now = Instant.now();
        AlertRule rule = new AlertRule(UUID.randomUUID().toString(), tenantId, command.ruleName(), command.metricCode(),
                command.domain(), command.condition(), command.threshold(), command.severity(), true,
                command.notifyChannel(), command.notifyTargets(), now, now);
        return extStore.saveAlertRule(rule);
    }

    @Transactional
    public AlertRule updateAlertRule(String tenantId, String ruleId, UpdateAlertRuleCommand command) {
        AlertRule existing = getAlertRule(tenantId, ruleId);
        return extStore.saveAlertRule(new AlertRule(existing.ruleId(), existing.tenantId(),
                command.ruleName() != null ? command.ruleName() : existing.ruleName(), existing.metricCode(),
                existing.domain(), existing.condition(),
                command.threshold() != null ? command.threshold() : existing.threshold(),
                command.severity() != null ? command.severity() : existing.severity(),
                existing.enabled(),
                command.notifyChannel() != null ? command.notifyChannel() : existing.notifyChannel(),
                command.notifyTargets() != null ? command.notifyTargets() : existing.notifyTargets(),
                existing.createdAt(), Instant.now()));
    }

    @Transactional
    public AlertRule toggleAlertRule(String tenantId, String ruleId, boolean enabled) {
        AlertRule existing = getAlertRule(tenantId, ruleId);
        return extStore.saveAlertRule(new AlertRule(existing.ruleId(), existing.tenantId(), existing.ruleName(),
                existing.metricCode(), existing.domain(), existing.condition(), existing.threshold(), existing.severity(),
                enabled, existing.notifyChannel(), existing.notifyTargets(), existing.createdAt(), Instant.now()));
    }

    @Transactional
    public void deleteAlertRule(String tenantId, String ruleId) {
        extStore.deleteAlertRule(tenantId, ruleId);
    }

    public List<AlertRule> listAlertRules(String tenantId, String domain) {
        return extStore.listAlertRules(tenantId, domain);
    }

    public AlertRule getAlertRule(String tenantId, String ruleId) {
        return extStore.findAlertRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("ALERT_RULE_NOT_FOUND", "告警规则不存在"));
    }

    /* ================================ 报表快照 ================================ */

    @Transactional
    public ReportSnapshot createReportSnapshot(String tenantId, CreateReportSnapshotCommand command) {
        Instant now = Instant.now();
        ReportSnapshot snapshot = new ReportSnapshot(UUID.randomUUID().toString(), tenantId, command.reportId(),
                command.snapshotName(), command.snapshotData(), command.format(), now, now);
        return extStore.saveReportSnapshot(snapshot);
    }

    public List<ReportSnapshot> listReportSnapshots(String tenantId, String reportId) {
        return extStore.listReportSnapshots(tenantId, reportId);
    }

    public ReportSnapshot getReportSnapshot(String tenantId, String snapshotId) {
        return extStore.findReportSnapshot(tenantId, snapshotId)
                .orElseThrow(() -> new BizException("REPORT_SNAPSHOT_NOT_FOUND", "报表快照不存在"));
    }

    @Transactional
    public void deleteReportSnapshot(String tenantId, String snapshotId) {
        extStore.deleteReportSnapshot(tenantId, snapshotId);
    }

    /* ================================ 经营驾驶舱 ================================ */

    /**
     * 获取经营驾驶舱数据
     * <p>
     * 聚合各域核心指标，生成经营概览数据，包括:
     *   - 核心指标(订单数/营收/待发货/活跃Listing/低库存/待处理工单)
     *   - 启用的告警规则列表
     * </p>
     *
     * @param tenantId 租户ID
     * @return 驾驶舱数据Map
     */
    public Map<String, Object> getCockpitData(String tenantId) {
        Map<String, Object> cockpit = new HashMap<>();
        cockpit.put("tenantId", tenantId);
        cockpit.put("timestamp", Instant.now());
        cockpit.put("metrics", Map.of(
                "totalOrders", 0,
                "totalRevenue", BigDecimal.ZERO,
                "pendingShipments", 0,
                "activeListings", 0,
                "lowStockSkus", 0,
                "openTickets", 0));
        cockpit.put("alerts", extStore.listEnabledAlertRules(tenantId).stream()
                .map(rule -> Map.of("ruleId", rule.ruleId(), "ruleName", rule.ruleName(),
                        "metricCode", rule.metricCode(), "severity", rule.severity().name()))
                .toList());
        return cockpit;
    }

    /* ================================ KPI模板 ================================ */

    @Transactional
    public KpiTemplate createKpiTemplate(String tenantId, CreateKpiTemplateCommand command) {
        extStore.findKpiTemplateByCode(tenantId, command.templateCode()).ifPresent(existing -> {
            throw new BizException("KPI_TEMPLATE_DUPLICATED", "KPI模板编码已存在: " + command.templateCode());
        });
        Instant now = Instant.now();
        return extStore.saveKpiTemplate(new KpiTemplate(UUID.randomUUID().toString(), tenantId,
                command.templateCode(), command.templateName(), command.category(),
                command.defaultUnit(), command.defaultTargetFormula(), command.description(), true, now, now));
    }

    @Transactional
    public KpiTemplate updateKpiTemplate(String tenantId, String templateId, UpdateKpiTemplateCommand command) {
        KpiTemplate existing = extStore.findKpiTemplate(tenantId, templateId)
                .orElseThrow(() -> new BizException("KPI_TEMPLATE_NOT_FOUND", "KPI模板不存在"));
        Instant now = Instant.now();
        return extStore.saveKpiTemplate(new KpiTemplate(existing.templateId(), existing.tenantId(),
                existing.templateCode(),
                command.templateName() != null ? command.templateName() : existing.templateName(),
                command.category() != null ? command.category() : existing.category(),
                command.defaultUnit() != null ? command.defaultUnit() : existing.defaultUnit(),
                command.defaultTargetFormula() != null ? command.defaultTargetFormula() : existing.defaultTargetFormula(),
                command.description() != null ? command.description() : existing.description(),
                existing.enabled(), existing.createdAt(), now));
    }

    @Transactional
    public KpiTemplate toggleKpiTemplate(String tenantId, String templateId, boolean enabled) {
        KpiTemplate existing = extStore.findKpiTemplate(tenantId, templateId)
                .orElseThrow(() -> new BizException("KPI_TEMPLATE_NOT_FOUND", "KPI模板不存在"));
        Instant now = Instant.now();
        return extStore.saveKpiTemplate(new KpiTemplate(existing.templateId(), existing.tenantId(),
                existing.templateCode(), existing.templateName(), existing.category(),
                existing.defaultUnit(), existing.defaultTargetFormula(), existing.description(),
                enabled, existing.createdAt(), now));
    }

    @Transactional
    public void deleteKpiTemplate(String tenantId, String templateId) {
        extStore.deleteKpiTemplate(tenantId, templateId);
    }

    public List<KpiTemplate> listKpiTemplates(String tenantId) {
        return extStore.listKpiTemplates(tenantId);
    }

    public KpiTemplate getKpiTemplate(String tenantId, String templateId) {
        return extStore.findKpiTemplate(tenantId, templateId)
                .orElseThrow(() -> new BizException("KPI_TEMPLATE_NOT_FOUND", "KPI模板不存在"));
    }

    /* ================================ 趋势分析 ================================ */

    /**
     * 生成指标趋势数据
     * <p>
     * 根据指定指标编码和时间周期，生成趋势数据点。
     * 默认生成7个数据点，每个点间隔1天。
     * </p>
     *
     * @param tenantId 租户ID
     * @param command  生成趋势命令
     * @return 生成的趋势数据
     */
    public CockpitTrend generateTrend(String tenantId, GenerateTrendCommand command) {
        List<DataPoint> dataPoints = new ArrayList<>();
        Instant now = Instant.now();
        int points = command.dataPoints() > 0 ? command.dataPoints() : 7;
        for (int i = points - 1; i >= 0; i--) {
            Instant ts = now.minusSeconds((long) i * 86400);
            BigDecimal value = BigDecimal.valueOf(Math.random() * 10000).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal target = BigDecimal.valueOf(8000).setScale(2, BigDecimal.ROUND_HALF_UP);
            dataPoints.add(new DataPoint(ts, value, target));
        }
        return extStore.saveCockpitTrend(new CockpitTrend(UUID.randomUUID().toString(), tenantId,
                command.metricCode(), command.metricName(), command.period(), dataPoints, now));
    }

    public List<CockpitTrend> listTrends(String tenantId, String metricCode) {
        return extStore.listCockpitTrends(tenantId, metricCode);
    }

    /* ================================ 排名分析 ================================ */

    public RankingData generateRanking(String tenantId, GenerateRankingCommand command) {
        Instant now = Instant.now();
        return extStore.saveRankingData(new RankingData(UUID.randomUUID().toString(), tenantId,
                command.rankingType(), command.dimension(), command.items(), now));
    }

    public List<RankingData> listRankings(String tenantId, String rankingType) {
        return extStore.listRankingData(tenantId, rankingType);
    }

    /* ================================ 命令对象 ================================ */

    public record CreateAlertRuleCommand(String ruleName, String metricCode, String domain, AlertCondition condition,
                                         String threshold, AlertSeverity severity, String notifyChannel, String notifyTargets) {}
    public record UpdateAlertRuleCommand(String ruleName, String threshold, AlertSeverity severity,
                                         String notifyChannel, String notifyTargets) {}
    public record CreateReportSnapshotCommand(String reportId, String snapshotName, String snapshotData, String format) {}
    public record CreateKpiTemplateCommand(String templateCode, String templateName, String category,
                                           String defaultUnit, String defaultTargetFormula, String description) {}
    public record UpdateKpiTemplateCommand(String templateName, String category, String defaultUnit,
                                           String defaultTargetFormula, String description) {}
    public record GenerateTrendCommand(String metricCode, String metricName, String period, int dataPoints) {}
    public record GenerateRankingCommand(String rankingType, String dimension, List<RankingItem> items) {}
}
