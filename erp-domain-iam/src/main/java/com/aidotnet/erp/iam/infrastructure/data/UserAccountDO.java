package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 用户账户数据对象
 * <p>
 * 描述: 对应iam_user_account表，存储用户账户信息。
 *       用户名在同一租户下唯一，密码存储为BCrypt哈希值。
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_user_account")
public class UserAccountDO {

    /** 用户唯一标识 */
    @TableId(type = IdType.ASSIGN_ID)
    private String userId;
    /** 租户ID，实现多租户隔离 */
    private String tenantId;
    /** 所属组织ID */
    private String orgId;
    /** 用户名，租户内唯一 */
    private String username;
    /** 邮箱地址 */
    private String email;
    /** 手机号码 */
    private String phone;
    /** 密码哈希值(BCrypt) */
    private String passwordHash;
    /** 是否启用，false时无法登录 */
    private Boolean enabled;
    /** 业务状态: active(正常)/locked(锁定)/inactive(未激活) */
    private String status;
    /** 最后登录时间 */
    private Instant lastLogin;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public UserAccountDO() {
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getLastLogin() { return lastLogin; }
    public void setLastLogin(Instant lastLogin) { this.lastLogin = lastLogin; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
