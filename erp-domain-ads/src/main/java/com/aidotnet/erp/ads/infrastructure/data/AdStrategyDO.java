package com.aidotnet.erp.ads.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 广告策略持久化对象，承载广告自动调价策略的核心配置与执行状态。
 * <p>
 * 对应数据库表: ads_strategy
 * </p>
 * <p>
 * 业务说明:
 *   广告策略定义了广告投放的自动化规则，包括出价调整、预算分配、
 *   关键词挖掘和关键词推荐四种策略类型。策略支持定时调度执行，
 *   并可由PMS(利润管理系统)自动生成。
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.ads.domain.AdStrategy
 */
@TableName("ads_strategy")
public class AdStrategyDO {

    /** 策略唯一标识，由系统自动生成。 */
    @TableId(type = IdType.ASSIGN_ID)
    private String strategyId;

    /** 租户标识，用于多租户数据隔离。 */
    private String tenantId;

    /** 策略编码，租户内唯一，如 BID_ADJ_001。 */
    private String strategyCode;

    /** 策略名称，用于前端展示，如 "自动出价调整策略"。 */
    private String strategyName;

    /** 策略类型: BID_ADJUSTMENT(出价调整)/BUDGET_ADJUSTMENT(预算分配)/KEYWORD_HARVESTING(关键词挖掘)/KEYWORD_SUGGESTION(关键词推荐)。 */
    private String strategyType;

    /** 目标广告活动ID，策略作用的目标广告活动。 */
    private String targetCampaignId;

    /** 触发条件JSON，定义策略执行的触发条件。 */
    private String conditionsJson;

    /** 执行动作JSON，定义策略触发后执行的操作。 */
    private String actionsJson;

    /** 是否由PMS自动生成。 */
    private Boolean pmsGenerated;

    /** 调度表达式，Cron格式，定义策略的定时执行计划。 */
    private String scheduleExpression;

    /** 策略状态: DRAFT(草稿)/PENDING(待审核)/ACTIVE(启用)/DISABLED(停用)/ARCHIVED(归档)。 */
    private String status;

    /** 最后执行时间，记录策略最近一次执行的时间戳。 */
    private Instant lastExecutedAt;

    /** 创建时间。 */
    private Instant createdAt;

    /** 更新时间。 */
    private Instant updatedAt;

    public AdStrategyDO() {}

    public String getStrategyId() { return strategyId; }
    public void setStrategyId(String strategyId) { this.strategyId = strategyId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getStrategyCode() { return strategyCode; }
    public void setStrategyCode(String strategyCode) { this.strategyCode = strategyCode; }
    public String getStrategyName() { return strategyName; }
    public void setStrategyName(String strategyName) { this.strategyName = strategyName; }
    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
    public String getTargetCampaignId() { return targetCampaignId; }
    public void setTargetCampaignId(String targetCampaignId) { this.targetCampaignId = targetCampaignId; }
    public String getConditionsJson() { return conditionsJson; }
    public void setConditionsJson(String conditionsJson) { this.conditionsJson = conditionsJson; }
    public String getActionsJson() { return actionsJson; }
    public void setActionsJson(String actionsJson) { this.actionsJson = actionsJson; }
    public Boolean getPmsGenerated() { return pmsGenerated; }
    public void setPmsGenerated(Boolean pmsGenerated) { this.pmsGenerated = pmsGenerated; }
    public String getScheduleExpression() { return scheduleExpression; }
    public void setScheduleExpression(String scheduleExpression) { this.scheduleExpression = scheduleExpression; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getLastExecutedAt() { return lastExecutedAt; }
    public void setLastExecutedAt(Instant lastExecutedAt) { this.lastExecutedAt = lastExecutedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
