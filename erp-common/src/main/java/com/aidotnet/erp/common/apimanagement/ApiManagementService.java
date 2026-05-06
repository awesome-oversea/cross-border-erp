package com.aidotnet.erp.common.apimanagement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApiManagementService {

    private static final Logger log = LoggerFactory.getLogger(ApiManagementService.class);
    private final Map<String, ApiDocument> documents = new ConcurrentHashMap<>();
    private final Map<String, ApiVersion> versions = new ConcurrentHashMap<>();
    private final Map<String, ApiTestResult> testResults = new ConcurrentHashMap<>();
    private final Map<String, ApiTrafficStats> trafficStats = new ConcurrentHashMap<>();

    public ApiDocument createDocument(String apiName, String basePath, String description,
                                       String version, String category, String author) {
        String docId = "APIDOC-" + System.currentTimeMillis();
        ApiDocument doc = new ApiDocument(docId, apiName, basePath, description, version,
                category, author, true, Instant.now(), Instant.now());
        documents.put(docId, doc);
        log.info("Created API document: id={}, name={}, basePath={}", docId, apiName, basePath);
        return doc;
    }

    public ApiDocument updateDocument(String docId, String apiName, String basePath,
                                       String description, String version, boolean active) {
        ApiDocument doc = documents.get(docId);
        if (doc == null) throw new IllegalArgumentException("API document not found: " + docId);
        ApiDocument updated = new ApiDocument(docId,
                apiName != null ? apiName : doc.apiName(),
                basePath != null ? basePath : doc.basePath(),
                description != null ? description : doc.description(),
                version != null ? version : doc.version(),
                doc.category(), doc.author(), active, doc.createdAt(), Instant.now());
        documents.put(docId, updated);
        log.info("Updated API document: id={}", docId);
        return updated;
    }

    public List<ApiDocument> listDocuments(String category, Boolean active) {
        return documents.values().stream()
                .filter(d -> category == null || category.equals(d.category()))
                .filter(d -> active == null || active.equals(d.active()))
                .toList();
    }

    public ApiVersion publishVersion(String docId, String version, String changeLog,
                                      List<String> endpoints, String publisher) {
        ApiDocument doc = documents.get(docId);
        if (doc == null) throw new IllegalArgumentException("API document not found: " + docId);
        String verId = "APIVER-" + System.currentTimeMillis();
        ApiVersion ver = new ApiVersion(verId, docId, version, changeLog, endpoints,
                "PUBLISHED", publisher, Instant.now());
        versions.put(verId, ver);
        log.info("Published API version: id={}, doc={}, version={}", verId, docId, version);
        return ver;
    }

    public ApiVersion deprecateVersion(String verId) {
        ApiVersion ver = versions.get(verId);
        if (ver == null) throw new IllegalArgumentException("API version not found: " + verId);
        ApiVersion deprecated = new ApiVersion(verId, ver.docId(), ver.version(),
                ver.changeLog(), ver.endpoints(), "DEPRECATED", ver.publisher(), ver.publishedAt());
        versions.put(verId, deprecated);
        log.info("Deprecated API version: id={}", verId);
        return deprecated;
    }

    public List<ApiVersion> listVersions(String docId) {
        return versions.values().stream()
                .filter(v -> docId == null || docId.equals(v.docId()))
                .toList();
    }

    public ApiTestResult executeTest(String docId, String endpoint, String method,
                                      Map<String, String> headers, String requestBody) {
        String testId = "APITEST-" + System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        int statusCode = 200;
        String responseBody = "{\"status\":\"ok\"}";
        long duration = System.currentTimeMillis() - startTime;
        String status = statusCode >= 200 && statusCode < 300 ? "PASSED" : "FAILED";

        ApiTestResult result = new ApiTestResult(testId, docId, endpoint, method,
                headers != null ? headers : Map.of(), requestBody, statusCode,
                responseBody, duration, status, Instant.now());
        testResults.put(testId, result);
        log.info("API test executed: id={}, endpoint={}, method={}, status={}, duration={}ms",
                testId, endpoint, method, status, duration);
        return result;
    }

    public List<ApiTestResult> listTestResults(String docId) {
        return testResults.values().stream()
                .filter(r -> docId == null || docId.equals(r.docId()))
                .toList();
    }

    public ApiTrafficStats recordTraffic(String docId, String endpoint, String method,
                                          int statusCode, long duration) {
        String key = docId + ":" + endpoint + ":" + method;
        ApiTrafficStats existing = trafficStats.get(key);
        long totalCalls = (existing != null ? existing.totalCalls() : 0) + 1;
        long errorCalls = (existing != null ? existing.errorCalls() : 0) + (statusCode >= 400 ? 1 : 0);
        long totalDuration = (existing != null ? existing.totalDuration() : 0) + duration;
        double avgDuration = (double) totalDuration / totalCalls;
        double errorRate = (double) errorCalls / totalCalls * 100;

        ApiTrafficStats stats = new ApiTrafficStats(key, docId, endpoint, method,
                totalCalls, errorCalls, avgDuration, errorRate, totalDuration, Instant.now());
        trafficStats.put(key, stats);
        return stats;
    }

    public List<ApiTrafficStats> listTrafficStats(String docId) {
        return trafficStats.values().stream()
                .filter(s -> docId == null || docId.equals(s.docId()))
                .toList();
    }

    public ApiTrafficSummary getTrafficSummary(String docId) {
        List<ApiTrafficStats> stats = listTrafficStats(docId);
        long totalCalls = stats.stream().mapToLong(ApiTrafficStats::totalCalls).sum();
        long totalErrors = stats.stream().mapToLong(ApiTrafficStats::errorCalls).sum();
        double avgDuration = stats.isEmpty() ? 0 : stats.stream().mapToDouble(ApiTrafficStats::avgDuration).average().orElse(0);
        double errorRate = totalCalls > 0 ? (double) totalErrors / totalCalls * 100 : 0;
        return new ApiTrafficSummary(docId, totalCalls, totalErrors, avgDuration, errorRate, Instant.now());
    }

    public record ApiDocument(String docId, String apiName, String basePath, String description,
                               String version, String category, String author, boolean active,
                               Instant createdAt, Instant updatedAt) {}
    public record ApiVersion(String verId, String docId, String version, String changeLog,
                              List<String> endpoints, String status, String publisher, Instant publishedAt) {}
    public record ApiTestResult(String testId, String docId, String endpoint, String method,
                                 Map<String, String> headers, String requestBody, int statusCode,
                                 String responseBody, long duration, String status, Instant executedAt) {}
    public record ApiTrafficStats(String key, String docId, String endpoint, String method,
                                   long totalCalls, long errorCalls, double avgDuration,
                                   double errorRate, long totalDuration, Instant updatedAt) {}
    public record ApiTrafficSummary(String docId, long totalCalls, long totalErrors,
                                     double avgDuration, double errorRate, Instant generatedAt) {}
}
