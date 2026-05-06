package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户数据权限范围数据对象
 * <p>
 * 描述: 对应iam_data_scope表，存储用户的数据权限范围定义。
 *       支持按资源类型限定数据访问边界，实现10维数据权限控制。
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_data_scope")
public class UserDataScopeDO {

    /** 数据范围唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String scopeId;
    /** 用户ID */
    private String userId;
    /** 租户ID */
    private String tenantId;
    /** 资源类型，如 org、department、store、warehouse */
    private String resourceType;
    /** 资源ID集合，逗号分隔 */
    private String resourceIds;
    /** 范围类型: ALL/DEPT/DEPT_AND_SUB/CUSTOM */
    private String scopeType;

    public UserDataScopeDO() {
    }

    public String getScopeId() { return scopeId; }
    public void setScopeId(String scopeId) { this.scopeId = scopeId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getResourceIds() { return resourceIds; }
    public void setResourceIds(String resourceIds) { this.resourceIds = resourceIds; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
}
