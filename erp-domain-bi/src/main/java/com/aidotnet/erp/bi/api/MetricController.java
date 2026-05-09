package com.aidotnet.erp.bi.api;

import com.aidotnet.erp.bi.application.MetricCaliberService;
import com.aidotnet.erp.bi.domain.MetricCaliber;
import com.aidotnet.erp.bi.domain.MetricCaliberValue;
import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 指标口径控制器
 * <p>
 * 路径规范: /bi/api/in/v1/metrics
 * 提供指标口径定义、口径变更、指标试算、口径值查询等 REST API。
 * BI 作为经营指标主控方，指标口径的创建、变更、启停和试算均通过此接口管理。
 * </p>
 */
@RestController
@RequestMapping({"/bi/api/in/v1/metrics", "/bi/api/v1/metrics"})
public class MetricController {

    private final MetricCaliberService metricCaliberService;

    public MetricController(MetricCaliberService metricCaliberService) {
        this.metricCaliberService = metricCaliberService;
    }

    @PostMapping("/calibers")
    public Result<MetricCaliber> createCaliber(@RequestBody CreateCaliberRequest request) {
        return Result.ok(metricCaliberService.createCaliber(currentTenant(),
                request.metricCode(), request.metricName(), request.category(),
                request.caliberType(), request.formula(), request.formulaDescription(),
                request.numeratorMetric(), request.denominatorMetric(), request.unit(),
                request.dataSource(), request.calculationScope(), request.dimensions(),
                request.excludeConditions(), request.permissionCode(), request.dataLevel(),
                request.description()));
    }

    @PatchMapping("/calibers/{caliberId}")
    public Result<MetricCaliber> updateCaliber(@PathVariable String caliberId,
                                               @RequestBody UpdateCaliberRequest request) {
        return Result.ok(metricCaliberService.updateCaliber(currentTenant(), caliberId,
                request.formula(), request.formulaDescription(), request.numeratorMetric(),
                request.denominatorMetric(), request.unit(), request.dataSource(),
                request.calculationScope(), request.dimensions(), request.excludeConditions(),
                request.description()));
    }

    @GetMapping("/calibers")
    public Result<List<MetricCaliber>> listCalibers(@RequestParam(required = false) String category) {
        return Result.ok(metricCaliberService.listCalibers(currentTenant(), category));
    }

    @GetMapping("/calibers/{metricCode}")
    public Result<MetricCaliber> getCaliber(@PathVariable String metricCode) {
        return Result.ok(metricCaliberService.getCaliberByCode(currentTenant(), metricCode));
    }

    @PostMapping("/calibers/{caliberId}/toggle")
    public Result<MetricCaliber> toggleCaliber(@PathVariable String caliberId, @RequestParam boolean enabled) {
        return Result.ok(metricCaliberService.toggleCaliber(currentTenant(), caliberId, enabled));
    }

    @PostMapping("/calculate")
    public Result<MetricCaliberValue> calculate(@RequestBody CalculateMetricRequest request) {
        return Result.ok(metricCaliberService.calculateMetric(currentTenant(),
                request.metricCode(), request.dimensionKey(), request.dimensionValue(),
                request.numeratorValue(), request.denominatorValue()));
    }

    @GetMapping("/values")
    public Result<List<MetricCaliberValue>> listValues(@RequestParam(required = false) String metricCode) {
        return Result.ok(metricCaliberService.listCaliberValues(currentTenant(), metricCode));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateCaliberRequest(String metricCode, String metricName, String category,
                                       String caliberType, String formula, String formulaDescription,
                                       String numeratorMetric, String denominatorMetric, String unit,
                                       String dataSource, String calculationScope, List<String> dimensions,
                                       List<String> excludeConditions, String permissionCode,
                                       String dataLevel, String description) {}

    public record UpdateCaliberRequest(String formula, String formulaDescription,
                                       String numeratorMetric, String denominatorMetric,
                                       String unit, String dataSource, String calculationScope,
                                       List<String> dimensions, List<String> excludeConditions,
                                       String description) {}

    public record CalculateMetricRequest(String metricCode, String dimensionKey, String dimensionValue,
                                         BigDecimal numeratorValue, BigDecimal denominatorValue) {}
}
