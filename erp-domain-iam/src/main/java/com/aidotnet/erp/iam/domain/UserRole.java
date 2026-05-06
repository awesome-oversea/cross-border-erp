package com.aidotnet.erp.iam.domain;

import java.time.Instant;

/**
 * 用户-角色关联领域模型
 * <p>
 * 描述: 用户与角色的多对多关联实体，支持用户在特定组织范围内拥有角色。
 *       一个用户可关联多个角色，一个角色可关联多个用户。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一用户在同一组织下不可重复关联同一角色
 *   2. orgId为null表示全局角色，非null表示组织范围内角色
 *   3. 用户的最终权限为所有角色权限的并集
 * </p>
 *
 * @param userId    用户ID
 * @param roleId    角色ID
 * @param orgId     组织ID，null表示全局角色
 * @param createdAt 创建时间
 * @author ERP系统
 */
public record UserRole(
        String userId,
        String roleId,
        String orgId,
        Instant createdAt
) {}
