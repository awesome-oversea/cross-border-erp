package com.aidotnet.erp.dashboard.domain;

import java.time.Instant;
import java.util.Map;

/**
 * 仪表盘组件领域模型
 * <p>
 * 描述: 可配置的仪表盘展示组件，支持多种数据源和刷新策略。
 *       每个组件对应一个可视化单元(图表/表格/指标卡等)。
 * </p>
 * <p>
 * 业务规则:
 *   1. 组件类型(type)决定前端渲染方式: LINE_CHART/BAR_CHART/PIE_CHART/METRIC_CARD/TABLE
 *   2. 数据源(dataSource)指向BI域的指标定义或实时API
 *   3. 刷新频率(refreshRateSeconds)最小30秒，避免频繁请求
 *   4. DEPRECATED状态的组件不再展示但保留历史数据
 * </p>
 *
 * @param widgetId           组件唯一标识
 * @param tenantId           租户ID
 * @param type               组件类型，如 LINE_CHART、METRIC_CARD、TABLE
 * @param name               组件名称
 * @param dataSource         数据源标识，关联BI域指标定义
 * @param refreshRateSeconds 刷新频率(秒)，0表示不自动刷新
 * @param config             组件配置，JSON格式，包含样式、过滤条件等
 * @param status             组件状态: ACTIVE(启用)、INACTIVE(停用)、DEPRECATED(已废弃)
 * @param category           组件分类，如 SALES(销售)、INVENTORY(库存)、FINANCE(财务)
 * @param description        组件描述
 * @param createdAt          创建时间
 * @param updatedAt          更新时间
 * @author ERP系统
 */
public record DashboardWidget(String widgetId, String tenantId, String type, String name, String dataSource,
                              int refreshRateSeconds, Map<String, Object> config, WidgetStatus status,
                              String category, String description, Instant createdAt, Instant updatedAt) {}
