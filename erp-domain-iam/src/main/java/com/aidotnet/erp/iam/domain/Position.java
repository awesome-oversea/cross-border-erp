package com.aidotnet.erp.iam.domain;

import java.time.Instant;

/**
 * 岗位领域模型
 * <p>
 * 描述: 岗位实体，定义组织内的职能角色(区别于权限角色)。
 *       岗位关联组织，支持层级结构，用于人员编制和职责划分。
 * </p>
 * <p>
 * 业务规则:
 *   1. 岗位编码(code)在同一租户下唯一
 *   2. 岗位必须归属于某个组织(orgId)
 *   3. 岗位层级(level)表示岗位等级，数值越大等级越高
 *   4. 岗位状态: active(启用)、inactive(停用)
 * </p>
 *
 * @param positionId 岗位唯一标识
 * @param tenantId   租户ID
 * @param orgId      所属组织ID
 * @param name       岗位名称，如 "采购经理"、"仓库主管"
 * @param code       岗位编码，租户内唯一，如 PM、WH_LEAD
 * @param level      岗位等级，1-10，数值越大等级越高
 * @param parentId   上级岗位ID，null表示顶级岗位
 * @param status     岗位状态: active(启用)、inactive(停用)
 * @param createdAt  创建时间
 * @param updatedAt  更新时间
 * @author ERP系统
 */
public record Position(
        String positionId,
        String tenantId,
        String orgId,
        String name,
        String code,
        int level,
        String parentId,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}
