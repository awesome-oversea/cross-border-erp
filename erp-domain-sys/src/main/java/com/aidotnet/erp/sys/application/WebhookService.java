package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.WebhookDelivery;
import com.aidotnet.erp.sys.domain.WebhookEndpoint;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Webhook管理应用服务
 * <p>
 * 描述: 系统设置域Webhook中心服务，负责Webhook端点的注册/更新/删除、
 *       事件投递/重试、投递日志记录等业务逻辑。确保事件可靠投递到外部系统。
 * </p>
 * <p>
 * 核心能力:
 *   1. 端点管理 - 注册/更新/删除Webhook端点，支持事件订阅过滤
 *   2. 事件投递 - 按订阅关系投递事件到端点，HMAC签名验证
 *   3. 重试机制 - 投递失败自动重试，指数退避策略(最多3次)
 *   4. 投递日志 - 记录每次投递的请求/响应/状态码，确保可追溯
 * </p>
 *
 * @author ERP系统
 * @see WebhookEndpoint
 */
@Service
public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long[] RETRY_DELAYS_MS = {1000, 5000, 30000};

    private final SysExtStore extStore;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WebhookService(SysExtStore extStore) {
        this.extStore = extStore;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public WebhookEndpoint createEndpoint(String tenantId, CreateWebhookCommand command) {
        validateUrl(command.url());
        Instant now = Instant.now();
        WebhookEndpoint endpoint = new WebhookEndpoint(
                UUID.randomUUID().toString(), tenantId, command.name(), command.url(),
                command.eventType(), command.headers(), generateSecret(), true,
                command.retryCount() > 0 ? command.retryCount() : 3,
                command.timeoutSeconds() > 0 ? command.timeoutSeconds() : 30,
                command.subscribedEvents(), now, now);
        return extStore.saveWebhookEndpoint(endpoint);
    }

    @Transactional
    public WebhookEndpoint updateEndpoint(String tenantId, String endpointId, UpdateWebhookCommand command) {
        WebhookEndpoint existing = getEndpoint(tenantId, endpointId);
        Instant now = Instant.now();
        WebhookEndpoint updated = new WebhookEndpoint(
                existing.endpointId(), tenantId,
                command.name() != null ? command.name() : existing.name(),
                command.url() != null ? command.url() : existing.url(),
                command.eventType() != null ? command.eventType() : existing.eventType(),
                command.headers() != null ? command.headers() : existing.headers(),
                existing.secret(),
                command.active() != null ? command.active() : existing.active(),
                existing.retryCount(), existing.timeoutSeconds(),
                command.subscribedEvents() != null ? command.subscribedEvents() : existing.subscribedEvents(),
                existing.createdAt(), now);
        return extStore.saveWebhookEndpoint(updated);
    }

    @Transactional
    public void deleteEndpoint(String tenantId, String endpointId) {
        WebhookEndpoint endpoint = getEndpoint(tenantId, endpointId);
        extStore.deleteWebhookEndpoint(tenantId, endpoint.endpointId());
    }

    public WebhookEndpoint getEndpoint(String tenantId, String endpointId) {
        return extStore.findWebhookEndpoint(tenantId, endpointId)
                .orElseThrow(() -> new BizException("WEBHOOK_NOT_FOUND", "Webhook端点不存在"));
    }

    public List<WebhookEndpoint> listEndpoints(String tenantId) {
        return extStore.listWebhookEndpoints(tenantId);
    }

    @Transactional
    public List<WebhookDelivery> deliver(String tenantId, String eventType, Map<String, Object> payload) {
        List<WebhookEndpoint> endpoints = extStore.listWebhookEndpoints(tenantId).stream()
                .filter(e -> e.active())
                .filter(e -> e.subscribedEvents().contains(eventType) || e.subscribedEvents().contains("*"))
                .toList();
        List<WebhookDelivery> deliveries = new ArrayList<>();
        for (WebhookEndpoint endpoint : endpoints) {
            deliveries.add(deliverToEndpoint(tenantId, endpoint, eventType, payload));
        }
        return deliveries;
    }

    public List<WebhookDelivery> listDeliveries(String tenantId, String endpointId) {
        return extStore.listWebhookDeliveries(tenantId, endpointId);
    }

    @Transactional
    public WebhookDelivery redeliver(String tenantId, String deliveryId) {
        WebhookDelivery delivery = extStore.findWebhookDelivery(tenantId, deliveryId)
                .orElseThrow(() -> new BizException("DELIVERY_NOT_FOUND", "投递记录不存在"));
        WebhookEndpoint endpoint = getEndpoint(tenantId, delivery.endpointId());
        return deliverToEndpoint(tenantId, endpoint, delivery.eventType(), delivery.payload());
    }

    private WebhookDelivery deliverToEndpoint(String tenantId, WebhookEndpoint endpoint,
                                               String eventType, Map<String, Object> payload) {
        int attemptCount = 0;
        int statusCode = 0;
        String responseBody = null;
        boolean success = false;

        while (attemptCount < MAX_RETRY_ATTEMPTS && !success) {
            attemptCount++;
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint.url()))
                        .header("Content-Type", "application/json")
                        .header("X-Webhook-Event", eventType)
                        .header("X-Webhook-Signature", computeSignature(payload, endpoint.secret()))
                        .POST(HttpRequest.BodyPublishers.ofString(
                                objectMapper.writeValueAsString(payload)))
                        .timeout(Duration.ofSeconds(endpoint.timeoutSeconds()));

                if (endpoint.headers() != null) {
                    endpoint.headers().forEach(builder::header);
                }

                HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                statusCode = response.statusCode();
                responseBody = response.body();
                success = statusCode >= 200 && statusCode < 300;

                if (!success && attemptCount < MAX_RETRY_ATTEMPTS) {
                    log.warn("Webhook delivery failed, retrying: endpoint={} status={} attempt={}/{}",
                            endpoint.endpointId(), statusCode, attemptCount, MAX_RETRY_ATTEMPTS);
                    Thread.sleep(RETRY_DELAYS_MS[attemptCount - 1]);
                }
            } catch (BizException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Webhook delivery error: endpoint={} attempt={}/{} error={}",
                        endpoint.endpointId(), attemptCount, MAX_RETRY_ATTEMPTS, e.getMessage());
                if (attemptCount >= MAX_RETRY_ATTEMPTS) {
                    responseBody = e.getMessage();
                }
            }
        }

        WebhookDelivery delivery = new WebhookDelivery(
                UUID.randomUUID().toString(), tenantId, endpoint.endpointId(), eventType,
                payload, statusCode, responseBody, success, attemptCount, null, Instant.now());
        return extStore.saveWebhookDelivery(delivery);
    }

    private String generateSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String computeSignature(Map<String, Object> payload, String secret) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256");
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(json.getBytes());
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BizException("WEBHOOK_URL_INVALID", "URL不能为空");
        }
        if (!url.startsWith("https://")) {
            throw new BizException("WEBHOOK_URL_INVALID", "URL必须使用HTTPS");
        }
    }

    public record CreateWebhookCommand(
            String name, String url, String eventType, Map<String, String> headers,
            int retryCount, int timeoutSeconds, List<String> subscribedEvents) {}

    public record UpdateWebhookCommand(
            String name, String url, String eventType, Map<String, String> headers,
            Boolean active, List<String> subscribedEvents) {}
}
