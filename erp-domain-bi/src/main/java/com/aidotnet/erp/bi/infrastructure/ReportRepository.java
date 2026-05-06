package com.aidotnet.erp.bi.infrastructure;

import com.aidotnet.erp.bi.domain.BiDashboard;
import com.aidotnet.erp.bi.domain.DashboardWidget;
import com.aidotnet.erp.bi.domain.DataVisualization;
import com.aidotnet.erp.bi.domain.Dimension;
import com.aidotnet.erp.bi.domain.KpiMetric;
import com.aidotnet.erp.bi.domain.KpiStatus;
import com.aidotnet.erp.bi.domain.MetricDefinition;
import com.aidotnet.erp.bi.domain.ReportDefinition;
import com.aidotnet.erp.bi.infrastructure.data.BiDashboardDO;
import com.aidotnet.erp.bi.infrastructure.data.DashboardWidgetDO;
import com.aidotnet.erp.bi.infrastructure.data.DataVisualizationDO;
import com.aidotnet.erp.bi.infrastructure.data.DimensionDO;
import com.aidotnet.erp.bi.infrastructure.data.KpiMetricDO;
import com.aidotnet.erp.bi.infrastructure.data.MetricDefinitionDO;
import com.aidotnet.erp.bi.infrastructure.data.ReportDefinitionDO;
import com.aidotnet.erp.bi.infrastructure.mapper.ReportMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 报表与商业智能数据仓储层
 * <p>
 * 描述: BI域核心数据持久化层，负责报表定义、KPI指标、指标口径定义、
 *       仪表盘、组件、可视化、维度等实体的CRUD操作。所有数据通过
 *       MyBatis持久化到数据库，支持多租户数据隔离。
 * </p>
 * <p>
 * 核心职责:
 *   1. 报表定义 - 创建/更新/查询/删除报表定义，支持按编码和类型查询
 *   2. KPI指标 - 记录KPI指标值，更新目标值，自动计算达成状态
 *   3. 指标口径 - 管理指标口径定义，确保指标计算标准一致性
 *   4. 仪表盘 - 创建/更新/删除BI仪表盘，管理布局和配置
 *   5. 组件管理 - 创建/更新/删除仪表盘组件，支持排序和启禁用
 *   6. 可视化 - 创建/更新/删除数据可视化配置，关联报表
 *   7. 维度管理 - 创建/更新/删除分析维度，支持按编码和类型查询
 * </p>
 * <p>
 * 设计说明:
 *   1. 采用DO(数据对象)与领域对象分离模式，通过转换方法实现层间数据映射
 *   2. JSONB字段(如config)通过Jackson序列化/反序列化处理
 *   3. 所有操作均带租户隔离(tenantId)，确保多租户数据安全
 *   4. save方法采用upsert模式(存在则更新，不存在则插入)
 *   5. KPI状态自动计算: ON_TRACK(≥90%) / AT_RISK(≥70%) / OFF_TRACK(<70%)
 * </p>
 *
 * @author ERP系统
 * @see ReportMapper
 * @see ReportDefinition
 * @see KpiMetric
 */
@Repository
public class ReportRepository {

    private final ReportMapper mapper;
    private final ObjectMapper objectMapper;

