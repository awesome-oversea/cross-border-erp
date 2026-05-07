package com.aidotnet.erp.bi.api;

import com.aidotnet.erp.bi.application.BiOperationalService;
import com.aidotnet.erp.bi.domain.AlertCenterReport;
import com.aidotnet.erp.bi.domain.FbaShipmentAnalysisReport;
import com.aidotnet.erp.bi.domain.OperationMonitorReport;
import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BI 经营分析控制器。
 * <p>
 * 描述: 聚焦经营侧分析读接口，当前提供 FBA 货件时效、异常和成本分析能力。
 * </p>
 */
@RestController
@RequestMapping({"/bi/api/in/v1", "/bi/api/v1"})
public class BiOperationalController {

    private final BiOperationalService biOperationalService;

    public BiOperationalController(BiOperationalService biOperationalService) {
        this.biOperationalService = biOperationalService;
    }

    @GetMapping("/fba-shipment-analysis")
    public Result<FbaShipmentAnalysisReport> getFbaShipmentAnalysis() {
        return Result.ok(biOperationalService.analyzeFbaShipments(currentTenant()));
    }

    @GetMapping("/operation-monitor")
    public Result<OperationMonitorReport> getOperationMonitor() {
        return Result.ok(biOperationalService.getOperationMonitor(currentTenant()));
    }

    @GetMapping("/alert-center")
    public Result<AlertCenterReport> getAlertCenter(@org.springframework.web.bind.annotation.RequestParam(required = false) String category,
                                                    @org.springframework.web.bind.annotation.RequestParam(required = false) String severity,
                                                    @org.springframework.web.bind.annotation.RequestParam(required = false) String status,
                                                    @org.springframework.web.bind.annotation.RequestParam(required = false) String sourceDomain) {
        return Result.ok(biOperationalService.getAlertCenter(currentTenant(), category, severity, status, sourceDomain));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "Tenant id is required");
        }
        return tenantId;
    }
}
