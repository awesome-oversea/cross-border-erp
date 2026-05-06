package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 物流规则数据对象(LogisticsRuleDO)
 * <p>
 * 描述: 物流规则数据对象，对应sys_logistics_rule表。
 *       定义物流渠道的费用计算规则和时效预估，支持按国家、渠道和重量区间匹配。
 *       费用计算公式: 总费用 = baseCost + (实际重量 - weightMinKg) × costPerKg。
 *       优先级数值越小优先级越高，匹配时取优先级最高的规则。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一租户下(countryCode, channel, weightMinKg, weightMaxKg)建议唯一
 *   2. weightMinKg必须小于weightMaxKg
 *   3. enabled=false的规则不参与物流计算
 *   4. priority数值越小优先级越高
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_logistics_rule
 *   - 主键: rule_id (ASSIGN_ID策略)
 *   - 索引: (tenant_id, country_code, channel), (tenant_id, enabled, priority)
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_logistics_rule")
public class LogisticsRuleDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String ruleId;
    private String tenantId;
    private String ruleName;
    private String countryCode;
    private String channel;
    private BigDecimal weightMinKg;
    private BigDecimal weightMaxKg;
    private BigDecimal baseCost;
    private BigDecimal costPerKg;
    private int estimatedDaysMin;
    private int estimatedDaysMax;
    private boolean enabled;
    private int priority;
    private Instant createdAt;
    private Instant updatedAt;

    public LogisticsRuleDO() {}

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public BigDecimal getWeightMinKg() { return weightMinKg; }
    public void setWeightMinKg(BigDecimal weightMinKg) { this.weightMinKg = weightMinKg; }
    public BigDecimal getWeightMaxKg() { return weightMaxKg; }
    public void setWeightMaxKg(BigDecimal weightMaxKg) { this.weightMaxKg = weightMaxKg; }
    public BigDecimal getBaseCost() { return baseCost; }
    public void setBaseCost(BigDecimal baseCost) { this.baseCost = baseCost; }
    public BigDecimal getCostPerKg() { return costPerKg; }
    public void setCostPerKg(BigDecimal costPerKg) { this.costPerKg = costPerKg; }
    public int getEstimatedDaysMin() { return estimatedDaysMin; }
    public void setEstimatedDaysMin(int estimatedDaysMin) { this.estimatedDaysMin = estimatedDaysMin; }
    public int getEstimatedDaysMax() { return estimatedDaysMax; }
    public void setEstimatedDaysMax(int estimatedDaysMax) { this.estimatedDaysMax = estimatedDaysMax; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
