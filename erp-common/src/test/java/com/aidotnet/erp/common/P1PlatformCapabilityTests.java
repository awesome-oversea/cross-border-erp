package com.aidotnet.erp.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aidotnet.erp.common.approval.ApprovalService;
import com.aidotnet.erp.common.approval.ApprovalStatus;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.idempotency.IdempotencyService;
import com.aidotnet.erp.common.integration.IntegrationRegistry;
import com.aidotnet.erp.common.job.InMemoryJobScheduler;
import com.aidotnet.erp.common.notify.MockNotificationService;
import com.aidotnet.erp.common.ratelimit.InMemoryRateLimiter;
import com.aidotnet.erp.common.security.DataMasker;
import com.aidotnet.erp.common.translation.MockTranslationService;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class P1PlatformCapabilityTests {

    @Test
    void idempotencyReturnsStoredResultAndRejectsConflict() {
        IdempotencyService service = new IdempotencyService(Duration.ofMinutes(1));
        AtomicInteger counter = new AtomicInteger();

        String first = service.execute("tenant-a", "key-1", "body-a", () -> "created-" + counter.incrementAndGet());
        String second = service.execute("tenant-a", "key-1", "body-a", () -> "created-" + counter.incrementAndGet());

        assertThat(first).isEqualTo("created-1");
        assertThat(second).isEqualTo(first);
        assertThat(counter).hasValue(1);
        assertThatThrownBy(() -> service.execute("tenant-a", "key-1", "body-b", () -> "bad"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("different request");
    }

    @Test
    void rateLimiterRejectsExcessiveRequests() {
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(2, Duration.ofMinutes(1));
        limiter.check("api", "actor-1");
        limiter.check("api", "actor-1");

        assertThatThrownBy(() -> limiter.check("api", "actor-1"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("rate limit");
    }

    @Test
    void approvalWorkflowRecordsHistoryAndRejectsIllegalTransition() {
        ApprovalService service = new ApprovalService();
        var instance = service.start("tenant-a", "PURCHASE_ORDER", "po-1", "buyer");
        var approved = service.approve("tenant-a", instance.approvalId(), "manager", "ok");

        assertThat(approved.status()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(approved.history()).hasSize(2);
        assertThatThrownBy(() -> service.reject("tenant-a", instance.approvalId(), "manager", "late"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("not pending");
    }

    @Test
    void scheduledJobCanBeTriggeredAndDisabled() {
        InMemoryJobScheduler scheduler = new InMemoryJobScheduler();
        AtomicInteger counter = new AtomicInteger();
        scheduler.register("sync-orders", "0 */5 * * * ?");

        var log = scheduler.trigger("sync-orders", counter::incrementAndGet);
        scheduler.disable("sync-orders");

        assertThat(log.success()).isTrue();
        assertThat(counter).hasValue(1);
        assertThat(scheduler.logs()).hasSize(1);
        assertThatThrownBy(() -> scheduler.trigger("sync-orders", counter::incrementAndGet))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void notificationAndTranslationUseMockRecordsAndGlossary() {
        MockNotificationService notificationService = new MockNotificationService();
        var sent = notificationService.send("tenant-a", "WEBHOOK", "ops", "Order ${orderNo} failed",
                Map.of("orderNo", "A001"));

        MockTranslationService translationService = new MockTranslationService(Map.of("wireless mouse", "无线鼠标"));
        var translated = translationService.translate("tenant-a", "en", "zh-CN", "wireless mouse");

        assertThat(sent.content()).isEqualTo("Order A001 failed");
        assertThat(notificationService.records()).hasSize(1);
        assertThat(translated.translatedText()).isEqualTo("无线鼠标");
        assertThat(translated.glossaryHit()).isTrue();
    }

    @Test
    void dataMaskerMasksSensitiveFields() {
        assertThat(DataMasker.email("buyer@example.com")).isEqualTo("b***@example.com");
        assertThat(DataMasker.phone("13812345678")).isEqualTo("138****5678");
    }

    @Test
    void integrationRegistryRegistersConnectorAndAuthenticatesApiClient() {
        IntegrationRegistry registry = new IntegrationRegistry();
        var connector = registry.registerConnector("tenant-a", "amazon", "Amazon SP-API");
        var client = registry.createApiClient("tenant-a", "pms-service");

        registry.authenticate("tenant-a", "pms-service", client.plainSecretOnce());
        var disabled = registry.disableConnector("tenant-a", "amazon");

        assertThat(connector.enabled()).isTrue();
        assertThat(disabled.enabled()).isFalse();
        assertThat(client.secretHash()).isNotEqualTo(client.plainSecretOnce());
        assertThatThrownBy(() -> registry.authenticate("tenant-a", "pms-service", "wrong"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("authentication failed");
    }
}
