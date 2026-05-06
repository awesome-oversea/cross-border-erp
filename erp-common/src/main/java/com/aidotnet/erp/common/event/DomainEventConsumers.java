package com.aidotnet.erp.common.event;

import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainEventConsumers {

    private static final Logger log = LoggerFactory.getLogger(DomainEventConsumers.class);
    private final DomainEventDispatcher dispatcher;

    public DomainEventConsumers(DomainEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Bean
    public Consumer<Map<String, Object>> omsEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[OMS Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> wmsEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[WMS Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> scmEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[SCM Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> tmsEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[TMS Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> fmsEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[FMS Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> adsEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[ADS Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> crmEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[CRM Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> fbaEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[FBA Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> pdmEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[PDM Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> dashboardEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[DASHBOARD Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> biEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[BI Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> iamEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[IAM Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> somEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[SOM Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }

    @Bean
    public Consumer<Map<String, Object>> sysEvents() {
        return message -> {
            String eventType = (String) message.get("eventType");
            log.info("[SYS Consumer] type={}, tenant={}", eventType, message.get("tenantId"));
            dispatcher.dispatchRaw(eventType, message);
        };
    }
}
