package com.aidotnet.erp.dashboard.domain;

import java.time.Instant;

/**
 * 快捷入口领域模型
 * <p>
 * 描述: 工作台快捷入口实体，用户可自定义常用功能的快速访问入口。
 *       支持按分类组织、自定义排序和图标。
 * </p>
 * <p>
 * 业务规则:
 *   1. 入口编码(entryCode)在同一用户下唯一
 *   2. 排序值(sortOrder)越小越靠前
 *   3. 每个用户最多配置20个快捷入口
 * </p>
 *
 * @param entryId   入口唯一标识
 * @param tenantId  租户ID
 * @param userId    用户ID
 * @param entryCode 入口编码，用户内唯一，如 CREATE_ORDER、VIEW_INVENTORY
 * @param entryName 入口名称，如 "创建订单"、"查看库存"
 * @param icon      图标标识，前端渲染用
 * @param url       跳转路径
 * @param category  入口分类，如 ORDER(订单)、PRODUCT(产品)、FINANCE(财务)
 * @param sortOrder 排序值，越小越靠前
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 * @author ERP系统
 */
public record QuickEntry(String entryId, String tenantId, String userId, String entryCode, String entryName,
                         String icon, String url, String category, int sortOrder,
                         Instant createdAt, Instant updatedAt) {}
