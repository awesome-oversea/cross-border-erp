package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 部门数据对象
 * <p>
 * 描述: 对应iam_department表，存储部门信息。
 *       部门隶属于组织架构，支持树形层级结构。
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_department")
public class DepartmentDO {

    /** 部门唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String deptId;
    /** 租户ID */
    private String tenantId;
    /** 部门名称 */
    private String name;
    /** 上级部门ID，null表示顶级部门 */
    private String parentDeptId;
    /** 所属组织ID */
    private String orgId;
    /** 部门负责人用户ID */
    private String managerId;
    /** 是否启用 */
    private Boolean enabled;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public DepartmentDO() {
    }

    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getParentDeptId() { return parentDeptId; }
    public void setParentDeptId(String parentDeptId) { this.parentDeptId = parentDeptId; }
    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
