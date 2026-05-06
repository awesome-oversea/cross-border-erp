package com.aidotnet.erp.dashboard.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 仪表盘指标领域模型
 * <p>
 * 描述: 工作台域核心指标实体，用于记录和展示各业务域的关键运营指标数据。
 *       支持AI看板的数据驱动展示，如销售额、订单量、库存周转率等。
 * </p>
 * <p>
 * 业务规则:
 *   1. 指标编码(metricCode)在同一租户下唯一
 *   2. 指标值(metricValue)采用BigDecimal保证精度
 *   3. 指标更新时自动刷新updatedAt时间戳
 * </p>
 *
 * @param metricId    指标唯一标识，UUID格式
 * @param tenantId    租户ID，实现多租户数据隔离
 * @param metricCode  指标编码，租户内唯一，如 DAILY_SALES、ORDER_COUNT
 * @param metricName  指标名称，用于前端展示，如 "日销售额"、"订单量"
 * @param metricValue 指标值，BigDecimal保证金额和比率精度
 * @param unit        指标单位，如 CNY、USD、%、件
 * @param updatedAt   最后更新时间，UTC时区
 * @author ERP系统
 */
public record DashboardMetric(String metricId, String tenantId, String metricCode, String metricName,
                              BigDecimal metricValue, String unit, Instant updatedAt) {}
