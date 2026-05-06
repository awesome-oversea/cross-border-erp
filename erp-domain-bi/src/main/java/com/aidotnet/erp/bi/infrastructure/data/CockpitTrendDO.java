package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 驾驶舱趋势数据对象
 * <p>
 * 描述: 对应bi_cockpit_trend表，用于存储经营驾驶舱的趋势图数据。
 *       每条记录代表一个指标在某段时间内的趋势数据点集合，
 *       以JSONB格式存储dataPoints，支持灵活的时间序列展示。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一指标编码+周期可存在多条趋势记录(按生成时间区分)
 *   2. dataPoints以JSON数组格式存储，每个元素包含timestamp/value/targetValue
 *   3. 趋势数据由系统定时任务或手动触发生成
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.CockpitTrend
 */
@TableName("bi_cockpit_trend")
public class CockpitTrendDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String trendId;

    private String tenantId;

    private String metricCode;

    private String metricName;

    private String period;

    private String dataPoints;

    private Instant generatedAt;

    public CockpitTrendDO() {}

    public String getTrendId() { return trendId; }
    public void setTrendId(String trendId) { this.trendId = trendId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getDataPoints() { return dataPoints; }
    public void setDataPoints(String dataPoints) { this.dataPoints = dataPoints; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
