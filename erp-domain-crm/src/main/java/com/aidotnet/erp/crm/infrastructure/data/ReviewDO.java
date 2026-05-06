package com.aidotnet.erp.crm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 评价数据对象
 * <p>
 * 描述: 对应crm_review表，存储客户产品评价。
 *       支持AI情感分析(sentiment字段)和评价回复标记(responded)。
 * </p>
 *
 * @author ERP系统
 */
@TableName("crm_review")
public class ReviewDO {

    /** 评价ID(主键) */
    @TableId(type = IdType.ASSIGN_ID)
    private String reviewId;
    /** 租户ID */
    private String tenantId;
    /** 客户ID */
    private String customerId;
    /** 产品ID */
    private String productId;
    /** 关联订单ID */
    private String orderId;
    /** 评价平台 */
    private String platform;
    /** 评分(1-5) */
    private int rating;
    /** 评价标题 */
    private String title;
    /** 评价内容 */
    private String text;
    /** AI情感分析结果 */
    private String sentiment;
    /** 是否已验证购买 */
    private Boolean verified;
    /** 是否已回复 */
    private Boolean responded;
    /** 评价日期 */
    private Instant reviewDate;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public ReviewDO() {}

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public Boolean getResponded() { return responded; }
    public void setResponded(Boolean responded) { this.responded = responded; }
    public Instant getReviewDate() { return reviewDate; }
    public void setReviewDate(Instant reviewDate) { this.reviewDate = reviewDate; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
