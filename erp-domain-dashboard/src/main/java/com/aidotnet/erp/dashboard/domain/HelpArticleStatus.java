package com.aidotnet.erp.dashboard.domain;

/**
 * 帮助文章状态枚举
 * <p>
 * 描述: 定义帮助文章的生命周期状态
 * </p>
 */
public enum HelpArticleStatus {
    /** 草稿 - 文章已创建但未发布 */
    DRAFT,
    /** 已发布 - 文章已发布，用户可见 */
    PUBLISHED,
    /** 已归档 - 文章已归档，不再展示但保留数据 */
    ARCHIVED
}
