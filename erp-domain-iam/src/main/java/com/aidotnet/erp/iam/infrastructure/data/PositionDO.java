package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 岗位数据对象
 * <p>
 * 描述: 对应iam_position表，存储岗位信息。
 *       岗位定义组织内的职能角色，支持层级结构。
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_position")
public class PositionDO {

    /** 岗位唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String positionId;
    /** 租户ID */
    private String tenantId;
    /** 所属组织ID */
    private String orgId;
    /** 岗位名称 */
    private String name;
    /** 岗位编码，租户内唯一 */
    private String code;
    /** 岗位等级，1-10 */
    private Integer level;
    /** 上级岗位ID */
    private String parentId;
    /** 岗位状态: active(启用)/inactive(停用) */
    private String status;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public PositionDO() {
    }

    public String getPositionId() { return positionId; }
    public void setPositionId(String positionId) { this.positionId = positionId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
