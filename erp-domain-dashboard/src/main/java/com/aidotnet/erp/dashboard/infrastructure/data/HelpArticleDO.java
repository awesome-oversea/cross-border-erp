package com.aidotnet.erp.dashboard.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 帮助文章数据对象
 * <p>
 * 描述: 对应dashboard_help_article表，用于存储帮助文档和知识库文章。
 *       支持按分类浏览和全文检索，浏览量自动累加。
 * </p>
 * <p>
 * 业务规则:
 *   1. 文章状态(status): DRAFT(草稿)/PUBLISHED(已发布)/ARCHIVED(已归档)
 *   2. 分类(category)用于前端按类目筛选
 *   3. tags存储标签JSON数组，支持多标签检索
 *   4. viewCount浏览量，每次查看自动+1
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.dashboard.domain.HelpArticle
 */
@TableName("dashboard_help_article")
public class HelpArticleDO {

    /** 文章唯一标识，UUID格式 */
    @TableId(type = IdType.ASSIGN_ID)
    private String articleId;

    /** 租户ID，实现多租户数据隔离 */
    private String tenantId;

    /** 文章标题 */
    private String title;

    /** 文章内容，支持富文本 */
    private String content;

    /** 文章分类，如: getting_started/faq/advanced */
    private String category;

    /** 标签JSON数组，如: ["入门","配置"] */
    private String tags;

    /** 浏览次数，每次查看自动+1 */
    private int viewCount;

    /** 文章状态: DRAFT/PUBLISHED/ARCHIVED */
    private String status;

    /** 创建时间，UTC时区 */
    private Instant createdAt;

    /** 更新时间，UTC时区 */
    private Instant updatedAt;

    public HelpArticleDO() {}

    public String getArticleId() { return articleId; }
    public void setArticleId(String articleId) { this.articleId = articleId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
