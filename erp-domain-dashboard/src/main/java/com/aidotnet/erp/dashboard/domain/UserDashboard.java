package com.aidotnet.erp.dashboard.domain;

import java.time.Instant;

/**
 * 用户仪表盘领域模型
 * <p>
 * 描述: 用户自定义仪表盘配置实体，每个用户可拥有多个仪表盘布局。
 *       支持拖拽式布局配置和默认仪表盘设置。
 * </p>
 * <p>
 * 业务规则:
 *   1. 每个用户只能有一个默认仪表盘(isDefault=true)
 *   2. 设置新默认仪表盘时，自动取消原默认仪表盘
 *   3. 布局配置(layoutConfig)为JSON格式，描述组件位置和大小
 *   4. 组件列表(widgets)为JSON数组，引用DashboardWidget的widgetId
 * </p>
 *
 * @param dashboardId  仪表盘唯一标识
 * @param tenantId     租户ID
 * @param userId       用户ID
 * @param layoutConfig 布局配置，JSON格式，如 {"cols":12,"rows":[{"id":"w1","x":0,"y":0,"w":6,"h":4}]}
 * @param widgets      组件列表，JSON数组，引用组件ID
 * @param isDefault    是否为默认仪表盘
 * @param createdAt    创建时间
 * @param updatedAt    更新时间
 * @author ERP系统
 */
public record UserDashboard(String dashboardId, String tenantId, String userId, String layoutConfig,
                            String widgets, boolean isDefault, Instant createdAt, Instant updatedAt) {}
