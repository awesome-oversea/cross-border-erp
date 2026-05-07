package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.ExternalFinanceConnector;
import com.aidotnet.erp.fms.domain.ExternalFinanceVoucher;
import com.aidotnet.erp.fms.domain.FinanceSyncConfig;
import com.aidotnet.erp.fms.infrastructure.FmsExtStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 外部财务系统同步服务
 * <p>
 * 描述: FMS域集成服务，对接外部财务系统(金蝶/用友)和支付平台。
 *       支持凭证导出、支付同步、结算同步等双向数据交换。
 *       通过连接器模式(ExternalFinanceConnector)隔离不同系统的API差异。
 * </p>
 * <p>
 * 支持的外部系统:
 *   1. 金蝶(KingdeeFinanceConnector) - 凭证导出、科目同步
 *   2. 用友(YonyouFinanceConnector) - 凭证导出、科目同步
 *   3. Stripe(StripeConnector) - 支付记录同步
 *   4. PayPal(PayPalConnector) - 支付记录同步
 *   5. PingPong(PingPongConnector) - 跨境支付同步
 *   6. 连连支付(LianLianPayConnector) - 跨境支付同步
 * </p>
 *
 * @author ERP系统
 * @see ExternalFinanceConnector
 * @see ExternalFinanceVoucher
 */
@Service
public class ExternalFinanceSyncService {

    private static final Logger log = LoggerFactory.getLogger(ExternalFinanceSyncService.class);

    private final FmsExtStore extStore;
    private final Map<String, ExternalFinanceConnector> connectorMap;

    public ExternalFinanceSyncService(FmsExtStore extStore, List<ExternalFinanceConnector> connectors) {
        this.extStore = extStore;
        this.connectorMap = new ConcurrentHashMap<>();
        for (ExternalFinanceConnector connector : connectors) {
            connectorMap.put(connector.getFinanceSystem(), connector);
        }
    }

