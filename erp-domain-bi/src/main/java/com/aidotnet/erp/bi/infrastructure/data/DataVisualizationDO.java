package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 数据可视化数据对象
 * <p>
 * 描述: 对应bi_data_visualization表，用于存储数据可视化的配置数据。
 *       可视化是报表数据的展示形式，包含折线图、柱状图、饼图等类型，
 *       关联报表定义，通过排序号控制展示顺序。
 * </p>
 * <p>
 * 业务规则:
 *   1. 可视化必须关联一个报表定义(reportId)
 *   2. 可视化类型(vizType)支持: line/bar/pie/scatter/heatmap/table等
 *   3. 配置(config)以JSONB格式存储，包含字段映射、样式和交互配置
 *   4. 排序号(sortOrder)控制可视化在报表中的展示顺序(升序)
 *   5. 禁用的可视化不参与报表渲染
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.DataVisualization
 */
@TableName("bi_data_visualization")
public class DataVisualizationDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String vizId;
    private String tenantId;
    private String reportId;
    private String vizName;
    private String vizType;
    private String config;
    private Integer sortOrder;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public DataVisualizationDO() {}

    public String getVizId() { return vizId; }
    public void setVizId(String vizId) { this.vizId = vizId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getVizName() { return vizName; }
    public void setVizName(String vizName) { this.vizName = vizName; }
    public String getVizType() { return vizType; }
    public void setVizType(String vizType) { this.vizType = vizType; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
