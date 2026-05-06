package com.aidotnet.erp.dashboard.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.dashboard.application.DashboardService;
import com.aidotnet.erp.dashboard.application.DashboardService.SaveMetricCommand;
import com.aidotnet.erp.dashboard.domain.DashboardMetric;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘指标入站控制器
 * <p>
 * 描述: 提供仪表盘指标的RESTful API，支持指标的增删改查操作。
 *       所有接口需携带租户上下文，实现多租户数据隔离。
 * </p>
 * <p>
 * 接口规范:
 *   - 路径: /dashboard/api/in/v1/metrics
 *   - 认证: 需登录用户，通过Token获取租户ID
 *   - 返回: 统一Result包装，包含code/msg/data
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/dashboard/api/in/v1/metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 构造函数 - 依赖注入仪表盘服务
     *
     * @param dashboardService 仪表盘指标服务
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 创建指标
     *
     * @param request 创建指标请求体
     * @return 新创建的指标
     */
    @PostMapping
    public Result<DashboardMetric> create(@Valid @RequestBody SaveMetricRequest request) {
        return Result.ok(dashboardService.create(currentTenant(), new SaveMetricCommand(
                request.metricCode(), request.metricName(), request.metricValue(), request.unit())));
    }

    /**
     * 更新指标
     *
     * @param metricId 指标ID
     * @param request  更新指标请求体
     * @return 更新后的指标
     */
    @PutMapping("/{metricId}")
    public Result<DashboardMetric> update(@PathVariable String metricId, @Valid @RequestBody SaveMetricRequest request) {
        return Result.ok(dashboardService.update(currentTenant(), metricId, new SaveMetricCommand(
                request.metricCode(), request.metricName(), request.metricValue(), request.unit())));
    }

    /**
     * 删除指标
     *
     * @param metricId 指标ID
     * @return 操作结果
     */
    @DeleteMapping("/{metricId}")
    public Result<Void> delete(@PathVariable String metricId) {
        dashboardService.delete(currentTenant(), metricId);
        return Result.ok(null);
    }

    /**
     * 查询指标列表
     *
     * @return 当前租户下所有指标
     */
    @GetMapping
    public Result<List<DashboardMetric>> list() {
        return Result.ok(dashboardService.list(currentTenant()));
    }

    /**
     * 按编码查询指标
     *
     * @param metricCode 指标编码
     * @return 指标详情
     */
    @GetMapping("/code/{metricCode}")
    public Result<DashboardMetric> getByCode(@PathVariable String metricCode) {
        return Result.ok(dashboardService.getByCode(currentTenant(), metricCode));
    }

    /**
     * 按ID查询指标
     *
     * @param metricId 指标ID
     * @return 指标详情
     */
    @GetMapping("/{metricId}")
    public Result<DashboardMetric> getById(@PathVariable String metricId) {
        return Result.ok(dashboardService.getById(currentTenant(), metricId));
    }

    /**
     * 获取当前租户ID
     * <p>
     * 从TenantContext中获取当前登录用户的租户ID，
     * 若租户ID为空则抛出TENANT_REQUIRED异常。
     * </p>
     *
     * @return 租户ID
     * @throws BizException TENANT_REQUIRED - 租户不能为空
     */
    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    /**
     * 创建/更新指标请求体
     *
     * @param metricCode  指标编码，必填
     * @param metricName  指标名称，必填
     * @param metricValue 指标值，必填
     * @param unit        指标单位，必填
     */
    public record SaveMetricRequest(@NotBlank String metricCode, @NotBlank String metricName,
                                    @NotNull BigDecimal metricValue, @NotBlank String unit) {}
}
