package com.aidotnet.erp.iam.domain;

import java.time.Instant;
import java.util.List;

/**
 * 对象权限领域模型
 * <p>
 * 描述: 细粒度对象级权限，控制用户对特定业务对象的操作权限。
 *       如控制用户只能操作自己创建的订单、只能查看特定仓库的库存等。
 * </p>
 * <p>
 * 业务规则:
 *   1. 对象权限优先级高于角色权限
 *   2. permissions为允许的操作列表，如 [read, write, approve]
 *   3. grantedBy记录授权人，支持权限追溯
 * </p>
 *
 * @param objPermId   对象权限唯一标识
 * @param tenantId    租户ID
 * @param userId      用户ID
 * @param resourceType 资源类型，如 order、warehouse、supplier
 * @param resourceId  资源实例ID
 * @param permissions 允许的操作列表，如 [read, write, approve]
 * @param grantedBy   授权人用户ID
 * @param createdAt   创建时间
 * @author ERP系统
 */
public record ObjectPermission(
        String objPermId,
        String tenantId,
        String userId,
        String resourceType,
        String resourceId,
        List<String> permissions,
        String grantedBy,
        Instant createdAt
) {}
