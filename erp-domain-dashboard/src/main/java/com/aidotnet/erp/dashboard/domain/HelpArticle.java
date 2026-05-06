package com.aidotnet.erp.dashboard.domain;

import java.time.Instant;

/**
 * 帮助文章领域模型
 * <p>
 * 描述: 系统帮助文档实体，为用户提供操作指南和业务说明。
 *       支持分类管理、标签搜索和浏览量统计。
 * </p>
 * <p>
 * 业务规则:
 *   1. 文章创建后为DRAFT状态，需发布后才对用户可见
 *   2. 每次浏览自动递增viewCount
 *   3. ARCHIVED状态的文章不再展示但保留数据
 * </p>
 *
 * @param articleId  文章唯一标识
 * @param tenantId   租户ID
 * @param title      文章标题
 * @param content    文章内容，支持富文本/Markdown
 * @param category   文章分类，如 GETTING_STARTED(入门)、OPERATION(操作)、FAQ(常见问题)
 * @param tags       文章标签，逗号分隔，用于搜索和筛选
 * @param viewCount  浏览次数
 * @param status     文章状态: DRAFT(草稿)、PUBLISHED(已发布)、ARCHIVED(已归档)
 * @param createdAt  创建时间
 * @param updatedAt  更新时间
 * @author ERP系统
 */
public record HelpArticle(String articleId, String tenantId, String title, String content,
                          String category, String tags, int viewCount, HelpArticleStatus status,
                          Instant createdAt, Instant updatedAt) {}
