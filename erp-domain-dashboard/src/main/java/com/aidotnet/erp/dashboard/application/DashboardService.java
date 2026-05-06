package com.aidotnet.erp.dashboard.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.dashboard.domain.DashboardMetric;
import com.aidotnet.erp.dashboard.infrastructure.DashboardStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 仪表盘指标应用服务
 * <p>
 * 描述: 工作台域核心服务，负责仪表盘指标的增删改查业务逻辑。
 *       指标数据来源于各业务域的聚合计算结果，为AI看板提供数据支撑。
 * </p>
 * <p>
 * 核心能力:
 *   1. 指标创建 - 校验编码唯一性后创建指标
 *   2. 指标更新 - 支持部分字段更新，自动刷新时间戳
 *   3. 指标删除 - 逻辑删除，保留历史数据
 *   4. 指标查询 - 支持按租户列表查询和按编码精确查询
 * </p>
 * <p>
 * 业务规则:
 *   1. 指标编码(metricCode)在同一租户下唯一，重复编码抛出METRIC_DUPLICATED异常
 *   2. 更新指标时需校验编码唯一性(排除自身)
 *   3. 删除指标时校验指标存在性
 * </p>
 *
 * @author ERP系统
 */
@Service
public class DashboardService {

    private final DashboardStore store;

    /**
     * 构造函数 - 依赖注入仪表盘存储层
     *
     * @param store 仪表盘数据存储
     */
    public DashboardService(DashboardStore store) {
        this.store = store;
    }

    /**
     * 创建指标
     * <p>
     * 校验指标编码在租户内唯一性后创建新指标。
     * </p>
     *
     * @param tenantId 租户ID
     * @param command  创建指标命令
     * @return 新创建的指标实体
     * @throws BizException METRIC_DUPLICATED - 指标编码已存在
     */
    @Transactional
    public DashboardMetric create(String tenantId, SaveMetricCommand command) {
        store.findByCode(tenantId, command.metricCode()).ifPresent(existing -> {
            throw new BizException("METRIC_DUPLICATED", "指标编码已存在");
        });
        DashboardMetric metric = new DashboardMetric(
                UUID.randomUUID().toString(), tenantId, command.metricCode(),
                command.metricName(), command.metricValue(), command.unit(), Instant.now());
        return store.save(metric);
    }

    /**
     * 更新指标
     * <p>
     * 更新指定指标的编码、名称、值和单位，自动刷新更新时间。
     * 校验新编码不与其他指标冲突(排除自身)。
     * </p>
     *
     * @param tenantId 租户ID
     * @param metricId 指标ID
     * @param command  更新指标命令
     * @return 更新后的指标实体
     * @throws BizException METRIC_NOT_FOUND - 指标不存在
     * @throws BizException METRIC_DUPLICATED - 新编码与其他指标冲突
     */
    @Transactional
    public DashboardMetric update(String tenantId, String metricId, SaveMetricCommand command) {
        DashboardMetric metric = store.find(tenantId, metricId)
                .orElseThrow(() -> new BizException("METRIC_NOT_FOUND", "指标不存在"));
        store.findByCode(tenantId, command.metricCode())
                .filter(existing -> !existing.metricId().equals(metricId))
                .ifPresent(existing -> {
                    throw new BizException("METRIC_DUPLICATED", "指标编码已存在");
                });
        DashboardMetric updated = new DashboardMetric(
                metric.metricId(), metric.tenantId(), command.metricCode(), command.metricName(),
                command.metricValue(), command.unit(), Instant.now());
        return store.save(updated);
    }

    /**
     * 删除指标
     * <p>
     * 逻辑删除指定指标，校验指标存在性。
     * </p>
     *
     * @param tenantId 租户ID
     * @param metricId 指标ID
     * @throws BizException METRIC_NOT_FOUND - 指标不存在
     */
    @Transactional
    public void delete(String tenantId, String metricId) {
        DashboardMetric metric = store.find(tenantId, metricId)
                .orElseThrow(() -> new BizException("METRIC_NOT_FOUND", "指标不存在"));
        store.delete(metric.tenantId(), metric.metricId());
    }

    /**
     * 查询租户下所有指标
     *
     * @param tenantId 租户ID
     * @return 指标列表
     */
    public List<DashboardMetric> list(String tenantId) {
        return store.list(tenantId);
    }

    /**
     * 按编码查询指标
     *
     * @param tenantId   租户ID
     * @param metricCode 指标编码
     * @return 指标实体
     * @throws BizException METRIC_NOT_FOUND - 指标不存在
     */
    public DashboardMetric getByCode(String tenantId, String metricCode) {
        return store.findByCode(tenantId, metricCode)
                .orElseThrow(() -> new BizException("METRIC_NOT_FOUND", "指标不存在"));
    }

    /**
     * 根据ID查询指标
     *
     * @param tenantId 租户ID
     * @param metricId 指标ID
     * @return 指标实体
     * @throws BizException METRIC_NOT_FOUND - 指标不存在
     */
    public DashboardMetric getById(String tenantId, String metricId) {
        return store.find(tenantId, metricId)
                .orElseThrow(() -> new BizException("METRIC_NOT_FOUND", "指标不存在"));
    }

    /**
     * 创建/更新指标命令
     *
     * @param metricCode  指标编码
     * @param metricName  指标名称
     * @param metricValue 指标值
     * @param unit        指标单位
     */
    public record SaveMetricCommand(String metricCode, String metricName, BigDecimal metricValue, String unit) {}
}
