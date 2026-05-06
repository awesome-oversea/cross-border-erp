package com.aidotnet.erp.common.apimanagement;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/v1/api-management")
public class ApiManagementController {

    private final ApiManagementService apiMgmtService;

    public ApiManagementController(ApiManagementService apiMgmtService) {
        this.apiMgmtService = apiMgmtService;
    }

    @PostMapping("/documents")
    public Result<ApiManagementService.ApiDocument> createDocument(@RequestBody Map<String, Object> request) {
        return Result.ok(apiMgmtService.createDocument(
                (String) request.get("apiName"),
                (String) request.get("basePath"),
                (String) request.getOrDefault("description", ""),
                (String) request.getOrDefault("version", "1.0.0"),
                (String) request.getOrDefault("category", "GENERAL"),
                (String) request.get("author")
        ));
    }

    @PatchMapping("/documents/{docId}")
    public Result<ApiManagementService.ApiDocument> updateDocument(
            @PathVariable String docId, @RequestBody Map<String, Object> request) {
        return Result.ok(apiMgmtService.updateDocument(
                docId,
                (String) request.get("apiName"),
                (String) request.get("basePath"),
                (String) request.get("description"),
                (String) request.get("version"),
                request.containsKey("active") && (Boolean) request.get("active")
        ));
    }

    @GetMapping("/documents")
    public Result<List<ApiManagementService.ApiDocument>> listDocuments(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active) {
        return Result.ok(apiMgmtService.listDocuments(category, active));
    }

    @PostMapping("/documents/{docId}/versions")
    public Result<ApiManagementService.ApiVersion> publishVersion(
            @PathVariable String docId, @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> endpoints = (List<String>) request.getOrDefault("endpoints", List.of());
        return Result.ok(apiMgmtService.publishVersion(
                docId,
                (String) request.get("version"),
                (String) request.getOrDefault("changeLog", ""),
                endpoints,
                (String) request.get("publisher")
        ));
    }

    @PatchMapping("/versions/{verId}/deprecate")
    public Result<ApiManagementService.ApiVersion> deprecateVersion(@PathVariable String verId) {
        return Result.ok(apiMgmtService.deprecateVersion(verId));
    }

    @GetMapping("/versions")
    public Result<List<ApiManagementService.ApiVersion>> listVersions(
            @RequestParam(required = false) String docId) {
        return Result.ok(apiMgmtService.listVersions(docId));
    }

    @PostMapping("/documents/{docId}/test")
    public Result<ApiManagementService.ApiTestResult> executeTest(
            @PathVariable String docId, @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) request.getOrDefault("headers", Map.of());
        return Result.ok(apiMgmtService.executeTest(
                docId,
                (String) request.get("endpoint"),
                (String) request.getOrDefault("method", "GET"),
                headers,
                (String) request.get("requestBody")
        ));
    }

    @GetMapping("/test-results")
    public Result<List<ApiManagementService.ApiTestResult>> listTestResults(
            @RequestParam(required = false) String docId) {
        return Result.ok(apiMgmtService.listTestResults(docId));
    }

    @PostMapping("/traffic/record")
    public Result<ApiManagementService.ApiTrafficStats> recordTraffic(@RequestBody Map<String, Object> request) {
        return Result.ok(apiMgmtService.recordTraffic(
                (String) request.get("docId"),
                (String) request.get("endpoint"),
                (String) request.get("method"),
                (Integer) request.getOrDefault("statusCode", 200),
                ((Number) request.getOrDefault("duration", 0L)).longValue()
        ));
    }

    @GetMapping("/traffic")
    public Result<List<ApiManagementService.ApiTrafficStats>> listTrafficStats(
            @RequestParam(required = false) String docId) {
        return Result.ok(apiMgmtService.listTrafficStats(docId));
    }

    @GetMapping("/traffic/summary")
    public Result<ApiManagementService.ApiTrafficSummary> getTrafficSummary(
            @RequestParam String docId) {
        return Result.ok(apiMgmtService.getTrafficSummary(docId));
    }
}
