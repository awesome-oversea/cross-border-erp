package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 角色数据对象
 * <p>
 * 描述: 对应iam_role表，存储角色定义和权限集合。
 *       角色编码(code)在同一租户下唯一。
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_role")
public class RoleDO {

    /** 角色唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String roleId;
    /** 租户ID */
    private String tenantId;
    /** 角色名称 */
    private String name;
    /** 角色编码，租户内唯一 */
    private String code;
    /** 角色类型: system(系统内置)/custom(自定义) */
    private String type;
    /** 角色状态: active(启用)/inactive(停用) */
    private String status;
    /** 权限ID集合，逗号分隔 */
    private String permIds;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public RoleDO() {
    }

    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPermIds() { return permIds; }
    public void setPermIds(String permIds) { this.permIds = permIds; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
