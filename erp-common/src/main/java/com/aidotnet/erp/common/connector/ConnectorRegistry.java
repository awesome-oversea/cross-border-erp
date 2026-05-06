package com.aidotnet.erp.common.connector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConnectorRegistry {

    private static final Logger log = LoggerFactory.getLogger(ConnectorRegistry.class);
    private final Map<String, PlatformConnector> connectors = new HashMap<>();

    public void register(PlatformConnector connector) {
        connectors.put(connector.getPlatformCode(), connector);
        log.info("Registered platform connector: code={}, name={}", connector.getPlatformCode(), connector.getPlatformName());
    }

    public Optional<PlatformConnector> getConnector(String platformCode) {
        return Optional.ofNullable(connectors.get(platformCode));
    }

    public List<String> getAvailablePlatforms() {
        return connectors.keySet().stream().sorted().toList();
    }

    public boolean isSupported(String platformCode) {
        return connectors.containsKey(platformCode);
    }
}
