package com.aidotnet.erp.dashboard.domain;

import java.time.Instant;

/**
 * 公告领域模型
 * <p>
 * 描述: 系统公告实体，支持创建、发布、过期等全生命周期管理。
 *       公告可按类型分类(系统/业务/紧急)，按优先级排序展示。
 * </p>
 * <p>
 * 业务规则:
 *   1. 公告创建后为DRAFT状态，需发布后才对用户可见
 *   2. 已发布公告不可修改内容，仅可撤回
 *   3. 过期公告自动从展示列表中过滤
 * </p>
 *
 * @param announceId  公告唯一标识
 * @param tenantId    租户ID
 * @param title       公告标题
 * @param content     公告内容，支持富文本
 * @param type        公告类型: SYSTEM(系统公告)、BUSINESS(业务公告)、URGENT(紧急公告)
 * @param priority    优先级，数值越大越靠前
 * @param publishTime 计划发布时间
 * @param expireTime  过期时间，过期后不再展示
 * @param status      公告状态: DRAFT(草稿)、PUBLISHED(已发布)、EXPIRED(已过期)
 * @param createdAt   创建时间
 * @author ERP系统
 */
public record Announcement(String announceId, String tenantId, String title, String content,
                           AnnouncementType type, int priority, Instant publishTime,
                           Instant expireTime, AnnouncementStatus status, Instant createdAt) {}