    @Transactional
    public FinanceSyncConfig createSyncConfig(String tenantId, String financeSystem, String apiUrl,
                                               String apiKey, String apiSecret, String accountSet,
                                               boolean enabled, Map<String, String> mappingRules) {
        validateFinanceSystem(financeSystem);
        ExternalFinanceConnector connector = connectorMap.get(financeSystem);
        if (connector != null && enabled) {
            boolean connected = connector.testConnection(apiUrl, apiKey, apiSecret, accountSet);
            if (!connected) {
                throw new BizException("FINANCE_CONNECTION_FAILED", financeSystem + "连接测试失败");
            }
        }
        Instant now = Instant.now();
        FinanceSyncConfig existing = extStore.findFinanceSyncConfigBySystem(tenantId, financeSystem).orElse(null);
        FinanceSyncConfig config = existing == null
                ? new FinanceSyncConfig(
                        UUID.randomUUID().toString(), tenantId, financeSystem, apiUrl, apiKey,
                        apiSecret, accountSet, enabled, mappingRules, null, now, now)
                : new FinanceSyncConfig(
                        existing.configId(), existing.tenantId(), existing.financeSystem(), apiUrl, apiKey,
                        apiSecret, accountSet, enabled, mappingRules, existing.lastSyncAt(), existing.createdAt(), now);
        extStore.saveFinanceSyncConfig(config);
        log.info("Finance sync config created: tenant={} system={}", tenantId, financeSystem);
        return config;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExternalFinanceVoucher pushVoucher(String tenantId, String configId, String erpVoucherId, String voucherType,
                                               String voucherNumber, String erpReferenceType,
                                               String erpReferenceId, Map<String, Object> voucherData) {
        FinanceSyncConfig config = extStore.findFinanceSyncConfig(tenantId, configId)
                .orElseThrow(() -> new BizException("SYNC_CONFIG_NOT_FOUND", "同步配置不存在"));
        if (!config.enabled()) {
            throw new BizException("SYNC_DISABLED", "同步配置已禁用");
        }
        ExternalFinanceConnector connector = getConnector(config.financeSystem());
        Instant now = Instant.now();
        ExternalFinanceVoucher voucher = new ExternalFinanceVoucher(
                UUID.randomUUID().toString(), tenantId, erpVoucherId, config.financeSystem(), voucherType,
                voucherNumber, erpReferenceType, erpReferenceId, voucherData,
                "PENDING", null, null, now, now);
        try {
            String resultNumber = connector.pushVoucher(
                    config.apiUrl(), config.apiKey(), config.apiSecret(),
                    config.accountSet(), voucher);
            voucher = new ExternalFinanceVoucher(
                    voucher.voucherId(), voucher.tenantId(), voucher.erpVoucherId(), voucher.financeSystem(),
                    voucher.voucherType(), resultNumber, voucher.erpReferenceType(),
                    voucher.erpReferenceId(), voucher.voucherData(), "SYNCED", null, now, now, now);
            extStore.saveFinanceSyncConfig(new FinanceSyncConfig(
                    config.configId(), config.tenantId(), config.financeSystem(), config.apiUrl(), config.apiKey(),
                    config.apiSecret(), config.accountSet(), config.enabled(), config.mappingRules(),
                    now, config.createdAt(), now));
            log.info("Voucher pushed successfully: tenant={} system={} number={}",
                    tenantId, config.financeSystem(), resultNumber);
        } catch (Exception e) {
            voucher = new ExternalFinanceVoucher(
                    voucher.voucherId(), voucher.tenantId(), voucher.erpVoucherId(), voucher.financeSystem(),
                    voucher.voucherType(), voucher.voucherNumber(), voucher.erpReferenceType(),
                    voucher.erpReferenceId(), voucher.voucherData(), "FAILED",
                    e.getMessage(), null, now, now);
            log.error("Voucher push failed: tenant={} system={}", tenantId, config.financeSystem(), e);
        }
        extStore.saveExternalFinanceVoucher(voucher);
        return voucher;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExternalFinanceVoucher retryVoucher(String tenantId, String voucherId) {
        ExternalFinanceVoucher voucher = extStore.findExternalFinanceVoucher(tenantId, voucherId)
                .orElseThrow(() -> new BizException("VOUCHER_NOT_FOUND", "凭证不存在"));
        if (!"FAILED".equals(voucher.syncStatus())) {
            throw new BizException("VOUCHER_NOT_FAILED", "只有失败状态凭证可以重试");
        }
        FinanceSyncConfig config = extStore.findFinanceSyncConfigBySystem(tenantId, voucher.financeSystem())
                .orElseThrow(() -> new BizException("SYNC_CONFIG_NOT_FOUND", "同步配置不存在"));
        ExternalFinanceConnector connector = getConnector(voucher.financeSystem());
        try {
            String resultNumber = connector.pushVoucher(
                    config.apiUrl(), config.apiKey(), config.apiSecret(),
                    config.accountSet(), voucher);
            Instant now = Instant.now();
            voucher = new ExternalFinanceVoucher(
                    voucher.voucherId(), voucher.tenantId(), voucher.erpVoucherId(), voucher.financeSystem(),
                    voucher.voucherType(), resultNumber, voucher.erpReferenceType(),
                    voucher.erpReferenceId(), voucher.voucherData(), "SYNCED", null, now, now, now);
            extStore.saveFinanceSyncConfig(new FinanceSyncConfig(
                    config.configId(), config.tenantId(), config.financeSystem(), config.apiUrl(), config.apiKey(),
                    config.apiSecret(), config.accountSet(), config.enabled(), config.mappingRules(),
                    now, config.createdAt(), now));
        } catch (Exception e) {
            Instant now = Instant.now();
            voucher = new ExternalFinanceVoucher(
                    voucher.voucherId(), voucher.tenantId(), voucher.erpVoucherId(), voucher.financeSystem(),
                    voucher.voucherType(), voucher.voucherNumber(), voucher.erpReferenceType(),
                    voucher.erpReferenceId(), voucher.voucherData(), "FAILED",
                    e.getMessage(), null, now, now);
        }
        extStore.saveExternalFinanceVoucher(voucher);
        return voucher;
    }

    public List<ExternalFinanceVoucher> listVouchers(String tenantId, String syncStatus) {
        return extStore.listExternalFinanceVouchers(tenantId, syncStatus);
    }

    public ExternalFinanceVoucher findVoucher(String tenantId, String voucherId) {
        return extStore.findExternalFinanceVoucher(tenantId, voucherId)
                .orElseThrow(() -> new BizException("VOUCHER_NOT_FOUND", "External finance voucher not found"));
    }

    public List<FinanceSyncConfig> listSyncConfigs(String tenantId) {
        return extStore.listFinanceSyncConfigs(tenantId);
    }

    public java.util.Optional<FinanceSyncConfig> findSyncConfigBySystem(String tenantId, String financeSystem) {
        return extStore.findFinanceSyncConfigBySystem(tenantId, financeSystem);
    }

    public boolean testConnection(String tenantId, String configId) {
        FinanceSyncConfig config = extStore.findFinanceSyncConfig(tenantId, configId)
                .orElseThrow(() -> new BizException("SYNC_CONFIG_NOT_FOUND", "同步配置不存在"));
        ExternalFinanceConnector connector = getConnector(config.financeSystem());
        return connector.testConnection(config.apiUrl(), config.apiKey(),
                config.apiSecret(), config.accountSet());
    }

    private ExternalFinanceConnector getConnector(String financeSystem) {
        ExternalFinanceConnector connector = connectorMap.get(financeSystem);
        if (connector == null) {
            throw new BizException("CONNECTOR_NOT_FOUND", "不支持的财务系统: " + financeSystem);
        }
        return connector;
    }

    private void validateFinanceSystem(String financeSystem) {
        if (!connectorMap.containsKey(financeSystem)) {
            throw new BizException("UNSUPPORTED_FINANCE_SYSTEM", "不支持的财务系统: " + financeSystem);
        }
    }
}
