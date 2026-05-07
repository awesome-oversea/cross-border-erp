package com.aidotnet.erp.som.domain;

import java.time.Instant;
import java.util.List;

/**
 * 销售小组领域模型
 * <p>
 * 描述: 销售小组用于分组管理运营人员和统计业绩，
 *       支持小组维度的销售数据分析和绩效考核。
 * </p>
 * <p>
 * 业务规则:
 *   1. 一个运营人员只能属于一个销售小组
 *   2. 小组可以关联多个店铺用于业绩统计
 *   3. 小组负责人可查看组内所有Listing和业绩数据
 * </p>
 *
 * @author ERP系统
 */
public record SalesTeam(
        String teamId,
        String tenantId,
        String teamName,
        String leaderId,
        List<String> memberIds,
        List<String> storeIds,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
