package com.aidotnet.erp.bi.api;

import com.aidotnet.erp.common.api.Result;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bi/api/out/v1")
public class BiOutboundController {

    @GetMapping("/reports/{reportCode}/data")
    public Result<Map<String, Object>> exportReportData(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String reportCode,
            @RequestParam(required = false) String period) {
        return Result.ok(Map.of("reportCode", reportCode, "period", period, "exportedAt", Instant.now().toString()));
    }

    @GetMapping("/kpis")
    public Result<List<Map<String, Object>>> exportKpis(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String category) {
        return Result.ok(List.of());
    }

    @GetMapping("/metrics")
    public Result<List<Map<String, Object>>> exportMetrics(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String category) {
        return Result.ok(List.of());
    }

    @GetMapping("/dashboards/{dashboardId}")
    public Result<Map<String, Object>> exportDashboard(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String dashboardId) {
        return Result.ok(Map.of("dashboardId", dashboardId, "exportedAt", Instant.now().toString()));
    }

    @GetMapping("/alerts")
    public Result<List<Map<String, Object>>> exportAlerts(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String alertType) {
        return Result.ok(List.of());
    }
}
