package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.ConnectorCallLog;
import com.aidotnet.erp.sys.domain.ConnectorConfig;
import com.aidotnet.erp.sys.domain.ConnectorHealthStatus;
import com.aidotnet.erp.sys.domain.ConnectorSecret;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 连接器运维管理应用服务
 * <p>
 * 描述: 系统设置域连接器运维服务，负责第三方连接器的健康检查、
 *       限流管理、连接测试、故障恢复等业务逻辑。确保外部连接稳定可靠。
 * </p>
 *
 * @author ERP系统
 */
@Service
public class ConnectorOpsService {
    private static final Logger log = LoggerFactory.getLogger(ConnectorOpsService.class);
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String ENCRYPTION_KEY = "ErpConnectorOps!@";

    private final SysExtStore extStore;
    private final ConcurrentHashMap<String, ConnectorHealthStatus> healthStore = new ConcurrentHashMap<>();

    public ConnectorOpsService(SysExtStore extStore) {
        this.extStore = extStore;
    }

    public ConnectorHealthStatus checkHealth(String tenantId, String configId) {
        ConnectorConfig config = extStore.findConnectorConfig(tenantId, configId)
                .orElseThrow(() -> new BizException("CONNECTOR_NOT_FOUND", "连接器配置不存在"));
        ConnectorHealthStatus status = healthStore.getOrDefault(configId,
                new ConnectorHealthStatus(configId, tenantId, config.connectorType(), config.platform(),
                        "UNKNOWN", 0, 0, 0.0, 0.0, 0, Instant.now()));
        return status;
    }

    public List<ConnectorHealthStatus> listHealthStatuses(String tenantId) {
        return healthStore.values().stream()
                .filter(h -> h.tenantId().equals(tenantId))
                .toList();
    }

    public ConnectorHealthStatus recordHealth(String tenantId, String configId, boolean success,
                                               long durationMs, String errorMessage) {
        ConnectorHealthStatus prev = healthStore.getOrDefault(configId,
                new ConnectorHealthStatus(configId, tenantId, "", "", "UNKNOWN", 0, 0, 100.0, 0.0, 0, Instant.now()));
        long now = System.currentTimeMillis();
        int consecutiveFailures = success ? 0 : prev.consecutiveFailures() + 1;
        double successRate = success
                ? Math.min(100.0, prev.successRate() + 0.5)
                : Math.max(0.0, prev.successRate() - 2.0);
        double avgLatency = prev.avgLatencyMs() == 0
                ? durationMs
                : (prev.avgLatencyMs() * 0.8 + durationMs * 0.2);
        String status = consecutiveFailures >= 5 ? "UNHEALTHY" : success ? "HEALTHY" : "DEGRADED";
        ConnectorHealthStatus updated = new ConnectorHealthStatus(
                configId, tenantId, prev.connectorType(), prev.platform(), status,
                success ? now : prev.lastSuccessAt(),
                success ? prev.lastFailureAt() : now,
                successRate, avgLatency, consecutiveFailures, Instant.now());
        healthStore.put(configId, updated);
        if ("UNHEALTHY".equals(status)) {
            log.warn("Connector unhealthy: configId={} consecutiveFailures={}", configId, consecutiveFailures);
        }
        return updated;
    }

    public ConnectorCallLog recordCall(String tenantId, String configId, String connectorType,
                                        String platform, String endpoint, String method,
                                        String traceId, int statusCode, long durationMs,
                                        boolean success, String errorMessage) {
        ConnectorCallLog callLog = new ConnectorCallLog(
                UUID.randomUUID().toString(), tenantId, configId, connectorType, platform,
                endpoint, method, traceId, statusCode, durationMs, success, errorMessage, Instant.now());
        extStore.saveConnectorCallLog(callLog);
        recordHealth(tenantId, configId, success, durationMs, errorMessage);
        return callLog;
    }

    public List<ConnectorCallLog> listCallLogs(String tenantId, String configId) {
        return extStore.listConnectorCallLogs(tenantId, configId);
    }

    public ConnectorSecret storeSecret(String tenantId, String configId, String keyType,
                                        String plainValue, String rotatedBy) {
        String encrypted = encrypt(plainValue);
        String masked = maskValue(plainValue);
        Instant now = Instant.now();
        ConnectorSecret secret = new ConnectorSecret(
                UUID.randomUUID().toString(), tenantId, configId, keyType,
                encrypted, masked, rotatedBy, now, now, now);
        extStore.saveConnectorSecret(secret);
        log.info("Connector secret stored: tenant={} config={} keyType={}", tenantId, configId, keyType);
        return secret;
    }

    public String decryptSecret(String tenantId, String secretId) {
        ConnectorSecret secret = extStore.findConnectorSecret(tenantId, secretId)
                .orElseThrow(() -> new BizException("SECRET_NOT_FOUND", "密钥不存在"));
        return decrypt(secret.encryptedValue());
    }

    public ConnectorSecret rotateSecret(String tenantId, String secretId, String newPlainValue, String rotatedBy) {
        ConnectorSecret secret = extStore.findConnectorSecret(tenantId, secretId)
                .orElseThrow(() -> new BizException("SECRET_NOT_FOUND", "密钥不存在"));
        String encrypted = encrypt(newPlainValue);
        String masked = maskValue(newPlainValue);
        Instant now = Instant.now();
        ConnectorSecret rotated = new ConnectorSecret(
                secret.secretId(), tenantId, secret.configId(), secret.keyType(),
                encrypted, masked, rotatedBy, now, secret.createdAt(), now);
        extStore.saveConnectorSecret(rotated);
        log.info("Connector secret rotated: tenant={} secret={}", tenantId, secretId);
        return rotated;
    }

    public List<ConnectorSecret> listSecrets(String tenantId, String configId) {
        return extStore.listConnectorSecrets(tenantId, configId);
    }

    private String encrypt(String plainText) {
        try {
            SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new BizException("ENCRYPTION_ERROR", "加密失败: " + e.getMessage());
        }
    }

    private String decrypt(String cipherText) {
        try {
            SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BizException("DECRYPTION_ERROR", "解密失败: " + e.getMessage());
        }
    }

    private String maskValue(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}
