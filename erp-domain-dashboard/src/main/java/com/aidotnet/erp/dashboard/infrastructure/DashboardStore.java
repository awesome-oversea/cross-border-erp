package com.aidotnet.erp.dashboard.infrastructure;

import com.aidotnet.erp.dashboard.domain.DashboardMetric;
import com.aidotnet.erp.dashboard.infrastructure.data.DashboardMetricDO;
import com.aidotnet.erp.dashboard.infrastructure.mapper.DashboardMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * 仪表盘指标数据存储
 * <p>
 * 描述: 仪表盘指标的持久化存储实现，基于MyBatis将指标数据持久化到dashboard_metric表。
 *       指标数据来源于各业务域的聚合计算结果，为AI看板提供数据驱动展示。
 * </p>
 * <p>
 * 核心职责:
 *   1. 指标保存 - 采用upsert模式(存在则更新，不存在则插入)
 *   2. 指标查询 - 支持按metricId精确查询和按metricCode编码查询
 *   3. 指标删除 - 按metricId删除指定指标
 *   4. 指标列表 - 查询租户下所有指标
 * </p>
 * <p>
 * 存储规则:
 *   1. 所有查询必须带tenantId实现多租户隔离
 *   2. 按metricId为主键存储，支持按metricCode唯一索引查询
 *   3. metricCode在租户内唯一，由数据库UNIQUE约束保证
 * </p>
 * <p>
 * 设计说明:
 *   1. 采用DO(数据对象)与领域对象(record)分离模式，通过转换方法实现层间数据映射
 *   2. 所有操作均带租户隔离(tenantId)，确保多租户数据安全
 *   3. save方法采用upsert模式(存在则更新，不存在则插入)
 * </p>
 *
 * @author ERP系统
 * @see DashboardMapper
 * @see DashboardMetric
 */
@Repository
public class DashboardStore {

    private final DashboardMapper mapper;

    /**
     * 构造函数 - 依赖注入仪表盘映射器
     *
     * @param mapper MyBatis仪表盘映射接口
     */
    public DashboardStore(DashboardMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存指标(存在则更新，不存在则插入)
     * <p>
     * 以metricId为主键判断指标是否已存在，
     * 已存在则更新指标编码、名称、值、单位和更新时间，
     * 不存在则插入新指标记录。
     * </p>
     *
     * @param metric 指标领域对象
     * @return 保存后的指标领域对象
     */
    public DashboardMetric save(DashboardMetric metric) {
        DashboardMetricDO existing = mapper.selectDashboardMetric(metric.tenantId(), metric.metricId());
        DashboardMetricDO data = toData(metric);
        if (existing == null) {
            mapper.insertDashboardMetric(data);
        } else {
            mapper.updateDashboardMetric(data);
        }
        return metric;
    }

    /**
     * 按ID查询指标
     * <p>
     * 同时校验租户ID，确保多租户数据隔离
     * </p>
     *
     * @param tenantId 租户ID
     * @param metricId 指标ID
     * @return 指标实体(可能为空)
     */
    public Optional<DashboardMetric> find(String tenantId, String metricId) {
        return Optional.ofNullable(mapper.selectDashboardMetric(tenantId, metricId))
                .map(this::toDomain);
    }

    /**
     * 按编码查询指标
     * <p>
     * 在租户范围内按metricCode精确匹配，metricCode在租户内唯一
     * </p>
     *
     * @param tenantId   租户ID
     * @param metricCode 指标编码
     * @return 指标实体(可能为空)
     */
    public Optional<DashboardMetric> findByCode(String tenantId, String metricCode) {
        return Optional.ofNullable(mapper.selectDashboardMetricByCode(tenantId, metricCode))
                .map(this::toDomain);
    }

    /**
     * 查询租户下所有指标
     *
     * @param tenantId 租户ID
     * @return 指标列表
     */
    public List<DashboardMetric> list(String tenantId) {
        return mapper.selectDashboardMetrics(tenantId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 删除指标
     * <p>
     * 从数据库中物理删除指定指标记录
     * </p>
     *
     * @param tenantId 租户ID
     * @param metricId 指标ID
     */
    public void delete(String tenantId, String metricId) {
        mapper.deleteDashboardMetric(tenantId, metricId);
    }

    /**
     * 领域对象转换为数据对象
     *
     * @param metric 指标领域对象
     * @return 指标数据对象
     */
    private DashboardMetricDO toData(DashboardMetric metric) {
        DashboardMetricDO data = new DashboardMetricDO();
        data.setMetricId(metric.metricId());
        data.setTenantId(metric.tenantId());
        data.setMetricCode(metric.metricCode());
        data.setMetricName(metric.metricName());
        data.setMetricValue(metric.metricValue());
        data.setUnit(metric.unit());
        data.setUpdatedAt(metric.updatedAt() != null ? metric.updatedAt() : Instant.now());
        return data;
    }

    /**
     * 数据对象转换为领域对象
     *
     * @param data 指标数据对象
     * @return 指标领域对象
     */
    private DashboardMetric toDomain(DashboardMetricDO data) {
        return new DashboardMetric(
                data.getMetricId(), data.getTenantId(), data.getMetricCode(),
                data.getMetricName(), data.getMetricValue(), data.getUnit(), data.getUpdatedAt());
    }
}
