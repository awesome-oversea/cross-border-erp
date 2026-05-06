package com.aidotnet.erp.common.message;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "erp.messaging")
public class PlatformMessageProperties {

    private String defaultBinding = "erpKafkaOutbound-out-0";
    private List<MessageRoute> routes = new ArrayList<>();

    public String getDefaultBinding() {
        return defaultBinding;
    }

    public void setDefaultBinding(String defaultBinding) {
        this.defaultBinding = defaultBinding;
    }

    public List<MessageRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(List<MessageRoute> routes) {
        this.routes = routes;
    }

    public String bindingFor(String eventType) {
        if (defaultBinding == null || defaultBinding.isBlank()) {
            throw new IllegalStateException("erp.messaging.default-binding must not be blank");
        }
        return routes.stream()
                .filter(route -> route.matches(eventType))
                .findFirst()
                .map(MessageRoute::bindingName)
                .orElse(defaultBinding.trim());
    }
}
