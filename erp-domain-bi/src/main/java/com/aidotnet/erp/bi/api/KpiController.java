package com.aidotnet.erp.bi.api;

import com.aidotnet.erp.bi.application.KpiAssessmentService;
import com.aidotnet.erp.bi.domain.KpiAssessment;
import com.aidotnet.erp.bi.domain.KpiTarget;
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
 * KPI目标与考核控制器
 * <p>
 * 路径规范: /bi/api/in/v1/kpi 与 /bi/api/in/v1/kpis
 * 提供 KPI 目标设定、目标调整、考核评估、考核明细查询、部门评分等 REST API。
 * 保留原有 /kpi 路径，并补充 /kpis 复数路径，便于与 BI 域其他资源命名保持一致。
 * </p>
 */
@RestController
@RequestMapping({"/bi/api/in/v1/kpi", "/bi/api/in/v1/kpis"})
public class KpiController {

    private final KpiAssessmentService kpiAssessmentService;

    public KpiController(KpiAssessmentService kpiAssessmentService) {
        this.kpiAssessmentService = kpiAssessmentService;
    }

    @PostMapping("/targets")
    public Result<KpiTarget> createTarget(@RequestBody CreateTargetRequest request) {
        return Result.ok(kpiAssessmentService.createTarget(currentTenant(),
                request.kpiCode(), request.kpiName(), request.department(), request.role(),
                request.period(), request.targetValue(), request.warningValue(),
                request.excellentValue(), request.unit(), request.metricCode(),
                request.caliberId(), request.applicableRoles(), request.scoringRule(), request.weight()));
    }

    @PatchMapping("/targets/{targetId}")
    public Result<KpiTarget> updateTarget(@PathVariable String targetId,
                                          @RequestBody UpdateTargetRequest request) {
        return Result.ok(kpiAssessmentService.updateTarget(currentTenant(), targetId,
                request.targetValue(), request.warningValue(), request.excellentValue(),
                request.scoringRule(), request.weight(), request.enabled()));
    }

    @GetMapping("/targets")
    public Result<List<KpiTarget>> listTargets(@RequestParam(required = false) String department,
                                               @RequestParam(required = false) String period) {
        return Result.ok(kpiAssessmentService.listTargets(currentTenant(), department, period));
    }

    @GetMapping("/targets/{targetId}")
    public Result<KpiTarget> getTarget(@PathVariable String targetId) {
        return Result.ok(kpiAssessmentService.getTarget(currentTenant(), targetId));
    }

    @PostMapping("/targets/{targetId}/assess")
    public Result<KpiAssessment> assess(@PathVariable String targetId, @RequestBody AssessRequest request) {
        return Result.ok(kpiAssessmentService.assess(currentTenant(), targetId,
                request.userId(), request.actualValue(), request.assessorId(), request.comment()));
    }

    @GetMapping("/targets/{targetId}/assessments")
    public Result<List<KpiAssessment>> listTargetAssessments(@PathVariable String targetId) {
        return Result.ok(kpiAssessmentService.listAssessmentsByTarget(currentTenant(), targetId));
    }

    @GetMapping("/assessments")
    public Result<List<KpiAssessment>> listAssessments(@RequestParam(required = false) String userId,
                                                       @RequestParam(required = false) String period) {
        return Result.ok(kpiAssessmentService.listAssessments(currentTenant(), userId, period));
    }

    @GetMapping("/assessments/{assessmentId}")
    public Result<KpiAssessment> getAssessment(@PathVariable String assessmentId) {
        return Result.ok(kpiAssessmentService.getAssessment(currentTenant(), assessmentId));
    }

    @GetMapping("/departments/{department}/score")
    public Result<BigDecimal> departmentScore(@PathVariable String department,
                                              @RequestParam String period) {
        return Result.ok(kpiAssessmentService.calculateDepartmentScore(currentTenant(), department, period));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateTargetRequest(String kpiCode, String kpiName, String department, String role,
                                      String period, BigDecimal targetValue, BigDecimal warningValue,
                                      BigDecimal excellentValue, String unit, String metricCode,
                                      String caliberId, List<String> applicableRoles,
                                      String scoringRule, BigDecimal weight) {}

    public record UpdateTargetRequest(BigDecimal targetValue, BigDecimal warningValue,
                                      BigDecimal excellentValue, String scoringRule,
                                      BigDecimal weight, Boolean enabled) {}

    public record AssessRequest(String userId, BigDecimal actualValue, String assessorId, String comment) {}
}
