package com.aidotnet.erp.iam.domain;

import java.time.Instant;

/**
 * 租户领域模型
 * <p>
 * 描述: 多租户隔离的核心实体，每个租户拥有独立的数据空间和配置。
 *       租户是ERP系统最顶层的数据隔离维度，所有业务数据均通过tenantId关联到租户。
 * </p>
 * <p>
 * 业务规则:
 *   1. 租户ID全局唯一，由系统分配或外部系统提供
 *   2. 租户状态控制整个租户的可用性: ACTIVE(正常)/DISABLED(停用)
 *   3. 租户过期后所有用户无法登录
 *   4. 套餐类型(plan)决定租户可使用的功能范围
 * </p>
 *
 * @param tenantId  租户唯一标识
 * @param name      租户名称，如 "XX跨境电商有限公司"
 * @param code      租户编码，如 "TENANT_001"
 * @param status    租户状态: ACTIVE(正常)、DISABLED(停用)
 * @param plan      套餐类型: standard(标准版)、professional(专业版)、enterprise(企业版)
 * @param expireAt  过期时间，null表示永不过期
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @author ERP系统
 */
public record Tenant(
        String tenantId,
        String name,
        String code,
        TenantStatus status,
        String plan,
        Instant expireAt,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * 判断租户是否处于活跃状态
     *
     * @return true-租户正常，false-租户已停用
     */
    public boolean active() {
        return status == TenantStatus.ACTIVE;
    }

    /**
     * 判断租户是否已过期
     *
     * @return true-已过期，false-未过期或永不过期
     */
    public boolean expired() {
        return expireAt != null && Instant.now().isAfter(expireAt);
    }
}
