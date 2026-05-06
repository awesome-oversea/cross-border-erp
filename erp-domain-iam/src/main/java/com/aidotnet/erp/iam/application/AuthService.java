package com.aidotnet.erp.iam.application;

import com.aidotnet.erp.common.context.TraceContext;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.security.JwtProvider;
import com.aidotnet.erp.iam.domain.AuditLog;
import com.aidotnet.erp.iam.domain.AuthToken;
import com.aidotnet.erp.iam.domain.Role;
import com.aidotnet.erp.iam.domain.UserAccount;
import com.aidotnet.erp.iam.infrastructure.IamStore;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 * <p>
 * 描述: IAM域认证服务，负责用户登录/登出、Token签发与验证、权限校验。
 *       基于JWT实现无状态认证，支持多租户隔离和细粒度权限控制。
 * </p>
 * <p>
 * 核心能力:
 *   1. 用户登录 - 校验租户状态、用户状态、密码匹配后签发JWT
 *   2. 用户登出 - 注销Token并记录审计日志
 *   3. Token验证 - 校验Token有效性
 *   4. 权限校验 - 验证用户是否拥有指定权限
 * </p>
 * <p>
 * 安全规则:
 *   1. 登录失败不暴露具体原因(用户名或密码错误)
 *   2. 租户停用或过期时禁止登录
 *   3. 用户停用时禁止登录
 *   4. Token有效期2小时
 *   5. 所有登录/登出操作记录审计日志
 * </p>
 *
 * @author ERP系统
 */
@Service
public class AuthService {

    private final IamStore iamStore;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数 - 依赖注入
     *
     * @param iamStore        IAM数据存储
     * @param jwtProvider     JWT令牌提供者
     * @param passwordEncoder 密码编码器(BCrypt)
     */
    public AuthService(IamStore iamStore, JwtProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.iamStore = iamStore;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录
     * <p>
     * 校验流程: 租户存在且有效 → 用户存在且启用 → 密码匹配 → 签发JWT。
     * 登录成功后更新用户lastLogin时间。
     * </p>
     *
     * @param tenantId 租户ID
     * @param username 用户名
     * @param password 密码(明文)
     * @return 认证令牌(包含JWT、用户信息、权限列表)
     * @throws BizException TENANT_NOT_FOUND - 租户不存在
     * @throws BizException TENANT_DISABLED  - 租户已停用
     * @throws BizException TENANT_EXPIRED   - 租户已过期
     * @throws BizException AUTH_FAILED      - 用户名或密码错误
     */
    public AuthToken login(String tenantId, String username, String password) {
        var tenant = iamStore.findTenant(tenantId).orElseThrow(() -> new BizException("TENANT_NOT_FOUND", "租户不存在"));
        if (!tenant.active()) {
            audit(tenantId, username, "LOGIN", "iam", username, false);
            throw new BizException("TENANT_DISABLED", "租户已停用");
        }
        if (tenant.expired()) {
            audit(tenantId, username, "LOGIN", "iam", username, false);
            throw new BizException("TENANT_EXPIRED", "租户已过期");
        }
        UserAccount user = iamStore.findUser(tenantId, username)
                .orElseThrow(() -> new BizException("AUTH_FAILED", "用户名或密码错误"));
        if (!user.enabled() || !passwordEncoder.matches(password, user.passwordHash())) {
            audit(tenantId, username, "LOGIN", "iam", username, false);
            throw new BizException("AUTH_FAILED", "用户名或密码错误");
        }
        Set<String> permissions = iamStore.findUserPermissions(user.userId());
        String jwt = jwtProvider.createToken(tenantId, user.userId(), username, permissions);
        AuthToken token = new AuthToken(jwt, tenantId, user.userId(), username, permissions,
                Instant.now().plus(2, ChronoUnit.HOURS));
        iamStore.saveToken(token);
        UserAccount updated = new UserAccount(user.userId(), user.tenantId(), user.orgId(), user.username(),
                user.email(), user.phone(), user.passwordHash(), user.enabled(), user.status(), Instant.now(), user.createdAt(), Instant.now());
        iamStore.saveUser(updated);
        audit(tenantId, username, "LOGIN", "iam", username, true);
        return token;
    }

    public void logout(String token) {
        iamStore.findToken(token).ifPresent(authToken -> audit(authToken.tenantId(), authToken.username(), "LOGOUT", "iam", authToken.username(), true));
        iamStore.removeToken(token);
    }

    public AuthToken verify(String token) {
        return iamStore.findToken(token).orElseThrow(() -> new BizException("UNAUTHORIZED", "认证失败"));
    }

    public void requirePermission(String token, String permission) {
        AuthToken authToken = verify(token);
        if (!authToken.permissions().contains(permission)) {
            throw new BizException("FORBIDDEN", "权限不足");
        }
    }

    private void audit(String tenantId, String actor, String action, String module, String target, boolean success) {
        iamStore.appendAudit(new AuditLog(UUID.randomUUID().toString(), tenantId, actor, action, module, target,
                TraceContext.getTraceId(), success, Instant.now()));
    }
}
