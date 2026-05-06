package com.aidotnet.erp.som.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 价格规则领域模型
 * <p>
 * 描述: Listing定价策略规则，支持固定价、百分比、竞品跟踪、成本加成等类型。
 *       规则按顺序叠加应用，最终计算出售价。
 * </p>
 *
 * @param ruleId    规则唯一标识
 * @param tenantId  租户ID
 * @param name      规则名称
 * @param type      规则类型: FIXED/PERCENTAGE/COMPETITIVE/COST_PLUS
 * @param conditions 触发条件(JSON)
 * @param actions    执行动作(JSON)
 * @param minPrice  最低保底价
 * @param maxPrice  最高限价
 * @param status    状态: ACTIVE/INACTIVE/ARCHIVED
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @author ERP系统
 */
public record PriceRule(String ruleId, String tenantId, String name, PriceRuleType type,
                        String conditions, String actions, BigDecimal minPrice, BigDecimal maxPrice,
                        PriceRuleStatus status, Instant createdAt, Instant updatedAt) {}
