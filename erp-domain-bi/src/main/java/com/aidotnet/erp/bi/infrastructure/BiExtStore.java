package com.aidotnet.erp.bi.infrastructure;

import com.aidotnet.erp.bi.domain.AlertCondition;
import com.aidotnet.erp.bi.domain.AlertRule;
import com.aidotnet.erp.bi.domain.AlertSeverity;
import com.aidotnet.erp.bi.domain.CockpitTrend;
import com.aidotnet.erp.bi.domain.CustomReport;
import com.aidotnet.erp.bi.domain.DataExportTask;
import com.aidotnet.erp.bi.domain.DeveloperCommissionReport;
import com.aidotnet.erp.bi.domain.Dimension;
import com.aidotnet.erp.bi.domain.KpiAssessment;
import com.aidotnet.erp.bi.domain.KpiStatus;
import com.aidotnet.erp.bi.domain.KpiTarget;
import com.aidotnet.erp.bi.domain.KpiTemplate;
import com.aidotnet.erp.bi.domain.MetricCaliber;
import com.aidotnet.erp.bi.domain.MetricCaliberValue;
import com.aidotnet.erp.bi.domain.RankingData;
import com.aidotnet.erp.bi.domain.ReportSnapshot;
import com.aidotnet.erp.bi.infrastructure.data.AlertRuleDO;
import com.aidotnet.erp.bi.infrastructure.data.CockpitTrendDO;
import com.aidotnet.erp.bi.infrastructure.data.CustomReportDO;
import com.aidotnet.erp.bi.infrastructure.data.DeveloperCommissionReportDO;
import com.aidotnet.erp.bi.infrastructure.data.KpiAssessmentDO;
import com.aidotnet.erp.bi.infrastructure.data.KpiTargetDO;
import com.aidotnet.erp.bi.infrastructure.data.KpiTemplateDO;
import com.aidotnet.erp.bi.infrastructure.data.MetricCaliberDO;
import com.aidotnet.erp.bi.infrastructure.data.MetricCaliberValueDO;
import com.aidotnet.erp.bi.infrastructure.data.RankingDataDO;
import com.aidotnet.erp.bi.infrastructure.data.ReportSnapshotDO;
import com.aidotnet.erp.bi.infrastructure.mapper.BiExtMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * BI扩展数据存储层
 * <p>
 * 描述: BI域扩展功能的数据持久化层，负责告警规则、报表快照、数据导出任务、
 *       KPI模板、驾驶舱趋势、排名数据、指标口径、口径计算值、KPI目标和
 *       KPI考核等实体的CRUD操作。所有数据通过MyBatis持久化到数据库。
 * </p>
 * <p>
 * 设计说明:
 *   1. 采用DO(数据对象)与领域对象分离模式，通过转换方法实现层间数据映射
 *   2. JSONB字段(如dataPoints/items/dimensions)通过Jackson序列化/反序列化处理
 *   3. 所有操作均带租户隔离(tenantId)，确保多租户数据安全
 *   4. save方法采用upsert模式(存在则更新，不存在则插入)
 * </p>
 * <p>
 * 迁移记录:
 *   原ConcurrentHashMap内存存储已迁移至MyBatis数据库持久化，
 *   解决了服务重启数据丢失、集群数据不一致等问题。
 * </p>
 *
 * @author ERP系统
 * @see BiExtMapper
 */
@Repository
public class BiExtStore {

    private static final Logger log = LoggerFactory.getLogger(BiExtStore.class);

    private final BiExtMapper mapper;
    private final ObjectMapper objectMapper;

