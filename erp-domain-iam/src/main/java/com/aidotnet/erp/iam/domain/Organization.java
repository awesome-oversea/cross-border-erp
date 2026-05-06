package com.aidotnet.erp.iam.domain;

import java.time.Instant;

/**
 * 组织领域模型
 * <p>
 * 描述: 组织架构实体，支持树形层级结构。
 *       组织是部门的上级容器，一个组织下可包含多个部门。
 *       通过path和level实现高效的层级查询。
 * </p>
 * <p>
 * 业务规则:
 *   1. 组织架构为树形结构，通过parentOrgId关联上级组织
 *   2. path记录从根到当前节点的路径，如 /root/child/grandchild/
 *   3. level表示层级深度，根组织level=1
 *   4. 组织类型(type): company(公司)、division(事业部)、team(团队)
 *   5. 组织不可跨租户引用
 * </p>
 *
 * @param orgId       组织唯一标识
 * @param tenantId    租户ID
 * @param name        组织名称
 * @param parentOrgId 上级组织ID，null表示顶级组织
 * @param type        组织类型: company(公司)、division(事业部)、team(团队)
 * @param path        层级路径，如 /org1/org2/org3/
 * @param level       层级深度，根组织为1
 * @param managerId   负责人用户ID
 * @param createdAt   创建时间
 * @param updatedAt   更新时间
 * @author ERP系统
 */
public record Organization(
        String orgId,
        String tenantId,
        String name,
        String parentOrgId,
        String type,
        String path,
        int level,
        String managerId,
        Instant createdAt,
        Instant updatedAt
) {}
