package com.aidotnet.erp.iam.infrastructure.data;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 认证令牌数据对象
 * <p>
 * 描述: 对应iam_auth_token表，存储JWT认证令牌信息。
 *       Token签发后存储在服务端，支持主动注销和过期检查。
 * </p>
 *
 * @author ERP系统
 */
@TableName("iam_auth_token")
public class AuthTokenDO {

    /** Token哈希值，用于快速查找 */
    private String tokenHash;
    /** 租户ID */
    private String tenantId;
    /** 用户ID */
    private String userId;
    /** 用户名 */
    private String username;
    /** 权限列表，逗号分隔 */
    private String permissions;
    /** 过期时间 */
    private Instant expiresAt;
    /** 创建时间 */
    private Instant createdAt;

    public AuthTokenDO() {
    }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
