package com.aidotnet.erp.iam.domain;

import java.time.Instant;

/**
 * 部门领域模型
 * <p>
 * 描述: 部门实体，隶属于组织架构下的具体业务单元。
 *       部门支持树形层级结构，通过parentDeptId关联上级部门。
 * </p>
 * <p>
 * 业务规则:
 *   1. 部门必须归属于某个组织(orgId)
 *   2. 部门支持树形结构，通过parentDeptId关联上级部门
 *   3. 停用部门不影响已有用户关联，但新用户不可分配到停用部门
 *   4. 部门负责人(managerId)必须是已存在的用户
 * </p>
 *
 * @param deptId       部门唯一标识
 * @param tenantId     租户ID
 * @param name         部门名称
 * @param parentDeptId 上级部门ID，null表示顶级部门
 * @param orgId        所属组织ID
 * @param managerId    部门负责人用户ID
 * @param enabled      是否启用
 * @param createdAt    创建时间
 * @param updatedAt    更新时间
 * @author ERP系统
 */
public record Department(
        String deptId,
        String tenantId,
        String name,
        String parentDeptId,
        String orgId,
        String managerId,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * 创建启用/停用状态变更后的新部门对象
     * <p>
     * 不可变对象模式，通过withXxx方法创建新实例
     * </p>
     *
     * @param enabled 新的启用状态
     * @return 状态变更后的新部门对象
     */
    public Department withEnabled(boolean enabled) {
        return new Department(deptId, tenantId, name, parentDeptId, orgId, managerId, enabled, createdAt, Instant.now());
    }
}
