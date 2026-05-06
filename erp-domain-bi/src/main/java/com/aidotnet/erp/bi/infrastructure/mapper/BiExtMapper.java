package com.aidotnet.erp.bi.infrastructure.mapper;

import com.aidotnet.erp.bi.domain.DataExportTask;
import com.aidotnet.erp.bi.domain.Dimension;
import com.aidotnet.erp.bi.domain.ReportSnapshot;
import com.aidotnet.erp.bi.infrastructure.data.AlertRuleDO;
import com.aidotnet.erp.bi.infrastructure.data.CockpitTrendDO;
import com.aidotnet.erp.bi.infrastructure.data.KpiAssessmentDO;
import com.aidotnet.erp.bi.infrastructure.data.KpiTargetDO;
import com.aidotnet.erp.bi.infrastructure.data.KpiTemplateDO;
import com.aidotnet.erp.bi.infrastructure.data.MetricCaliberDO;
import com.aidotnet.erp.bi.infrastructure.data.MetricCaliberValueDO;
import com.aidotnet.erp.bi.infrastructure.data.RankingDataDO;
import com.aidotnet.erp.bi.infrastructure.data.ReportSnapshotDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * BI扩展数据访问映射接口
 * <p>
 * 描述: 提供BI域扩展功能的数据访问层，包括告警规则、报表快照、
 *       数据导出任务、维度查询、KPI模板、驾驶舱趋势、排名数据、
 *       指标口径、口径计算值、KPI目标和KPI考核等实体的CRUD操作。
 * </p>
 * <p>
 * 数据库表映射:
 *   1. bi_alert_rule - 告警规则
 *   2. bi_report_snapshot - 报表快照
 *   3. bi_data_export_task - 数据导出任务
 *   4. bi_kpi_template - KPI模板
 *   5. bi_cockpit_trend - 驾驶舱趋势
 *   6. bi_ranking_data - 排名数据
 *   7. bi_metric_caliber - 指标口径
 *   8. bi_metric_caliber_value - 口径计算值
 *   9. bi_kpi_target - KPI目标
 *   10. bi_kpi_assessment - KPI考核
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface BiExtMapper {

    void insertAlertRule(AlertRuleDO rule);
    void updateAlertRule(AlertRuleDO rule);
    AlertRuleDO selectAlertRule(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);
    List<AlertRuleDO> selectAlertRules(@Param("tenantId") String tenantId, @Param("domain") String domain);
    List<AlertRuleDO> selectEnabledAlertRules(@Param("tenantId") String tenantId);
    void deleteAlertRule(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);

    void insertReportSnapshot(ReportSnapshotDO snapshot);
    ReportSnapshotDO selectReportSnapshot(@Param("tenantId") String tenantId, @Param("snapshotId") String snapshotId);
    List<ReportSnapshotDO> selectReportSnapshots(@Param("tenantId") String tenantId, @Param("reportId") String reportId);
    void deleteReportSnapshot(@Param("tenantId") String tenantId, @Param("snapshotId") String snapshotId);

    void insertDataExportTask(DataExportTask task);
    DataExportTask selectDataExportTask(@Param("tenantId") String tenantId, @Param("taskId") String taskId);
    List<DataExportTask> selectDataExportTasks(@Param("tenantId") String tenantId, @Param("status") String status);
    void updateDataExportTask(DataExportTask task);

    List<Dimension> selectDimensions(@Param("tenantId") String tenantId, @Param("enabled") Boolean enabled);

    List<ReportSnapshot> selectReportSnapshotDomains(@Param("tenantId") String tenantId, @Param("reportId") String reportId);

    void insertKpiTemplate(KpiTemplateDO template);
    void updateKpiTemplate(KpiTemplateDO template);
    KpiTemplateDO selectKpiTemplate(@Param("tenantId") String tenantId, @Param("templateId") String templateId);
    KpiTemplateDO selectKpiTemplateByCode(@Param("tenantId") String tenantId, @Param("templateCode") String templateCode);
    List<KpiTemplateDO> selectKpiTemplates(@Param("tenantId") String tenantId);
    List<KpiTemplateDO> selectKpiTemplatesByCategory(@Param("tenantId") String tenantId, @Param("category") String category);
    void deleteKpiTemplate(@Param("tenantId") String tenantId, @Param("templateId") String templateId);

    void insertCockpitTrend(CockpitTrendDO trend);
    CockpitTrendDO selectCockpitTrend(@Param("tenantId") String tenantId, @Param("trendId") String trendId);
    List<CockpitTrendDO> selectCockpitTrends(@Param("tenantId") String tenantId, @Param("metricCode") String metricCode);
    void deleteCockpitTrend(@Param("tenantId") String tenantId, @Param("trendId") String trendId);

    void insertRankingData(RankingDataDO ranking);
    RankingDataDO selectRankingData(@Param("tenantId") String tenantId, @Param("rankingId") String rankingId);
    List<RankingDataDO> selectRankingDataList(@Param("tenantId") String tenantId, @Param("rankingType") String rankingType);
    void deleteRankingData(@Param("tenantId") String tenantId, @Param("rankingId") String rankingId);

    void insertMetricCaliber(MetricCaliberDO caliber);
    void updateMetricCaliber(MetricCaliberDO caliber);
    MetricCaliberDO selectMetricCaliber(@Param("tenantId") String tenantId, @Param("caliberId") String caliberId);
    MetricCaliberDO selectMetricCaliberByCode(@Param("tenantId") String tenantId, @Param("metricCode") String metricCode);
    List<MetricCaliberDO> selectMetricCalibers(@Param("tenantId") String tenantId, @Param("category") String category);
    void deleteMetricCaliber(@Param("tenantId") String tenantId, @Param("caliberId") String caliberId);

    void insertMetricCaliberValue(MetricCaliberValueDO value);
    List<MetricCaliberValueDO> selectMetricCaliberValues(@Param("tenantId") String tenantId, @Param("metricCode") String metricCode);
    void deleteMetricCaliberValue(@Param("tenantId") String tenantId, @Param("valueId") String valueId);

    void insertKpiTarget(KpiTargetDO target);
    void updateKpiTarget(KpiTargetDO target);
    KpiTargetDO selectKpiTarget(@Param("tenantId") String tenantId, @Param("targetId") String targetId);
    List<KpiTargetDO> selectKpiTargets(@Param("tenantId") String tenantId, @Param("department") String department, @Param("period") String period);
    void deleteKpiTarget(@Param("tenantId") String tenantId, @Param("targetId") String targetId);

    void insertKpiAssessment(KpiAssessmentDO assessment);
    KpiAssessmentDO selectKpiAssessment(@Param("tenantId") String tenantId, @Param("assessmentId") String assessmentId);
    List<KpiAssessmentDO> selectKpiAssessments(@Param("tenantId") String tenantId, @Param("userId") String userId, @Param("period") String period);
    List<KpiAssessmentDO> selectKpiAssessmentsByTarget(@Param("tenantId") String tenantId, @Param("targetId") String targetId);
    void deleteKpiAssessment(@Param("tenantId") String tenantId, @Param("assessmentId") String assessmentId);
}
