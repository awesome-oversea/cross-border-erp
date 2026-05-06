package com.aidotnet.erp.iam.domain;

import java.time.Instant;
import java.util.Set;

/**
 * 认证令牌领域模型
 * <p>
 * 描述: JWT认证令牌实体，承载用户认证状态和权限信息。
 *       Token签发后存储在服务端，支持主动注销和过期检查。
 * </p>
 * <p>
 * 安全规则:
 *   1. Token有效期默认2小时
 *   2. Token包含租户ID、用户ID、用户名、权限列表
 *   3. 登出时Token从服务端移除
 *   4. Token过期后需重新登录
 * </p>
 *
 * @param token       JWT令牌字符串
 * @param tenantId    租户ID
 * @param userId      用户ID
 * @param username    用户名
 * @param permissions 用户权限编码集合
 * @param expiresAt   过期时间
 * @author ERP系统
 */
public record AuthToken(String token, String tenantId, String userId, String username, Set<String> permissions,
                        Instant expiresAt) {

    /**
     * 判断Token是否已过期
     *
     * @return true-已过期，false-未过期
     */
    public boolean expired() {
        return Instant.now().isAfter(expiresAt);
    }
}
