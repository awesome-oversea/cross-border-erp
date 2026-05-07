package com.aidotnet.erp.common.risk;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/fms/api/v1")
public class ComplianceRiskController {

    private final ComplianceRiskService complianceRiskService;

    public ComplianceRiskController(ComplianceRiskService complianceRiskService) {
        this.complianceRiskService = complianceRiskService;
    }

    @PostMapping("/compliance/check")
    public Result<ComplianceRiskService.ComplianceCheckResult> checkCompliance(@RequestBody Map<String, Object> request) {
        return Result.ok(complianceRiskService.check(
                (String) request.get("category"),
                (String) request.get("content"),
                request
        ));
    }

    @GetMapping("/compliance/rules")
    public Result<Map<String, Object>> getComplianceRules() {
        return Result.ok(Map.of());
    }

    @PostMapping("/risk/assess")
    public Result<ComplianceRiskService.RiskAssessmentResult> assessRisk(@RequestBody Map<String, Object> request) {
        return Result.ok(complianceRiskService.assessRisk(
                (String) request.get("entityType"),
                (String) request.get("entityId"),
                request
        ));
    }
}
