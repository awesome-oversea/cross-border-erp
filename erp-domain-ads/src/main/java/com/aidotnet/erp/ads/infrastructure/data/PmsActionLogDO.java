package com.aidotnet.erp.ads.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * PMS操作日志持久化对象，记录PMS(利润管理系统)自动调价的每次操作详情。
 * <p>
 * 对应数据库表: ads_pms_action_log
 * </p>
 * <p>
 * 业务说明:
 *   PMS操作日志是广告域与PMS系统集成的核心审计记录，确保AI优化引擎的
 *   每次操作可追溯、可审计、可回滚。日志记录操作前后的值变化、
 *   PMS决策原因、是否支持回滚及回滚状态。
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.ads.domain.PmsActionLog
 */
@TableName("ads_pms_action_log")
public class PmsActionLogDO {

    /** 日志唯一标识。 */
    @TableId(type = IdType.ASSIGN_ID)
    private String logId;

    /** 租户标识，用于多租户数据隔离。 */
    private String tenantId;

    /** 关联广告活动ID。 */
    private String campaignId;

    /** 关联关键词竞价ID。 */
    private String bidId;

    /** 操作类型: BID_ADJUST(出价调整)/KEYWORD_SUGGEST(关键词推荐)/PMS_OPTIMIZATION_TOGGLE(PMS优化开关)。 */
    private String actionType;

    /** 操作前值。 */
    private String beforeValue;

    /** 操作后值。 */
    private String afterValue;

    /** PMS决策原因，AI优化引擎给出的调整理由。 */
    private String pmsReason;

    /** 是否支持回滚。 */
    private Boolean canRollback;

    /** 是否已回滚。 */
    private Boolean rolledBack;

    /** 执行时间。 */
    private Instant executedAt;

    /** 回滚时间，若已回滚则记录回滚操作的时间。 */
    private Instant rolledBackAt;

    public PmsActionLogDO() {}

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public String getBidId() { return bidId; }
    public void setBidId(String bidId) { this.bidId = bidId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getBeforeValue() { return beforeValue; }
    public void setBeforeValue(String beforeValue) { this.beforeValue = beforeValue; }
    public String getAfterValue() { return afterValue; }
    public void setAfterValue(String afterValue) { this.afterValue = afterValue; }
    public String getPmsReason() { return pmsReason; }
    public void setPmsReason(String pmsReason) { this.pmsReason = pmsReason; }
    public Boolean getCanRollback() { return canRollback; }
    public void setCanRollback(Boolean canRollback) { this.canRollback = canRollback; }
    public Boolean getRolledBack() { return rolledBack; }
    public void setRolledBack(Boolean rolledBack) { this.rolledBack = rolledBack; }
    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
    public Instant getRolledBackAt() { return rolledBackAt; }
    public void setRolledBackAt(Instant rolledBackAt) { this.rolledBackAt = rolledBackAt; }
}
