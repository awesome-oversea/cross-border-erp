package com.aidotnet.erp.common.connector;

import com.aidotnet.erp.common.api.Result;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/v1/connector")
public class ConnectorManagerController {

    private static final Logger log = LoggerFactory.getLogger(ConnectorManagerController.class);
    private final ConnectorRegistry connectorRegistry;

    public ConnectorManagerController(ConnectorRegistry connectorRegistry) {
        this.connectorRegistry = connectorRegistry;
    }

    @GetMapping("/platforms")
    public Result<List<String>> getPlatformConnectors() {
        return Result.ok(connectorRegistry.getAvailablePlatforms());
    }

    @PostMapping("/platforms/{type}/register")
    public Result<Void> registerPlatform(@PathVariable String type, @RequestBody Map<String, String> config) {
        log.info("Registering platform connector: type={}", type);
        return Result.ok(null);
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("platforms", connectorRegistry.getAvailablePlatforms());
        health.put("status", "UP");
        return Result.ok(health);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("registeredPlatforms", connectorRegistry.getAvailablePlatforms().size());
        stats.put("totalCalls", 0);
        stats.put("successRate", 0.0);
        return Result.ok(stats);
    }
}
