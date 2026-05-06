package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * KPI考核记录数据对象
 * <p>
 * 描述: 对应bi_kpi_assessment表，用于存储KPI考核评估记录。
 *       每条记录代表某用户在某考核周期内对特定KPI目标的评估结果，
 *       包含实际值、达成率、评分、考核状态和评审意见。
 * </p>
 * <p>
 * 业务规则:
 *   1. 考核记录关联KPI目标(targetId)，目标禁用时不可创建考核
 *   2. 达成率(achievementRate) = 实际值/目标值 * 100
 *   3. 评分(score)根据评分规则(LINEAR/THRESHOLD)计算
 *   4. 考核状态(status)根据达成率和目标阈值自动判定
 *   5. 考核记录创建后不可修改，确保考核公正性
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.KpiAssessment
 */
@TableName("bi_kpi_assessment")
public class KpiAssessmentDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String assessmentId;

    private String tenantId;

    private String targetId;

    private String kpiCode;

    private String kpiName;

    private String department;

    private String userId;

    private String period;

    private BigDecimal actualValue;

    private BigDecimal targetValue;

    private BigDecimal achievementRate;

    private BigDecimal score;

    private String status;

    private String assessorId;

    private String comment;

    private Instant assessedAt;

    private Instant createdAt;

    public KpiAssessmentDO() {}

    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = assessmentId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getKpiCode() { return kpiCode; }
    public void setKpiCode(String kpiCode) { this.kpiCode = kpiCode; }
    public String getKpiName() { return kpiName; }
    public void setKpiName(String kpiName) { this.kpiName = kpiName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public BigDecimal getActualValue() { return actualValue; }
    public void setActualValue(BigDecimal actualValue) { this.actualValue = actualValue; }
    public BigDecimal getTargetValue() { return targetValue; }
    public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }
    public BigDecimal getAchievementRate() { return achievementRate; }
    public void setAchievementRate(BigDecimal achievementRate) { this.achievementRate = achievementRate; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssessorId() { return assessorId; }
    public void setAssessorId(String assessorId) { this.assessorId = assessorId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Instant getAssessedAt() { return assessedAt; }
    public void setAssessedAt(Instant assessedAt) { this.assessedAt = assessedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
