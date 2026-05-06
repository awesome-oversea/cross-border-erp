package com.aidotnet.erp.iam.domain;

import java.time.Instant;
import java.util.Set;

/**
 * 角色领域模型
 * <p>
 * 描述: 权限角色实体，采用RBAC(Role-Based Access Control)模型。
 *       角色是权限的集合，用户通过关联角色获得对应权限。
 * </p>
 * <p>
 * 业务规则:
 *   1. 角色编码(code)在同一租户下唯一
 *   2. 角色类型(type): system(系统内置)、custom(自定义)
 *   3. 系统内置角色不可删除，仅可修改权限
 *   4. 角色权限(permIds)为权限编码集合，如 iam:tenant:read
 * </p>
 *
 * @param roleId    角色唯一标识
 * @param tenantId  租户ID
 * @param name      角色名称，如 "管理员"、"运营人员"
 * @param code      角色编码，租户内唯一，如 ADMIN、OPERATOR
 * @param type      角色类型: system(系统内置)、custom(自定义)
 * @param status    角色状态: active(启用)、inactive(停用)
 * @param permIds   权限ID集合，关联Permission.permId
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @author ERP系统
 */
public record Role(
        String roleId,
        String tenantId,
        String name,
        String code,
        String type,
        String status,
        Set<String> permIds,
        Instant createdAt,
        Instant updatedAt
) {}