    public ReportRepository(ReportMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /* ================================ 报表定义 ================================ */

    /**
     * 保存报表定义(存在则更新，不存在则插入)
     *
     * @param report 报表定义领域对象
     * @return 保存后的报表定义
     */
    public ReportDefinition save(ReportDefinition report) {
        ReportDefinitionDO existing = mapper.selectReport(report.tenantId(), report.reportId());
        ReportDefinitionDO data = toReportData(report);
        if (existing == null) {
            mapper.insertReport(data);
        } else {
            mapper.updateReport(data);
        }
        return report;
    }

    /**
     * 根据报表ID查询报表定义
     *
     * @param tenantId 租户ID
     * @param reportId 报表ID
     * @return 报表定义(可能为空)
     */
    public Optional<ReportDefinition> find(String tenantId, String reportId) {
        return Optional.ofNullable(mapper.selectReport(tenantId, reportId)).map(this::toReportDomain);
    }

    /**
     * 根据报表编码查询报表定义
     *
     * @param tenantId   租户ID
     * @param reportCode 报表编码(租户内唯一)
     * @return 报表定义(可能为空)
     */
    public Optional<ReportDefinition> findByCode(String tenantId, String reportCode) {
        return Optional.ofNullable(mapper.selectReportByCode(tenantId, reportCode)).map(this::toReportDomain);
    }

    /**
     * 查询租户下所有报表定义
     *
     * @param tenantId 租户ID
     * @return 报表定义列表
     */
    public List<ReportDefinition> list(String tenantId) {
        return mapper.selectReports(tenantId).stream().map(this::toReportDomain).toList();
    }

    /**
     * 按报表类型查询报表定义
     *
     * @param tenantId   租户ID
     * @param reportType 报表类型(如sales/inventory/finance等)
     * @return 报表定义列表
     */
    public List<ReportDefinition> listByType(String tenantId, String reportType) {
        return mapper.selectReportsByType(tenantId, reportType).stream().map(this::toReportDomain).toList();
    }

    /**
     * 删除报表定义
     *
     * @param tenantId 租户ID
     * @param reportId 报表ID
     */
    public void deleteReport(String tenantId, String reportId) {
        mapper.deleteReport(tenantId, reportId);
    }

    /* ================================ KPI指标 ================================ */

    /**
     * 保存KPI指标记录(仅插入，每次记录生成新的KPI数据点)
     *
     * @param kpi KPI指标领域对象
     * @return 保存后的KPI指标
     */
    public KpiMetric saveKpi(KpiMetric kpi) {
        mapper.insertKpi(toKpiData(kpi));
        return kpi;
    }

    /**
     * 更新KPI目标值并重新计算达成状态
     *
     * @param kpi 包含新目标值的KPI指标对象
     * @return 更新后的KPI指标
     */
    public KpiMetric updateKpiTarget(KpiMetric kpi) {
        mapper.updateKpiTarget(toKpiData(kpi));
        return kpi;
    }

    /**
     * 根据KPI ID查询指标
     *
     * @param tenantId 租户ID
     * @param kpiId    KPI指标ID
     * @return KPI指标(可能为空)
     */
    public Optional<KpiMetric> findKpi(String tenantId, String kpiId) {
        return Optional.ofNullable(mapper.selectKpi(tenantId, kpiId)).map(this::toKpiDomain);
    }

    /**
     * 根据KPI编码查询最新一条指标记录
     *
     * @param tenantId 租户ID
     * @param kpiCode  KPI指标编码
     * @return 最新的KPI指标(可能为空)
     */
    public Optional<KpiMetric> findLatestKpiByCode(String tenantId, String kpiCode) {
        return Optional.ofNullable(mapper.selectLatestKpiByCode(tenantId, kpiCode)).map(this::toKpiDomain);
    }

    /**
     * 查询租户下所有KPI指标
     *
     * @param tenantId 租户ID
     * @return KPI指标列表(按测量时间倒序、编码升序排列)
     */
    public List<KpiMetric> listKpis(String tenantId) {
        return mapper.selectKpis(tenantId).stream().map(this::toKpiDomain).toList();
    }

    /**
     * 按分类查询KPI指标
     *
     * @param tenantId 租户ID
     * @param category KPI分类(如sales/profit/inventory等)
     * @return KPI指标列表
     */
    public List<KpiMetric> listKpisByCategory(String tenantId, String category) {
        return mapper.selectKpisByCategory(tenantId, category).stream().map(this::toKpiDomain).toList();
    }

    /**
     * 删除KPI指标记录
     *
     * @param tenantId 租户ID
     * @param kpiId    KPI指标ID
     */
    public void deleteKpi(String tenantId, String kpiId) {
        mapper.deleteKpi(tenantId, kpiId);
    }

    /* ================================ 指标口径定义 ================================ */

    /**
     * 保存指标口径定义(存在则更新，不存在则插入)
     *
     * @param metric 指标口径定义领域对象
     * @return 保存后的指标口径定义
     */
    public MetricDefinition saveMetric(MetricDefinition metric) {
        MetricDefinitionDO existing = mapper.selectMetric(metric.tenantId(), metric.metricId());
        MetricDefinitionDO data = toMetricData(metric);
        if (existing == null) {
            mapper.insertMetric(data);
        } else {
            mapper.updateMetric(data);
        }
        return metric;
    }

    /**
     * 根据指标ID查询指标口径定义
     *
     * @param tenantId 租户ID
     * @param metricId 指标ID
     * @return 指标口径定义(可能为空)
     */
    public Optional<MetricDefinition> findMetric(String tenantId, String metricId) {
        return Optional.ofNullable(mapper.selectMetric(tenantId, metricId)).map(this::toMetricDomain);
    }

    /**
     * 根据指标编码查询指标口径定义
     *
     * @param tenantId   租户ID
     * @param metricCode 指标编码(租户内唯一)
     * @return 指标口径定义(可能为空)
     */
    public Optional<MetricDefinition> findMetricByCode(String tenantId, String metricCode) {
        return Optional.ofNullable(mapper.selectMetricByCode(tenantId, metricCode)).map(this::toMetricDomain);
    }

    /**
     * 按条件查询指标口径定义
     *
     * @param tenantId 租户ID
     * @param category 分类(可选)
     * @param enabled  是否启用(可选)
     * @return 指标口径定义列表
     */
    public List<MetricDefinition> listMetrics(String tenantId, String category, Boolean enabled) {
        return mapper.selectMetrics(tenantId, category, enabled).stream().map(this::toMetricDomain).toList();
    }

    /**
     * 删除指标口径定义
     *
     * @param tenantId 租户ID
     * @param metricId 指标ID
     */
    public void deleteMetric(String tenantId, String metricId) {
        mapper.deleteMetric(tenantId, metricId);
    }

    /* ================================ 仪表盘组件 ================================ */

    /**
     * 保存仪表盘组件(存在则更新，不存在则插入)
     *
     * @param widget 仪表盘组件领域对象
     * @return 保存后的仪表盘组件
     */
    public DashboardWidget saveWidget(DashboardWidget widget) {
        DashboardWidgetDO existing = mapper.selectWidget(widget.tenantId(), widget.widgetId());
        DashboardWidgetDO data = toWidgetData(widget);
        if (existing == null) {
            mapper.insertWidget(data);
        } else {
            mapper.updateWidget(data);
        }
        return widget;
    }

    /**
     * 根据组件ID查询仪表盘组件
     *
     * @param tenantId 租户ID
     * @param widgetId 组件ID
     * @return 仪表盘组件(可能为空)
     */
    public Optional<DashboardWidget> findWidget(String tenantId, String widgetId) {
        return Optional.ofNullable(mapper.selectWidget(tenantId, widgetId)).map(this::toWidgetDomain);
    }

    /**
     * 查询租户下所有已启用的仪表盘组件(按排序号升序)
     *
     * @param tenantId 租户ID
     * @return 已启用的仪表盘组件列表
     */
    public List<DashboardWidget> listWidgets(String tenantId) {
        return mapper.selectWidgets(tenantId).stream()
                .filter(d -> Boolean.TRUE.equals(d.getEnabled()))
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(this::toWidgetDomain)
                .toList();
    }

    /**
     * 删除仪表盘组件
     *
     * @param tenantId 租户ID
     * @param widgetId 组件ID
     */
    public void deleteWidget(String tenantId, String widgetId) {
        mapper.deleteWidget(tenantId, widgetId);
    }

    /* ================================ 数据可视化 ================================ */

    /**
     * 保存数据可视化配置(存在则更新，不存在则插入)
     *
     * @param viz 数据可视化领域对象
     * @return 保存后的数据可视化
     */
    public DataVisualization saveVisualization(DataVisualization viz) {
        DataVisualizationDO existing = mapper.selectVisualization(viz.tenantId(), viz.vizId());
        DataVisualizationDO data = toVisualizationData(viz);
        if (existing == null) {
            mapper.insertVisualization(data);
        } else {
            mapper.updateVisualization(data);
        }
        return viz;
    }

    /**
     * 根据可视化ID查询数据可视化
     *
     * @param tenantId 租户ID
     * @param vizId    可视化ID
     * @return 数据可视化(可能为空)
     */
    public Optional<DataVisualization> findVisualization(String tenantId, String vizId) {
        return Optional.ofNullable(mapper.selectVisualization(tenantId, vizId)).map(this::toVisualizationDomain);
    }

    /**
     * 按报表ID查询数据可视化列表
     *
     * @param tenantId 租户ID
     * @param reportId 报表ID(可选，为null时查询所有)
     * @return 数据可视化列表
     */
    public List<DataVisualization> listVisualizations(String tenantId, String reportId) {
        return mapper.selectVisualizations(tenantId, reportId).stream().map(this::toVisualizationDomain).toList();
    }

    /**
     * 删除数据可视化
     *
     * @param tenantId 租户ID
     * @param vizId    可视化ID
     */
    public void deleteVisualization(String tenantId, String vizId) {
        mapper.deleteVisualization(tenantId, vizId);
    }

    /* ================================ 维度管理 ================================ */

    /**
     * 保存维度(存在则更新，不存在则插入)
     *
     * @param dimension 维度领域对象
     * @return 保存后的维度
     */
    public Dimension saveDimension(Dimension dimension) {
        DimensionDO existing = mapper.selectDimension(dimension.tenantId(), dimension.dimensionId());
        DimensionDO data = toDimensionData(dimension);
        if (existing == null) {
            mapper.insertDimension(data);
        } else {
            mapper.updateDimension(data);
        }
        return dimension;
    }

    /**
     * 根据维度ID查询维度
     *
     * @param tenantId    租户ID
     * @param dimensionId 维度ID
     * @return 维度(可能为空)
     */
    public Optional<Dimension> findDimension(String tenantId, String dimensionId) {
        return Optional.ofNullable(mapper.selectDimension(tenantId, dimensionId)).map(this::toDimensionDomain);
    }

    /**
     * 根据维度编码查询维度
     *
     * @param tenantId      租户ID
     * @param dimensionCode 维度编码(租户内唯一)
     * @return 维度(可能为空)
     */
    public Optional<Dimension> findDimensionByCode(String tenantId, String dimensionCode) {
        return Optional.ofNullable(mapper.selectDimensionByCode(tenantId, dimensionCode)).map(this::toDimensionDomain);
    }

    /**
     * 按启用状态查询维度列表
     *
     * @param tenantId 租户ID
     * @param enabled  是否启用(可选，为null时查询所有)
     * @return 维度列表
     */
    public List<Dimension> listDimensions(String tenantId, Boolean enabled) {
        return mapper.selectDimensions(tenantId, enabled).stream().map(this::toDimensionDomain).toList();
    }

    /**
     * 删除维度
     *
     * @param tenantId    租户ID
     * @param dimensionId 维度ID
     */
    public void deleteDimension(String tenantId, String dimensionId) {
        mapper.deleteDimension(tenantId, dimensionId);
    }

    /* ================================ BI仪表盘 ================================ */

    /**
     * 保存BI仪表盘(存在则更新，不存在则插入)
     *
     * @param dashboard BI仪表盘领域对象
     * @return 保存后的BI仪表盘
     */
    public BiDashboard saveDashboard(BiDashboard dashboard) {
        BiDashboardDO existing = mapper.selectDashboard(dashboard.tenantId(), dashboard.dashboardId());
        BiDashboardDO data = toDashboardData(dashboard);
        if (existing == null) {
            mapper.insertDashboard(data);
        } else {
            mapper.updateDashboard(data);
        }
        return dashboard;
    }

    /**
     * 根据仪表盘ID查询BI仪表盘
     *
     * @param tenantId    租户ID
     * @param dashboardId 仪表盘ID
     * @return BI仪表盘(可能为空)
     */
    public Optional<BiDashboard> findDashboard(String tenantId, String dashboardId) {
        return Optional.ofNullable(mapper.selectDashboard(tenantId, dashboardId)).map(this::toDashboardDomain);
    }

    /**
     * 查询租户下所有BI仪表盘
     *
     * @param tenantId 租户ID
     * @return BI仪表盘列表
     */
    public List<BiDashboard> listDashboards(String tenantId) {
        return mapper.selectDashboards(tenantId).stream().map(this::toDashboardDomain).toList();
    }

    /**
     * 删除BI仪表盘
     *
     * @param tenantId    租户ID
     * @param dashboardId 仪表盘ID
     */
    public void deleteDashboard(String tenantId, String dashboardId) {
        mapper.deleteDashboard(tenantId, dashboardId);
    }

    /* ================================ 私有方法 ================================ */

    /**
     * 计算KPI达成状态
     * <p>
     * 规则:
     *   - 目标值为空或0 → NOT_STARTED
     *   - 达成率 ≥ 90% → ON_TRACK
     *   - 达成率 ≥ 70% → AT_RISK
     *   - 达成率 < 70% → OFF_TRACK
     * </p>
     *
     * @param value       当前值
     * @param targetValue 目标值
     * @return KPI达成状态
     */
    private KpiStatus computeKpiStatus(BigDecimal value, BigDecimal targetValue) {
        if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) == 0) {
            return KpiStatus.NOT_STARTED;
        }
        BigDecimal ratio = value.divide(targetValue, 4, RoundingMode.HALF_UP);
        if (ratio.compareTo(BigDecimal.valueOf(0.9)) >= 0) {
            return KpiStatus.ON_TRACK;
        } else if (ratio.compareTo(BigDecimal.valueOf(0.7)) >= 0) {
            return KpiStatus.AT_RISK;
        } else {
            return KpiStatus.OFF_TRACK;
        }
    }

    private ReportDefinitionDO toReportData(ReportDefinition report) {
        ReportDefinitionDO data = new ReportDefinitionDO();
        data.setReportId(report.reportId());
        data.setTenantId(report.tenantId());
        data.setReportCode(report.reportCode());
        data.setReportName(report.reportName());
        data.setReportType(report.reportType());
        data.setDataSource(report.dataSource());
        data.setQueryText(report.queryText());
        data.setPermissionCode(report.permissionCode());
        data.setDataLevel(report.dataLevel());
        data.setDescription(report.description());
        data.setCreatedAt(report.createdAt());
        data.setUpdatedAt(report.updatedAt());
        return data;
    }

    private ReportDefinition toReportDomain(ReportDefinitionDO data) {
        return new ReportDefinition(
                data.getReportId(),
                data.getTenantId(),
                data.getReportCode(),
                data.getReportName(),
                data.getReportType(),
                data.getDataSource(),
                data.getQueryText(),
                data.getPermissionCode(),
                data.getDataLevel(),
                data.getDescription(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private KpiMetricDO toKpiData(KpiMetric kpi) {
        KpiMetricDO data = new KpiMetricDO();
        data.setKpiId(kpi.kpiId());
        data.setTenantId(kpi.tenantId());
        data.setKpiCode(kpi.kpiCode());
        data.setKpiName(kpi.kpiName());
        data.setCategory(kpi.category());
        data.setValue(kpi.value());
        data.setTargetValue(kpi.targetValue());
        data.setUnit(kpi.unit());
        data.setStatus(kpi.status() != null ? kpi.status().name() : computeKpiStatus(kpi.value(), kpi.targetValue()).name());
        data.setMeasuredAt(kpi.measuredAt());
        return data;
    }

    private KpiMetric toKpiDomain(KpiMetricDO data) {
        KpiStatus status = null;
        try {
            status = KpiStatus.valueOf(data.getStatus());
        } catch (Exception ignored) {
            status = computeKpiStatus(data.getValue(), data.getTargetValue());
        }
        return new KpiMetric(
                data.getKpiId(),
                data.getTenantId(),
                data.getKpiCode(),
                data.getKpiName(),
                data.getCategory(),
                data.getValue(),
                data.getTargetValue(),
                data.getUnit(),
                status,
                data.getMeasuredAt());
    }

    private MetricDefinitionDO toMetricData(MetricDefinition metric) {
        MetricDefinitionDO data = new MetricDefinitionDO();
        data.setMetricId(metric.metricId());
        data.setTenantId(metric.tenantId());
        data.setMetricCode(metric.metricCode());
        data.setMetricName(metric.metricName());
        data.setCategory(metric.category());
        data.setFormula(metric.formula());
        data.setUnit(metric.unit());
        data.setPermissionCode(metric.permissionCode());
        data.setDataLevel(metric.dataLevel());
        data.setDescription(metric.description());
        data.setEnabled(metric.enabled());
        data.setCreatedAt(metric.createdAt());
        data.setUpdatedAt(metric.updatedAt());
        return data;
    }

    private MetricDefinition toMetricDomain(MetricDefinitionDO data) {
        return new MetricDefinition(
                data.getMetricId(),
                data.getTenantId(),
                data.getMetricCode(),
                data.getMetricName(),
                data.getCategory(),
                data.getFormula(),
                data.getUnit(),
                data.getPermissionCode(),
                data.getDataLevel(),
                data.getDescription(),
                Boolean.TRUE.equals(data.getEnabled()),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private DashboardWidgetDO toWidgetData(DashboardWidget widget) {
        DashboardWidgetDO data = new DashboardWidgetDO();
        data.setWidgetId(widget.widgetId());
        data.setTenantId(widget.tenantId());
        data.setWidgetCode(widget.widgetCode());
        data.setWidgetName(widget.widgetName());
        data.setWidgetType(widget.widgetType());
        data.setSortOrder(widget.sortOrder());
        data.setEnabled(widget.enabled());
        data.setCreatedAt(widget.createdAt());
        data.setUpdatedAt(widget.updatedAt());
        try {
            data.setConfig(objectMapper.writeValueAsString(widget.config()));
        } catch (JsonProcessingException e) {
            data.setConfig("{}");
        }
        return data;
    }

    private DashboardWidget toWidgetDomain(DashboardWidgetDO data) {
        Map<String, Object> config = Collections.emptyMap();
        try {
            config = objectMapper.readValue(data.getConfig(), new TypeReference<>() {});
        } catch (JsonProcessingException ignored) {
        }
        return new DashboardWidget(
                data.getWidgetId(),
                data.getTenantId(),
                data.getWidgetCode(),
                data.getWidgetName(),
                data.getWidgetType(),
                config,
                data.getSortOrder(),
                Boolean.TRUE.equals(data.getEnabled()),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private DataVisualizationDO toVisualizationData(DataVisualization viz) {
        DataVisualizationDO data = new DataVisualizationDO();
        data.setVizId(viz.vizId());
        data.setTenantId(viz.tenantId());
        data.setReportId(viz.reportId());
        data.setVizName(viz.vizName());
        data.setVizType(viz.vizType());
        data.setSortOrder(viz.sortOrder());
        data.setEnabled(viz.enabled());
        data.setCreatedAt(viz.createdAt());
        data.setUpdatedAt(viz.updatedAt());
        try {
            data.setConfig(objectMapper.writeValueAsString(viz.config()));
        } catch (JsonProcessingException e) {
            data.setConfig("{}");
        }
        return data;
    }

    private DataVisualization toVisualizationDomain(DataVisualizationDO data) {
        Map<String, Object> config = Collections.emptyMap();
        try {
            config = objectMapper.readValue(data.getConfig(), new TypeReference<>() {});
        } catch (JsonProcessingException ignored) {
        }
        return new DataVisualization(
                data.getVizId(),
                data.getTenantId(),
                data.getReportId(),
                data.getVizName(),
                data.getVizType(),
                config,
                data.getSortOrder(),
                Boolean.TRUE.equals(data.getEnabled()),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private DimensionDO toDimensionData(Dimension dimension) {
        DimensionDO data = new DimensionDO();
        data.setDimensionId(dimension.dimensionId());
        data.setTenantId(dimension.tenantId());
        data.setDimensionCode(dimension.dimensionCode());
        data.setDimensionName(dimension.dimensionName());
        data.setDimensionType(dimension.dimensionType());
        data.setSourceField(dimension.sourceField());
        data.setDescription(dimension.description());
        data.setEnabled(dimension.enabled());
        data.setCreatedAt(dimension.createdAt());
        data.setUpdatedAt(dimension.updatedAt());
        return data;
    }

    private Dimension toDimensionDomain(DimensionDO data) {
        return new Dimension(
                data.getDimensionId(),
                data.getTenantId(),
                data.getDimensionCode(),
                data.getDimensionName(),
                data.getDimensionType(),
                data.getSourceField(),
                data.getDescription(),
                Boolean.TRUE.equals(data.getEnabled()),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private BiDashboardDO toDashboardData(BiDashboard dashboard) {
        BiDashboardDO data = new BiDashboardDO();
        data.setDashboardId(dashboard.dashboardId());
        data.setTenantId(dashboard.tenantId());
        data.setDashboardName(dashboard.dashboardName());
        data.setDashboardType(dashboard.dashboardType());
        data.setOwner(dashboard.owner());
        data.setEnabled(dashboard.enabled());
        data.setCreatedAt(dashboard.createdAt());
        data.setUpdatedAt(dashboard.updatedAt());
        try {
            data.setConfig(objectMapper.writeValueAsString(dashboard.config()));
        } catch (JsonProcessingException e) {
            data.setConfig("{}");
        }
        return data;
    }

    private BiDashboard toDashboardDomain(BiDashboardDO data) {
        Map<String, Object> config = Collections.emptyMap();
        try {
            config = objectMapper.readValue(data.getConfig(), new TypeReference<>() {});
        } catch (JsonProcessingException ignored) {
        }
        return new BiDashboard(
                data.getDashboardId(),
                data.getTenantId(),
                data.getDashboardName(),
                data.getDashboardType(),
                config,
                data.getOwner(),
                Boolean.TRUE.equals(data.getEnabled()),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }
}