    public BiExtStore(BiExtMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /* ================================ 告警规则 ================================ */

    /**
     * 保存告警规则(存在则更新，不存在则插入)
     *
     * @param rule 告警规则领域对象
     * @return 保存后的告警规则
     */
    public AlertRule saveAlertRule(AlertRule rule) {
        AlertRuleDO existing = mapper.selectAlertRule(rule.tenantId(), rule.ruleId());
        AlertRuleDO data = toAlertRuleData(rule);
        if (existing == null) {
            mapper.insertAlertRule(data);
        } else {
            mapper.updateAlertRule(data);
        }
        return rule;
    }

    public Optional<AlertRule> findAlertRule(String tenantId, String ruleId) {
        return Optional.ofNullable(mapper.selectAlertRule(tenantId, ruleId)).map(this::toAlertRuleDomain);
    }

    public List<AlertRule> listAlertRules(String tenantId, String domain) {
        return mapper.selectAlertRules(tenantId, domain).stream().map(this::toAlertRuleDomain).collect(Collectors.toList());
    }

    public List<AlertRule> listEnabledAlertRules(String tenantId) {
        return mapper.selectEnabledAlertRules(tenantId).stream().map(this::toAlertRuleDomain).collect(Collectors.toList());
    }

    public void deleteAlertRule(String tenantId, String ruleId) {
        mapper.deleteAlertRule(tenantId, ruleId);
    }

    /* ================================ 报表快照 ================================ */

    public ReportSnapshot saveReportSnapshot(ReportSnapshot snapshot) {
        mapper.insertReportSnapshot(toReportSnapshotData(snapshot));
        return snapshot;
    }

    public Optional<ReportSnapshot> findReportSnapshot(String tenantId, String snapshotId) {
        return Optional.ofNullable(mapper.selectReportSnapshot(tenantId, snapshotId)).map(this::toReportSnapshotDomain);
    }

    public List<ReportSnapshot> listReportSnapshots(String tenantId, String reportId) {
        return mapper.selectReportSnapshots(tenantId, reportId).stream().map(this::toReportSnapshotDomain).collect(Collectors.toList());
    }

    public void deleteReportSnapshot(String tenantId, String snapshotId) {
        mapper.deleteReportSnapshot(tenantId, snapshotId);
    }

    /* ================================ 数据导出任务 ================================ */

    /* ================================ 自定义报表 ================================ */

    public CustomReport saveCustomReport(CustomReport report) {
        CustomReportDO existing = mapper.selectCustomReport(report.tenantId(), report.reportId());
        CustomReportDO data = toCustomReportData(report);
        if (existing == null) {
            mapper.insertCustomReport(data);
        } else {
            mapper.updateCustomReport(data);
        }
        return report;
    }

    public Optional<CustomReport> findCustomReport(String tenantId, String reportId) {
        return Optional.ofNullable(mapper.selectCustomReport(tenantId, reportId)).map(this::toCustomReportDomain);
    }

    public Optional<CustomReport> findCustomReportByCode(String tenantId, String reportCode) {
        return Optional.ofNullable(mapper.selectCustomReportByCode(tenantId, reportCode)).map(this::toCustomReportDomain);
    }

    public List<CustomReport> listCustomReports(String tenantId, String subjectArea) {
        return mapper.selectCustomReports(tenantId, subjectArea).stream()
                .map(this::toCustomReportDomain)
                .collect(Collectors.toList());
    }

    public void deleteCustomReport(String tenantId, String reportId) {
        mapper.deleteCustomReport(tenantId, reportId);
    }

    /* ================================ 开发提成报表 ================================ */

    public DeveloperCommissionReport saveDeveloperCommissionReport(DeveloperCommissionReport report) {
        mapper.insertDeveloperCommissionReport(toDeveloperCommissionReportData(report));
        return report;
    }

    public Optional<DeveloperCommissionReport> findDeveloperCommissionReport(String tenantId, String reportId) {
        return Optional.ofNullable(mapper.selectDeveloperCommissionReport(tenantId, reportId))
                .map(this::toDeveloperCommissionReportDomain);
    }

    public List<DeveloperCommissionReport> listDeveloperCommissionReports(String tenantId, String period, String userId) {
        return mapper.selectDeveloperCommissionReports(tenantId, period, userId).stream()
                .map(this::toDeveloperCommissionReportDomain)
                .collect(Collectors.toList());
    }

    public void deleteDeveloperCommissionReport(String tenantId, String reportId) {
        mapper.deleteDeveloperCommissionReport(tenantId, reportId);
    }

    public void saveDataExportTask(DataExportTask task) {
        DataExportTask existing = mapper.selectDataExportTask(task.tenantId(), task.taskId());
        if (existing == null) {
            mapper.insertDataExportTask(task);
        } else {
            mapper.updateDataExportTask(task);
        }
    }

    public Optional<DataExportTask> findDataExportTask(String tenantId, String taskId) {
        return Optional.ofNullable(mapper.selectDataExportTask(tenantId, taskId));
    }

    public List<DataExportTask> findDataExportTasks(String tenantId, String status) {
        return mapper.selectDataExportTasks(tenantId, status);
    }

    /* ================================ 维度查询 ================================ */

    public List<Dimension> findDimensions(String tenantId, Boolean enabled) {
        return mapper.selectDimensions(tenantId, enabled);
    }

    public List<ReportSnapshot> findReportSnapshots(String tenantId, String reportId) {
        return mapper.selectReportSnapshots(tenantId, reportId).stream().map(this::toReportSnapshotDomain).collect(Collectors.toList());
    }

    /* ================================ KPI模板 ================================ */

    /**
     * 保存KPI模板(存在则更新，不存在则插入)
     *
     * @param template KPI模板领域对象
     * @return 保存后的KPI模板
     */
    public KpiTemplate saveKpiTemplate(KpiTemplate template) {
        KpiTemplateDO existing = mapper.selectKpiTemplate(template.tenantId(), template.templateId());
        KpiTemplateDO data = toKpiTemplateData(template);
        if (existing == null) {
            mapper.insertKpiTemplate(data);
        } else {
            mapper.updateKpiTemplate(data);
        }
        return template;
    }

    public List<KpiTemplate> listKpiTemplates(String tenantId) {
        return mapper.selectKpiTemplates(tenantId).stream().map(this::toKpiTemplateDomain).collect(Collectors.toList());
    }

    public Optional<KpiTemplate> findKpiTemplate(String tenantId, String templateId) {
        return Optional.ofNullable(mapper.selectKpiTemplate(tenantId, templateId)).map(this::toKpiTemplateDomain);
    }

    public Optional<KpiTemplate> findKpiTemplateByCode(String tenantId, String templateCode) {
        return Optional.ofNullable(mapper.selectKpiTemplateByCode(tenantId, templateCode)).map(this::toKpiTemplateDomain);
    }

    public List<KpiTemplate> listKpiTemplatesByCategory(String tenantId, String category) {
        return mapper.selectKpiTemplatesByCategory(tenantId, category).stream().map(this::toKpiTemplateDomain).collect(Collectors.toList());
    }

    public void deleteKpiTemplate(String tenantId, String templateId) {
        mapper.deleteKpiTemplate(tenantId, templateId);
    }

    /* ================================ 驾驶舱趋势 ================================ */

    /**
     * 保存驾驶舱趋势数据
     * <p>
     * 将DataPoint列表序列化为JSONB格式存储到数据库
     * </p>
     *
     * @param trend 驾驶舱趋势领域对象
     * @return 保存后的驾驶舱趋势
     */
    public CockpitTrend saveCockpitTrend(CockpitTrend trend) {
        mapper.insertCockpitTrend(toCockpitTrendData(trend));
        return trend;
    }

    public Optional<CockpitTrend> findCockpitTrend(String tenantId, String trendId) {
        return Optional.ofNullable(mapper.selectCockpitTrend(tenantId, trendId)).map(this::toCockpitTrendDomain);
    }

    public List<CockpitTrend> listCockpitTrends(String tenantId, String metricCode) {
        return mapper.selectCockpitTrends(tenantId, metricCode).stream().map(this::toCockpitTrendDomain).collect(Collectors.toList());
    }

    public void deleteCockpitTrend(String tenantId, String trendId) {
        mapper.deleteCockpitTrend(tenantId, trendId);
    }

    /* ================================ 排名数据 ================================ */

    /**
     * 保存排名数据
     * <p>
     * 将RankingItem列表序列化为JSONB格式存储到数据库
     * </p>
     *
     * @param ranking 排名数据领域对象
     * @return 保存后的排名数据
     */
    public RankingData saveRankingData(RankingData ranking) {
        mapper.insertRankingData(toRankingDataData(ranking));
        return ranking;
    }

    public Optional<RankingData> findRankingData(String tenantId, String rankingId) {
        return Optional.ofNullable(mapper.selectRankingData(tenantId, rankingId)).map(this::toRankingDataDomain);
    }

    public List<RankingData> listRankingData(String tenantId, String rankingType) {
        return mapper.selectRankingDataList(tenantId, rankingType).stream().map(this::toRankingDataDomain).collect(Collectors.toList());
    }

    public void deleteRankingData(String tenantId, String rankingId) {
        mapper.deleteRankingData(tenantId, rankingId);
    }

    /* ================================ 指标口径 ================================ */

    /**
     * 保存指标口径(存在则更新，不存在则插入)
     *
     * @param caliber 指标口径领域对象
     */
    public void saveMetricCaliber(MetricCaliber caliber) {
        MetricCaliberDO existing = mapper.selectMetricCaliber(caliber.tenantId(), caliber.caliberId());
        MetricCaliberDO data = toMetricCaliberData(caliber);
        if (existing == null) {
            mapper.insertMetricCaliber(data);
        } else {
            mapper.updateMetricCaliber(data);
        }
    }

    public Optional<MetricCaliber> findMetricCaliber(String tenantId, String caliberId) {
        return Optional.ofNullable(mapper.selectMetricCaliber(tenantId, caliberId)).map(this::toMetricCaliberDomain);
    }

    public Optional<MetricCaliber> findMetricCaliberByCode(String tenantId, String metricCode) {
        return Optional.ofNullable(mapper.selectMetricCaliberByCode(tenantId, metricCode)).map(this::toMetricCaliberDomain);
    }

    public List<MetricCaliber> listMetricCalibers(String tenantId, String category) {
        return mapper.selectMetricCalibers(tenantId, category).stream().map(this::toMetricCaliberDomain).collect(Collectors.toList());
    }

    public void deleteMetricCaliber(String tenantId, String caliberId) {
        mapper.deleteMetricCaliber(tenantId, caliberId);
    }

    /* ================================ 口径计算值 ================================ */

    public void saveMetricCaliberValue(MetricCaliberValue value) {
        mapper.insertMetricCaliberValue(toMetricCaliberValueData(value));
    }

    public List<MetricCaliberValue> listMetricCaliberValues(String tenantId, String metricCode) {
        return mapper.selectMetricCaliberValues(tenantId, metricCode).stream().map(this::toMetricCaliberValueDomain).collect(Collectors.toList());
    }

    public void deleteMetricCaliberValue(String tenantId, String valueId) {
        mapper.deleteMetricCaliberValue(tenantId, valueId);
    }

    /* ================================ KPI目标 ================================ */

    /**
     * 保存KPI目标(存在则更新，不存在则插入)
     *
     * @param target KPI目标领域对象
     */
    public void saveKpiTarget(KpiTarget target) {
        KpiTargetDO existing = mapper.selectKpiTarget(target.tenantId(), target.targetId());
        KpiTargetDO data = toKpiTargetData(target);
        if (existing == null) {
            mapper.insertKpiTarget(data);
        } else {
            mapper.updateKpiTarget(data);
        }
    }

    public Optional<KpiTarget> findKpiTarget(String tenantId, String targetId) {
        return Optional.ofNullable(mapper.selectKpiTarget(tenantId, targetId)).map(this::toKpiTargetDomain);
    }

    public List<KpiTarget> listKpiTargets(String tenantId, String department, String period) {
        return mapper.selectKpiTargets(tenantId, department, period).stream().map(this::toKpiTargetDomain).collect(Collectors.toList());
    }

    public void deleteKpiTarget(String tenantId, String targetId) {
        mapper.deleteKpiTarget(tenantId, targetId);
    }

    /* ================================ KPI考核 ================================ */

    public void saveKpiAssessment(KpiAssessment assessment) {
        mapper.insertKpiAssessment(toKpiAssessmentData(assessment));
    }

    public Optional<KpiAssessment> findKpiAssessment(String tenantId, String assessmentId) {
        return Optional.ofNullable(mapper.selectKpiAssessment(tenantId, assessmentId)).map(this::toKpiAssessmentDomain);
    }

    public List<KpiAssessment> listKpiAssessments(String tenantId, String userId, String period) {
        return mapper.selectKpiAssessments(tenantId, userId, period).stream().map(this::toKpiAssessmentDomain).collect(Collectors.toList());
    }

    public List<KpiAssessment> listKpiAssessmentsByTarget(String tenantId, String targetId) {
        return mapper.selectKpiAssessmentsByTarget(tenantId, targetId).stream().map(this::toKpiAssessmentDomain).collect(Collectors.toList());
    }

    public void deleteKpiAssessment(String tenantId, String assessmentId) {
        mapper.deleteKpiAssessment(tenantId, assessmentId);
    }

    /* ================================ DO与领域对象转换方法 ================================ */

    private CustomReportDO toCustomReportData(CustomReport report) {
        CustomReportDO data = new CustomReportDO();
        data.setReportId(report.reportId());
        data.setTenantId(report.tenantId());
        data.setReportCode(report.reportCode());
        data.setReportName(report.reportName());
        data.setSubjectArea(report.subjectArea());
        data.setVisibility(report.visibility());
        data.setPermissionCode(report.permissionCode());
        data.setDataLevel(report.dataLevel());
        data.setDescription(report.description());
        data.setEnabled(report.enabled());
        data.setLastRunSnapshotId(report.lastRunSnapshotId());
        data.setLastRunAt(report.lastRunAt());
        data.setCreatedAt(report.createdAt() != null ? report.createdAt() : Instant.now());
        data.setUpdatedAt(report.updatedAt() != null ? report.updatedAt() : Instant.now());
        try {
            data.setDimensions(objectMapper.writeValueAsString(report.dimensions() != null ? report.dimensions() : List.of()));
            data.setMetrics(objectMapper.writeValueAsString(report.metrics() != null ? report.metrics() : List.of()));
            data.setFilters(objectMapper.writeValueAsString(report.filters() != null ? report.filters() : Collections.emptyMap()));
            data.setSorts(objectMapper.writeValueAsString(report.sorts() != null ? report.sorts() : List.of()));
        } catch (JsonProcessingException e) {
            log.error("序列化自定义报表配置失败: reportId={}", report.reportId(), e);
            data.setDimensions("[]");
            data.setMetrics("[]");
            data.setFilters("{}");
            data.setSorts("[]");
        }
        return data;
    }

    private CustomReport toCustomReportDomain(CustomReportDO data) {
        List<String> dimensions = List.of();
        List<String> metrics = List.of();
        Map<String, Object> filters = Collections.emptyMap();
        List<String> sorts = List.of();
        try {
            if (data.getDimensions() != null) {
                dimensions = objectMapper.readValue(data.getDimensions(), new TypeReference<List<String>>() {});
            }
            if (data.getMetrics() != null) {
                metrics = objectMapper.readValue(data.getMetrics(), new TypeReference<List<String>>() {});
            }
            if (data.getFilters() != null) {
                filters = objectMapper.readValue(data.getFilters(), new TypeReference<Map<String, Object>>() {});
            }
            if (data.getSorts() != null) {
                sorts = objectMapper.readValue(data.getSorts(), new TypeReference<List<String>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化自定义报表配置失败: reportId={}", data.getReportId(), e);
        }
        return new CustomReport(
                data.getReportId(),
                data.getTenantId(),
                data.getReportCode(),
                data.getReportName(),
                data.getSubjectArea(),
                dimensions,
                metrics,
                filters,
                sorts,
                data.getVisibility(),
                data.getPermissionCode(),
                data.getDataLevel(),
                data.getDescription(),
                data.isEnabled(),
                data.getLastRunSnapshotId(),
                data.getLastRunAt(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private DeveloperCommissionReportDO toDeveloperCommissionReportData(DeveloperCommissionReport report) {
        DeveloperCommissionReportDO data = new DeveloperCommissionReportDO();
        data.setReportId(report.reportId());
        data.setTenantId(report.tenantId());
        data.setUserId(report.userId());
        data.setUserName(report.userName());
        data.setTeamCode(report.teamCode());
        data.setPeriod(report.period());
        data.setSkuCount(report.skuCount());
        data.setOrderCount(report.orderCount());
        data.setOrderRate(report.orderRate());
        data.setSalesProfit(report.salesProfit());
        data.setKpiScore(report.kpiScore());
        data.setBaseCommissionRate(report.baseCommissionRate());
        data.setCommissionCoefficient(report.commissionCoefficient());
        data.setCommissionAmount(report.commissionAmount());
        data.setCurrency(report.currency());
        data.setCreatedAt(report.createdAt() != null ? report.createdAt() : Instant.now());
        return data;
    }

    private DeveloperCommissionReport toDeveloperCommissionReportDomain(DeveloperCommissionReportDO data) {
        return new DeveloperCommissionReport(
                data.getReportId(),
                data.getTenantId(),
                data.getUserId(),
                data.getUserName(),
                data.getTeamCode(),
                data.getPeriod(),
                data.getSkuCount() != null ? data.getSkuCount() : 0,
                data.getOrderCount() != null ? data.getOrderCount() : 0,
                data.getOrderRate(),
                data.getSalesProfit(),
                data.getKpiScore(),
                data.getBaseCommissionRate(),
                data.getCommissionCoefficient(),
                data.getCommissionAmount(),
                data.getCurrency(),
                data.getCreatedAt());
    }

    private AlertRuleDO toAlertRuleData(AlertRule r) {
        AlertRuleDO data = new AlertRuleDO();
        data.setRuleId(r.ruleId());
        data.setTenantId(r.tenantId());
        data.setRuleName(r.ruleName());
        data.setMetricCode(r.metricCode());
        data.setDomain(r.domain());
        data.setCondition(r.condition().name());
        data.setThreshold(r.threshold());
        data.setSeverity(r.severity().name());
        data.setEnabled(r.enabled());
        data.setNotifyChannel(r.notifyChannel());
        data.setNotifyTargets(r.notifyTargets());
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private AlertRule toAlertRuleDomain(AlertRuleDO d) {
        return new AlertRule(d.getRuleId(), d.getTenantId(), d.getRuleName(), d.getMetricCode(), d.getDomain(),
                AlertCondition.valueOf(d.getCondition()), d.getThreshold(), AlertSeverity.valueOf(d.getSeverity()),
                d.isEnabled(), d.getNotifyChannel(), d.getNotifyTargets(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private ReportSnapshotDO toReportSnapshotData(ReportSnapshot s) {
        ReportSnapshotDO data = new ReportSnapshotDO();
        data.setSnapshotId(s.snapshotId());
        data.setTenantId(s.tenantId());
        data.setReportId(s.reportId());
        data.setSnapshotName(s.snapshotName());
        data.setSnapshotData(s.snapshotData());
        data.setFormat(s.format());
        data.setSnapshotAt(s.snapshotAt());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        return data;
    }

    private ReportSnapshot toReportSnapshotDomain(ReportSnapshotDO d) {
        return new ReportSnapshot(d.getSnapshotId(), d.getTenantId(), d.getReportId(), d.getSnapshotName(),
                d.getSnapshotData(), d.getFormat(), d.getSnapshotAt(), d.getCreatedAt());
    }

    private KpiTemplateDO toKpiTemplateData(KpiTemplate t) {
        KpiTemplateDO data = new KpiTemplateDO();
        data.setTemplateId(t.templateId());
        data.setTenantId(t.tenantId());
        data.setTemplateCode(t.templateCode());
        data.setTemplateName(t.templateName());
        data.setCategory(t.category());
        data.setDefaultUnit(t.defaultUnit());
        data.setDefaultTargetFormula(t.defaultTargetFormula());
        data.setDescription(t.description());
        data.setEnabled(t.enabled());
        data.setCreatedAt(t.createdAt() != null ? t.createdAt() : Instant.now());
        data.setUpdatedAt(t.updatedAt() != null ? t.updatedAt() : Instant.now());
        return data;
    }

    private KpiTemplate toKpiTemplateDomain(KpiTemplateDO d) {
        return new KpiTemplate(d.getTemplateId(), d.getTenantId(), d.getTemplateCode(), d.getTemplateName(),
                d.getCategory(), d.getDefaultUnit(), d.getDefaultTargetFormula(), d.getDescription(),
                d.isEnabled(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private CockpitTrendDO toCockpitTrendData(CockpitTrend t) {
        CockpitTrendDO data = new CockpitTrendDO();
        data.setTrendId(t.trendId());
        data.setTenantId(t.tenantId());
        data.setMetricCode(t.metricCode());
        data.setMetricName(t.metricName());
        data.setPeriod(t.period());
        try {
            data.setDataPoints(objectMapper.writeValueAsString(t.dataPoints()));
        } catch (JsonProcessingException e) {
            log.error("序列化趋势数据点失败: trendId={}", t.trendId(), e);
            data.setDataPoints("[]");
        }
        data.setGeneratedAt(t.generatedAt());
        return data;
    }

    private CockpitTrend toCockpitTrendDomain(CockpitTrendDO d) {
        List<CockpitTrend.DataPoint> dataPoints = List.of();
        try {
            dataPoints = objectMapper.readValue(d.getDataPoints(), new TypeReference<List<CockpitTrend.DataPoint>>() {});
        } catch (JsonProcessingException e) {
            log.error("反序列化趋势数据点失败: trendId={}", d.getTrendId(), e);
        }
        return new CockpitTrend(d.getTrendId(), d.getTenantId(), d.getMetricCode(), d.getMetricName(),
                d.getPeriod(), dataPoints, d.getGeneratedAt());
    }

    private RankingDataDO toRankingDataData(RankingData r) {
        RankingDataDO data = new RankingDataDO();
        data.setRankingId(r.rankingId());
        data.setTenantId(r.tenantId());
        data.setRankingType(r.rankingType());
        data.setDimension(r.dimension());
        try {
            data.setItems(objectMapper.writeValueAsString(r.items()));
        } catch (JsonProcessingException e) {
            log.error("序列化排名数据项失败: rankingId={}", r.rankingId(), e);
            data.setItems("[]");
        }
        data.setGeneratedAt(r.generatedAt());
        return data;
    }

    private RankingData toRankingDataDomain(RankingDataDO d) {
        List<RankingData.RankingItem> items = List.of();
        try {
            items = objectMapper.readValue(d.getItems(), new TypeReference<List<RankingData.RankingItem>>() {});
        } catch (JsonProcessingException e) {
            log.error("反序列化排名数据项失败: rankingId={}", d.getRankingId(), e);
        }
        return new RankingData(d.getRankingId(), d.getTenantId(), d.getRankingType(), d.getDimension(),
                items, d.getGeneratedAt());
    }

    private MetricCaliberDO toMetricCaliberData(MetricCaliber c) {
        MetricCaliberDO data = new MetricCaliberDO();
        data.setCaliberId(c.caliberId());
        data.setTenantId(c.tenantId());
        data.setMetricCode(c.metricCode());
        data.setMetricName(c.metricName());
        data.setCategory(c.category());
        data.setCaliberType(c.caliberType());
        data.setFormula(c.formula());
        data.setFormulaDescription(c.formulaDescription());
        data.setNumeratorMetric(c.numeratorMetric());
        data.setDenominatorMetric(c.denominatorMetric());
        data.setUnit(c.unit());
        data.setDataSource(c.dataSource());
        data.setCalculationScope(c.calculationScope());
        try {
            data.setDimensions(c.dimensions() != null ? objectMapper.writeValueAsString(c.dimensions()) : "[]");
            data.setExcludeConditions(c.excludeConditions() != null ? objectMapper.writeValueAsString(c.excludeConditions()) : "[]");
        } catch (JsonProcessingException e) {
            log.error("序列化口径维度/排除条件失败: caliberId={}", c.caliberId(), e);
            data.setDimensions("[]");
            data.setExcludeConditions("[]");
        }
        data.setPermissionCode(c.permissionCode());
        data.setDataLevel(c.dataLevel());
        data.setEnabled(c.enabled());
        data.setVersion(c.version());
        data.setDescription(c.description());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        data.setUpdatedAt(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        return data;
    }

    private MetricCaliber toMetricCaliberDomain(MetricCaliberDO d) {
        List<String> dimensions = List.of();
        List<String> excludeConditions = List.of();
        try {
            if (d.getDimensions() != null) {
                dimensions = objectMapper.readValue(d.getDimensions(), new TypeReference<List<String>>() {});
            }
            if (d.getExcludeConditions() != null) {
                excludeConditions = objectMapper.readValue(d.getExcludeConditions(), new TypeReference<List<String>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化口径维度/排除条件失败: caliberId={}", d.getCaliberId(), e);
        }
        return new MetricCaliber(d.getCaliberId(), d.getTenantId(), d.getMetricCode(), d.getMetricName(),
                d.getCategory(), d.getCaliberType(), d.getFormula(), d.getFormulaDescription(),
                d.getNumeratorMetric(), d.getDenominatorMetric(), d.getUnit(), d.getDataSource(),
                d.getCalculationScope(), dimensions, excludeConditions, d.getPermissionCode(),
                d.getDataLevel(), d.isEnabled(), d.getVersion(), d.getDescription(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private MetricCaliberValueDO toMetricCaliberValueData(MetricCaliberValue v) {
        MetricCaliberValueDO data = new MetricCaliberValueDO();
        data.setValueId(v.valueId());
        data.setTenantId(v.tenantId());
        data.setCaliberId(v.caliberId());
        data.setMetricCode(v.metricCode());
        data.setCalculatedValue(v.calculatedValue());
        data.setDimensionKey(v.dimensionKey());
        data.setDimensionValue(v.dimensionValue());
        data.setCalculatedAt(v.calculatedAt());
        return data;
    }

    private MetricCaliberValue toMetricCaliberValueDomain(MetricCaliberValueDO d) {
        return new MetricCaliberValue(d.getValueId(), d.getTenantId(), d.getCaliberId(), d.getMetricCode(),
                d.getCalculatedValue(), d.getDimensionKey(), d.getDimensionValue(), d.getCalculatedAt());
    }

    private KpiTargetDO toKpiTargetData(KpiTarget t) {
        KpiTargetDO data = new KpiTargetDO();
        data.setTargetId(t.targetId());
        data.setTenantId(t.tenantId());
        data.setKpiCode(t.kpiCode());
        data.setKpiName(t.kpiName());
        data.setDepartment(t.department());
        data.setRole(t.role());
        data.setPeriod(t.period());
        data.setTargetValue(t.targetValue());
        data.setWarningValue(t.warningValue());
        data.setExcellentValue(t.excellentValue());
        data.setUnit(t.unit());
        data.setMetricCode(t.metricCode());
        data.setCaliberId(t.caliberId());
        try {
            data.setApplicableRoles(t.applicableRoles() != null ? objectMapper.writeValueAsString(t.applicableRoles()) : "[]");
        } catch (JsonProcessingException e) {
            log.error("序列化适用角色失败: targetId={}", t.targetId(), e);
            data.setApplicableRoles("[]");
        }
        data.setScoringRule(t.scoringRule());
        data.setWeight(t.weight());
        data.setEnabled(t.enabled());
        data.setCreatedAt(t.createdAt() != null ? t.createdAt() : Instant.now());
        data.setUpdatedAt(t.updatedAt() != null ? t.updatedAt() : Instant.now());
        return data;
    }

    private KpiTarget toKpiTargetDomain(KpiTargetDO d) {
        List<String> applicableRoles = List.of();
        try {
            if (d.getApplicableRoles() != null) {
                applicableRoles = objectMapper.readValue(d.getApplicableRoles(), new TypeReference<List<String>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化适用角色失败: targetId={}", d.getTargetId(), e);
        }
        return new KpiTarget(d.getTargetId(), d.getTenantId(), d.getKpiCode(), d.getKpiName(),
                d.getDepartment(), d.getRole(), d.getPeriod(), d.getTargetValue(), d.getWarningValue(),
                d.getExcellentValue(), d.getUnit(), d.getMetricCode(), d.getCaliberId(),
                applicableRoles, d.getScoringRule(), d.getWeight(), d.isEnabled(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private KpiAssessmentDO toKpiAssessmentData(KpiAssessment a) {
        KpiAssessmentDO data = new KpiAssessmentDO();
        data.setAssessmentId(a.assessmentId());
        data.setTenantId(a.tenantId());
        data.setTargetId(a.targetId());
        data.setKpiCode(a.kpiCode());
        data.setKpiName(a.kpiName());
        data.setDepartment(a.department());
        data.setUserId(a.userId());
        data.setPeriod(a.period());
        data.setActualValue(a.actualValue());
        data.setTargetValue(a.targetValue());
        data.setAchievementRate(a.achievementRate());
        data.setScore(a.score());
        data.setStatus(a.status() != null ? a.status().name() : KpiStatus.NOT_STARTED.name());
        data.setAssessorId(a.assessorId());
        data.setComment(a.comment());
        data.setAssessedAt(a.assessedAt());
        data.setCreatedAt(a.createdAt() != null ? a.createdAt() : Instant.now());
        return data;
    }

    private KpiAssessment toKpiAssessmentDomain(KpiAssessmentDO d) {
        KpiStatus status = KpiStatus.NOT_STARTED;
        try {
            status = KpiStatus.valueOf(d.getStatus());
        } catch (Exception e) {
            log.warn("解析KPI状态失败: assessmentId={}, status={}", d.getAssessmentId(), d.getStatus());
        }
        return new KpiAssessment(d.getAssessmentId(), d.getTenantId(), d.getTargetId(), d.getKpiCode(),
                d.getKpiName(), d.getDepartment(), d.getUserId(), d.getPeriod(), d.getActualValue(),
                d.getTargetValue(), d.getAchievementRate(), d.getScore(), status,
                d.getAssessorId(), d.getComment(), d.getAssessedAt(), d.getCreatedAt());
    }
}
