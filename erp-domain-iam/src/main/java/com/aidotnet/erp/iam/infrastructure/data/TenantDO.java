package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 租户数据对象
 * <p>
 * 描述: 对应iam_tenant表，存储租户基本信息。
 *       租户是系统最顶层的数据隔离维度。
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_tenant")
public class TenantDO {

    /** 租户唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String tenantId;
    /** 租户名称 */
    private String name;
    /** 租户编码 */
    private String code;
    /** 租户状态: ACTIVE(正常)/DISABLED(停用) */
    private String status;
    /** 套餐类型: standard/professional/enterprise */
    private String plan;
    /** 过期时间，null表示永不过期 */
    private Instant expireAt;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public TenantDO() {
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public Instant getExpireAt() { return expireAt; }
    public void setExpireAt(Instant expireAt) { this.expireAt = expireAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
