package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 报表定义数据对象
 * <p>
 * 描述: 对应bi_report_definition表，用于存储报表定义的元数据信息。
 *       报表定义描述了报表的基本属性、数据源、查询逻辑和权限控制，
 *       是BI域报表执行和快照生成的基础配置。
 * </p>
 * <p>
 * 业务规则:
 *   1. 报表编码(reportCode)在租户内唯一
 *   2. 报表类型(reportType)支持: sales/inventory/finance/ads/crm等
 *   3. 数据级别(dataLevel)控制数据可见范围: tenant/department/personal
 *   4. 权限编码(permissionCode)关联IAM域权限控制
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.ReportDefinition
 */
@TableName("bi_report_definition")
public class ReportDefinitionDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String reportId;
    private String tenantId;
    private String reportCode;
    private String reportName;
    private String reportType;
    private String dataSource;
    private String queryText;
    private String permissionCode;
    private String dataLevel;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public ReportDefinitionDO() {}

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getDataLevel() { return dataLevel; }
    public void setDataLevel(String dataLevel) { this.dataLevel = dataLevel; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
