package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 用户-角色关联数据对象
 * <p>
 * 描述: 对应iam_user_role表，存储用户与角色的多对多关联关系。
 *       支持在特定组织范围内关联角色。
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_user_role")
public class UserRoleDO {

    /** 用户ID */
    private String userId;
    /** 角色ID */
    private String roleId;
    /** 组织ID，null表示全局角色 */
    private String orgId;
    /** 创建时间 */
    private Instant createdAt;

    public UserRoleDO() {
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
