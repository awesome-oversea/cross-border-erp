package com.aidotnet.erp.dashboard.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * AI洞察卡片数据对象
 * <p>
 * 描述: 对应dashboard_ai_insight_card表，用于存储AI生成的业务洞察卡片。
 *       洞察卡片由各业务域事件触发AI分析生成，为用户提供智能决策建议。
 * </p>
 * <p>
 * 业务规则:
 *   1. 洞察类型(insightType): anomaly(异常)/trend(趋势)/opportunity(机会)/risk(风险)
 *   2. 严重等级(severity): critical/high/medium/low，按等级降序展示
 *   3. data存储洞察数据JSONB，如异常指标值、趋势变化率等
 *   4. sourceDomain标识来源域，如: OMS/WMS/ADS/FMS
 *   5. validUntil过期后不再展示，自动清理
 *   6. isDismissed=true表示用户已忽略此洞察
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.dashboard.domain.AIInsightCard
 */
@TableName("dashboard_ai_insight_card")
public class AIInsightCardDO {

    /** 卡片唯一标识，UUID格式 */
    @TableId(type = IdType.ASSIGN_ID)
    private String cardId;

    /** 租户ID，实现多租户数据隔离 */
    private String tenantId;

    /** 用户ID，关联IAM域用户 */
    private String userId;

    /** 洞察标题 */
    private String title;

    /** 洞察摘要 */
    private String summary;

    /** 洞察类型: anomaly/trend/opportunity/risk */
    private String insightType;

    /** 严重等级: critical/high/medium/low */
    private String severity;

    /** 洞察数据JSONB，存储异常值、趋势变化率等 */
    private String data;

    /** 来源域，如: OMS/WMS/ADS/FMS */
    private String sourceDomain;

    /** AI建议 */
    private String suggestion;

    /** 操作链接，点击后跳转到对应业务页面 */
    private String actionUrl;

    /** 是否已读 */
    private boolean isRead;

    /** 是否已忽略 */
    private boolean isDismissed;

    /** 过期时间，过期后不再展示 */
    private Instant validUntil;

    /** 创建时间，UTC时区 */
    private Instant createdAt;

    /** 更新时间，UTC时区 */
    private Instant updatedAt;

    public AIInsightCardDO() {}

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getInsightType() { return insightType; }
    public void setInsightType(String insightType) { this.insightType = insightType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getSourceDomain() { return sourceDomain; }
    public void setSourceDomain(String sourceDomain) { this.sourceDomain = sourceDomain; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public boolean isDismissed() { return isDismissed; }
    public void setDismissed(boolean dismissed) { isDismissed = dismissed; }
    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
