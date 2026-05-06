package com.aidotnet.erp.bi.infrastructure.mapper;

import com.aidotnet.erp.bi.infrastructure.data.BiDashboardDO;
import com.aidotnet.erp.bi.infrastructure.data.DashboardWidgetDO;
import com.aidotnet.erp.bi.infrastructure.data.DataVisualizationDO;
import com.aidotnet.erp.bi.infrastructure.data.DimensionDO;
import com.aidotnet.erp.bi.infrastructure.data.KpiMetricDO;
import com.aidotnet.erp.bi.infrastructure.data.MetricDefinitionDO;
import com.aidotnet.erp.bi.infrastructure.data.ReportDefinitionDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 报表与商业智能数据访问映射接口
 * <p>
 * 描述: 提供BI域核心功能的数据访问层，包括报表定义、KPI指标、
 *       指标口径定义、仪表盘组件、数据可视化、维度和BI仪表盘等
 *       实体的CRUD操作。
 * </p>
 * <p>
 * 数据库表映射:
 *   1. bi_report_definition - 报表定义
 *   2. bi_kpi_metric - KPI指标
 *   3. bi_metric_definition - 指标口径定义
 *   4. bi_dashboard_widget - 仪表盘组件
 *   5. bi_data_visualization - 数据可视化
 *   6. bi_dimension - 分析维度
 *   7. bi_dashboard - BI仪表盘
 * </p>
 * <p>
 * 设计约定:
 *   1. 所有查询操作必须带tenantId参数，确保多租户数据隔离
 *   2. 删除操作采用物理删除，基于tenantId+业务ID联合条件
 *   3. 列表查询默认按创建时间倒序排列
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface ReportMapper {

    /* ================================ 报表定义 ================================ */

    void insertReport(ReportDefinitionDO report);
    void updateReport(ReportDefinitionDO report);
    ReportDefinitionDO selectReport(@Param("tenantId") String tenantId, @Param("reportId") String reportId);
    ReportDefinitionDO selectReportByCode(@Param("tenantId") String tenantId, @Param("reportCode") String reportCode);
    List<ReportDefinitionDO> selectReports(@Param("tenantId") String tenantId);
    List<ReportDefinitionDO> selectReportsByType(@Param("tenantId") String tenantId, @Param("reportType") String reportType);
    void deleteReport(@Param("tenantId") String tenantId, @Param("reportId") String reportId);

    /* ================================ KPI指标 ================================ */

    void insertKpi(KpiMetricDO kpi);
    KpiMetricDO selectKpi(@Param("tenantId") String tenantId, @Param("kpiId") String kpiId);
    List<KpiMetricDO> selectKpis(@Param("tenantId") String tenantId);
    List<KpiMetricDO> selectKpisByCategory(@Param("tenantId") String tenantId, @Param("category") String category);
    KpiMetricDO selectLatestKpiByCode(@Param("tenantId") String tenantId, @Param("kpiCode") String kpiCode);
    void updateKpiTarget(KpiMetricDO kpi);
    void deleteKpi(@Param("tenantId") String tenantId, @Param("kpiId") String kpiId);

    /* ================================ 指标口径定义 ================================ */

    void insertMetric(MetricDefinitionDO metric);
    void updateMetric(MetricDefinitionDO metric);
    MetricDefinitionDO selectMetric(@Param("tenantId") String tenantId, @Param("metricId") String metricId);
    MetricDefinitionDO selectMetricByCode(@Param("tenantId") String tenantId, @Param("metricCode") String metricCode);
    List<MetricDefinitionDO> selectMetrics(@Param("tenantId") String tenantId, @Param("category") String category,
                                           @Param("enabled") Boolean enabled);
    void deleteMetric(@Param("tenantId") String tenantId, @Param("metricId") String metricId);

    /* ================================ 仪表盘组件 ================================ */

    void insertWidget(DashboardWidgetDO widget);
    void updateWidget(DashboardWidgetDO widget);
    DashboardWidgetDO selectWidget(@Param("tenantId") String tenantId, @Param("widgetId") String widgetId);
    List<DashboardWidgetDO> selectWidgets(@Param("tenantId") String tenantId);
    void deleteWidget(@Param("tenantId") String tenantId, @Param("widgetId") String widgetId);

    /* ================================ 数据可视化 ================================ */

    void insertVisualization(DataVisualizationDO viz);
    void updateVisualization(DataVisualizationDO viz);
    DataVisualizationDO selectVisualization(@Param("tenantId") String tenantId, @Param("vizId") String vizId);
    List<DataVisualizationDO> selectVisualizations(@Param("tenantId") String tenantId, @Param("reportId") String reportId);
    void deleteVisualization(@Param("tenantId") String tenantId, @Param("vizId") String vizId);

    /* ================================ 维度管理 ================================ */

    void insertDimension(DimensionDO dimension);
    void updateDimension(DimensionDO dimension);
    DimensionDO selectDimension(@Param("tenantId") String tenantId, @Param("dimensionId") String dimensionId);
    DimensionDO selectDimensionByCode(@Param("tenantId") String tenantId, @Param("dimensionCode") String dimensionCode);
    List<DimensionDO> selectDimensions(@Param("tenantId") String tenantId, @Param("enabled") Boolean enabled);
    void deleteDimension(@Param("tenantId") String tenantId, @Param("dimensionId") String dimensionId);

    /* ================================ BI仪表盘 ================================ */

    void insertDashboard(BiDashboardDO dashboard);
    void updateDashboard(BiDashboardDO dashboard);
    BiDashboardDO selectDashboard(@Param("tenantId") String tenantId, @Param("dashboardId") String dashboardId);
    List<BiDashboardDO> selectDashboards(@Param("tenantId") String tenantId);
    void deleteDashboard(@Param("tenantId") String tenantId, @Param("dashboardId") String dashboardId);
}
