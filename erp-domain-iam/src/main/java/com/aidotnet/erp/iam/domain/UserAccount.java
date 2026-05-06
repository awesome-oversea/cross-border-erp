package com.aidotnet.erp.iam.domain;

import java.time.Instant;

/**
 * 用户账户领域模型
 * <p>
 * 描述: 系统用户账户实体，是IAM域的核心实体之一。
 *       用户通过账户登录系统，关联角色获得权限，关联组织/部门/岗位确定归属。
 * </p>
 * <p>
 * 业务规则:
 *   1. 用户名(username)在同一租户下唯一
 *   2. 密码存储为BCrypt哈希值，不可明文存储
 *   3. 用户状态(enabled+status)双重控制: enabled控制登录，status控制业务权限
 *   4. 用户必须归属于某个租户(tenantId)
 * </p>
 *
 * @param userId       用户唯一标识
 * @param tenantId     租户ID，实现多租户隔离
 * @param orgId        所属组织ID
 * @param username     用户名，租户内唯一，用于登录
 * @param email        邮箱地址
 * @param phone        手机号码
 * @param passwordHash 密码哈希值(BCrypt)
 * @param enabled      是否启用，false时无法登录
 * @param status       业务状态: active(正常)、locked(锁定)、inactive(未激活)
 * @param lastLogin    最后登录时间
 * @param createdAt    创建时间
 * @param updatedAt    更新时间
 * @author ERP系统
 */
public record UserAccount(
        String userId,
        String tenantId,
        String orgId,
        String username,
        String email,
        String phone,
        String passwordHash,
        boolean enabled,
        String status,
        Instant lastLogin,
        Instant createdAt,
        Instant updatedAt
) {}
