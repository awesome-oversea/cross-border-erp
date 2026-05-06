package com.aidotnet.erp.iam.domain;

import java.time.Instant;

/**
 * 权限领域模型
 * <p>
 * 描述: 细粒度权限定义，采用 resource:action 格式编码。
 *       权限是系统访问控制的最小单元，通过角色关联到用户。
 * </p>
 * <p>
 * 权限编码规范: {域}:{资源}:{操作}
 *   - iam:tenant:read  - 读取租户信息
 *   - iam:user:write   - 写入用户信息
 *   - oms:order:read   - 读取订单信息
 * </p>
 * <p>
 * 业务规则:
 *   1. 权限编码(permId)全局唯一
 *   2. 写权限隐含读权限(通过parentCode关联)
 *   3. 权限状态: active(启用)、inactive(停用)
 * </p>
 *
 * @param permId     权限唯一标识，格式为 {域}:{资源}:{操作}
 * @param resource   资源类型，如 tenant、user、role、order
 * @param action     操作类型: read(读)、write(写)
 * @param name       权限名称，如 "读取租户"、"写入用户"
 * @param parentCode 父权限编码，写权限的父权限为对应读权限
 * @param status     权限状态: active(启用)、inactive(停用)
 * @param createdAt  创建时间
 * @author ERP系统
 */
public record Permission(
        String permId,
        String resource,
        String action,
        String name,
        String parentCode,
        String status,
        Instant createdAt
) {}
