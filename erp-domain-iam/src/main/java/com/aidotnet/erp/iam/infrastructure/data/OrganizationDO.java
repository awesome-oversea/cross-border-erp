package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 组织数据对象
 * <p>
 * 描述: 对应iam_organization表，存储组织架构信息。
 *       支持树形层级结构，通过path和level实现高效层级查询。
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_organization")
public class OrganizationDO {

    /** 组织唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String orgId;
    /** 租户ID */
    private String tenantId;
    /** 上级组织ID，null表示顶级组织 */
    private String parentOrgId;
    /** 组织名称 */
    private String name;
    /** 组织类型: company(公司)/division(事业部)/team(团队) */
    private String type;
    /** 层级路径，如 /org1/org2/org3/ */
    private String path;
    /** 层级深度，根组织为1 */
    private Integer level;
    /** 负责人用户ID */
    private String managerId;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public OrganizationDO() {
    }

    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getParentOrgId() { return parentOrgId; }
    public void setParentOrgId(String parentOrgId) { this.parentOrgId = parentOrgId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
