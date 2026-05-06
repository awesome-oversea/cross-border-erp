package com.aidotnet.erp.iam.domain;

import java.util.Set;

/**
 * 用户数据范围领域模型
 * <p>
 * 描述: 数据权限范围定义，控制用户可访问的数据范围。
 *       支持按资源类型和资源ID集合限定数据访问边界。
 * </p>
 * <p>
 * 业务规则:
 *   1. 数据范围类型(scopeType): ALL(全部)、DEPT(本部门)、DEPT_AND_SUB(本部门及下级)、CUSTOM(自定义)
 *   2. CUSTOM类型需指定resourceIds
 *   3. 同一用户同一资源类型只能有一个数据范围
 * </p>
 *
 * @param scopeId      数据范围唯一标识
 * @param userId       用户ID
 * @param tenantId     租户ID
 * @param resourceType 资源类型，如 order、product、customer
 * @param resourceIds  资源ID集合，CUSTOM类型时有效
 * @param scopeType    范围类型: ALL/DEPT/DEPT_AND_SUB/CUSTOM
 * @author ERP系统
 */
public record UserDataScope(
        String scopeId,
        String userId,
        String tenantId,
        String resourceType,
        Set<String> resourceIds,
        String scopeType
) {
}
