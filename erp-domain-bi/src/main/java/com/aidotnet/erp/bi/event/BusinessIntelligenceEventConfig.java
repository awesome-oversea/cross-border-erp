package com.aidotnet.erp.bi.event;

import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Profile("bi-event-tap")
public class BusinessIntelligenceEventConfig {

    private static final Logger log = LoggerFactory.getLogger(BusinessIntelligenceEventConfig.class);

    @Bean
    public Consumer<Map<String, Object>> omsEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            String tenantId = (String) message.get("tenantId");
            log.info("BI received OMS event: type={}, tenant={}", eventType, tenantId);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> fmsEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            String tenantId = (String) message.get("tenantId");
            log.info("BI received FMS event: type={}, tenant={}", eventType, tenantId);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> adsEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            String tenantId = (String) message.get("tenantId");
            log.info("BI received ADS event: type={}, tenant={}", eventType, tenantId);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> wmsEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            String tenantId = (String) message.get("tenantId");
            log.info("BI received WMS event: type={}, tenant={}", eventType, tenantId);
        };
    }
}
