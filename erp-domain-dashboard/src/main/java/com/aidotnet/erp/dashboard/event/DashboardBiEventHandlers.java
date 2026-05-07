package com.aidotnet.erp.dashboard.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.common.event.StandardDomainEvent;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.dashboard.domain.AIInsightCard;
import com.aidotnet.erp.dashboard.infrastructure.DashboardRepository;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * BI -> Dashboard 洞察卡片事件处理器。
 */
@Component
public class DashboardBiEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(DashboardBiEventHandlers.class);

    private final DomainEventDispatcher dispatcher;
    private final DashboardRepository dashboardRepository;

    public DashboardBiEventHandlers(DomainEventDispatcher dispatcher, DashboardRepository dashboardRepository) {
        this.dispatcher = dispatcher;
        this.dashboardRepository = dashboardRepository;
    }

    @PostConstruct
    public void register() {
        dispatcher.register("erp.bi.insight.card.created", this::handleBiInsightCardCreated);
        log.info("[DASHBOARD] BI insight event handlers registered: 1 event type");
    }

    @SuppressWarnings("unchecked")
    private void handleBiInsightCardCreated(DomainEvent event) {
        String tenantId = event.tenantId();
        Map<String, Object> payload = event instanceof StandardDomainEvent standardEvent
                ? standardEvent.payload()
                : Map.of();
        String cardId = stringValue(payload.get("cardId"));
        String userId = stringValue(payload.get("userId"));
        String title = stringValue(payload.get("title"));
        String insightType = stringValue(payload.get("insightType"));
        String severity = stringValue(payload.get("severity"));
        if (!hasText(cardId) || !hasText(userId) || !hasText(title) || !hasText(insightType) || !hasText(severity)) {
            log.warn("[DASHBOARD] Ignore invalid BI insight card event: tenant={}, payload={}", tenantId, payload);
            return;
        }

        inTenant(tenantId, () -> {
            if (dashboardRepository.findAIInsightCard(tenantId, cardId).isPresent()) {
                return null;
            }
            Map<String, Object> data = payload.get("data") instanceof Map<?, ?> rawData
                    ? new LinkedHashMap<>((Map<String, Object>) rawData)
                    : Map.of();
            Instant validUntil = payload.get("validUntil") instanceof Instant instant ? instant : null;
            AIInsightCard card = new AIInsightCard(
                    cardId,
                    tenantId,
                    userId,
                    title,
                    stringValue(payload.get("summary")),
                    insightType,
                    severity,
                    data,
                    hasText(stringValue(payload.get("sourceDomain"))) ? stringValue(payload.get("sourceDomain")) : "BI",
                    stringValue(payload.get("suggestion")),
                    stringValue(payload.get("actionUrl")),
                    false,
                    false,
                    validUntil,
                    event.occurredAt(),
                    event.occurredAt());
            dashboardRepository.saveAIInsightCard(card);
            return null;
        });
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <T> T inTenant(String tenantId, Supplier<T> action) {
        String previousTenantId = TenantContext.getTenantId();
        TenantContext.setTenantId(tenantId);
        try {
            return action.get();
        } finally {
            TenantContext.setTenantId(previousTenantId);
        }
    }
}
