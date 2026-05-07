package com.aidotnet.erp.common.payment;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.persistence.entity.CommonPaymentAccountEntity;
import com.aidotnet.erp.common.persistence.entity.CommonPaymentChannelEntity;
import com.aidotnet.erp.common.persistence.entity.CommonPaymentSettlementEntity;
import com.aidotnet.erp.common.persistence.entity.CommonPaymentTransactionEntity;
import com.aidotnet.erp.common.persistence.mapper.CommonPaymentAccountMapper;
import com.aidotnet.erp.common.persistence.mapper.CommonPaymentChannelMapper;
import com.aidotnet.erp.common.persistence.mapper.CommonPaymentSettlementMapper;
import com.aidotnet.erp.common.persistence.mapper.CommonPaymentTransactionMapper;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentAggregationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentAggregationService.class);

    private final CommonPaymentChannelMapper channelMapper;
    private final CommonPaymentAccountMapper accountMapper;
    private final CommonPaymentTransactionMapper transactionMapper;
    private final CommonPaymentSettlementMapper settlementMapper;
    private final ObjectMapper objectMapper;

    public PaymentAggregationService(CommonPaymentChannelMapper channelMapper,
                                     CommonPaymentAccountMapper accountMapper,
                                     CommonPaymentTransactionMapper transactionMapper,
                                     CommonPaymentSettlementMapper settlementMapper,
                                     ObjectMapper objectMapper) {
        this.channelMapper = channelMapper;
        this.accountMapper = accountMapper;
        this.transactionMapper = transactionMapper;
        this.settlementMapper = settlementMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void registerChannel(String channelCode, String channelName, String channelType,
                                Map<String, String> config) {
        String tenantId = currentTenantRequired();
        CommonPaymentChannelEntity existing = findChannelEntity(tenantId, channelCode);
        CommonPaymentChannelEntity entity = existing == null ? new CommonPaymentChannelEntity() : existing;
        entity.setTenantId(tenantId);
        entity.setChannelCode(normalizeRequired(channelCode, "PAYMENT_CHANNEL_CODE_REQUIRED"));
        entity.setChannelName(normalizeRequired(channelName, "PAYMENT_CHANNEL_NAME_REQUIRED"));
        entity.setChannelType(normalizeRequired(channelType, "PAYMENT_CHANNEL_TYPE_REQUIRED"));
        entity.setEnabled(Boolean.TRUE);
        entity.setConfigJson(writeJson(config != null ? config : Map.of()));
        entity.setCreatedAt(existing == null ? Instant.now() : existing.getCreatedAt());
        entity.setUpdatedAt(Instant.now());
        if (existing == null) {
            channelMapper.insert(entity);
        } else {
            channelMapper.updateById(entity);
        }
        log.info("Registered payment channel: tenantId={}, channelCode={}, channelType={}",
                tenantId, entity.getChannelCode(), entity.getChannelType());
    }

    @Transactional
    public PaymentTransaction pay(String channelCode, String tenantId, String businessType,
                                  String businessId, BigDecimal amount, String currency,
                                  Map<String, String> payParams) {
        return executeInTenant(tenantId, () -> doPay(channelCode, businessType, businessId, amount, currency, payParams));
    }

    @Transactional
    public PaymentTransaction refund(String transactionId, BigDecimal refundAmount, String reason) {
        String tenantId = currentTenantRequired();
        CommonPaymentTransactionEntity original = findTransactionEntity(tenantId, transactionId);
        if (original == null) {
            throw new BizException("PAYMENT_TRANSACTION_NOT_FOUND", "Payment transaction does not exist");
        }
        if (!isRefundableStatus(original.getStatus())) {
            throw new BizException("PAYMENT_TRANSACTION_STATUS_INVALID", "Only paid transactions can be refunded");
        }
        BigDecimal alreadyRefunded = transactionMapper.selectList(new LambdaQueryWrapper<CommonPaymentTransactionEntity>()
                        .eq(CommonPaymentTransactionEntity::getTenantId, tenantId)
                        .eq(CommonPaymentTransactionEntity::getRefTransactionId, transactionId))
                .stream()
                .map(CommonPaymentTransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("PAYMENT_REFUND_AMOUNT_INVALID", "Refund amount must be greater than zero");
        }
        if (alreadyRefunded.add(refundAmount).compareTo(original.getAmount()) > 0) {
            throw new BizException("PAYMENT_REFUND_EXCEEDS_ORIGINAL", "Refund amount exceeds original payment");
        }

        CommonPaymentTransactionEntity refund = new CommonPaymentTransactionEntity();
        refund.setTenantId(tenantId);
        refund.setTransactionId(nextBusinessId("REF"));
        refund.setChannelCode(original.getChannelCode());
        refund.setAccountId(original.getAccountId());
        refund.setBusinessType("REFUND");
        refund.setBusinessId(original.getBusinessId());
        refund.setAmount(refundAmount);
        refund.setCurrency(original.getCurrency());
        refund.setStatus("REFUNDED");
        refund.setDirection("OUTFLOW");
        refund.setRefTransactionId(original.getTransactionId());
        refund.setReason(trimToNull(reason));
        refund.setCreatedAt(Instant.now());
        refund.setUpdatedAt(refund.getCreatedAt());
        transactionMapper.insert(refund);

        original.setStatus(alreadyRefunded.add(refundAmount).compareTo(original.getAmount()) == 0
                ? "FULLY_REFUNDED"
                : "PARTIALLY_REFUNDED");
        original.setUpdatedAt(Instant.now());
        transactionMapper.updateById(original);

        if (hasText(original.getAccountId())) {
            CommonPaymentAccountEntity account = findAccountEntity(tenantId, original.getAccountId());
            if (account != null) {
                account.setBalance(safeAmount(account.getBalance()).subtract(refundAmount));
                account.setUpdatedAt(Instant.now());
                accountMapper.updateById(account);
            }
        }

        log.info("Refund processed: tenantId={}, originalTransactionId={}, refundTransactionId={}, amount={}",
                tenantId, transactionId, refund.getTransactionId(), refundAmount);
        return toTransaction(refund);
    }

    @Transactional
    public BatchPayResult batchPay(String tenantId, List<BatchPayItem> items, String channelCode) {
        return executeInTenant(tenantId, () -> {
            List<String> transactionIds = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            int successCount = 0;
            int failCount = 0;

            for (BatchPayItem item : items) {
                try {
                    PaymentTransaction tx = doPay(channelCode, item.businessType(), item.businessId(),
                            item.amount(), item.currency(), null);
                    transactionIds.add(tx.transactionId());
                    totalAmount = totalAmount.add(item.amount());
                    successCount++;
                } catch (RuntimeException ex) {
                    log.warn("Batch pay failed: tenantId={}, businessId={}, error={}",
                            tenantId, item.businessId(), ex.getMessage());
                    failCount++;
                }
            }

            return new BatchPayResult(transactionIds, totalAmount, successCount, failCount);
        });
    }

    public List<PaymentChannel> listChannels() {
        String tenantId = currentTenantRequired();
        return channelMapper.selectList(new LambdaQueryWrapper<CommonPaymentChannelEntity>()
                        .eq(CommonPaymentChannelEntity::getTenantId, tenantId)
                        .orderByAsc(CommonPaymentChannelEntity::getCreatedAt, CommonPaymentChannelEntity::getChannelCode))
                .stream()
                .map(this::toChannel)
                .toList();
    }

    @Transactional
    public PaymentAccount registerAccount(String channelCode, String accountId, String accountName,
                                          String currency, Map<String, String> credentials) {
        String tenantId = currentTenantRequired();
        CommonPaymentChannelEntity channel = requireChannel(tenantId, channelCode);
        CommonPaymentAccountEntity existing = findAccountEntity(tenantId, accountId);
        CommonPaymentAccountEntity entity = existing == null ? new CommonPaymentAccountEntity() : existing;
        entity.setTenantId(tenantId);
        entity.setAccountId(normalizeRequired(accountId, "PAYMENT_ACCOUNT_ID_REQUIRED"));
        entity.setChannelCode(channel.getChannelCode());
        entity.setAccountName(normalizeRequired(accountName, "PAYMENT_ACCOUNT_NAME_REQUIRED"));
        entity.setCurrency(normalizeRequired(currency, "PAYMENT_ACCOUNT_CURRENCY_REQUIRED"));
        entity.setBalance(existing == null ? BigDecimal.ZERO : safeAmount(existing.getBalance()));
        entity.setFrozenBalance(existing == null ? BigDecimal.ZERO : safeAmount(existing.getFrozenBalance()));
        entity.setStatus("ACTIVE");
        entity.setCredentialsJson(writeJson(credentials != null ? credentials : Map.of()));
        entity.setCreatedAt(existing == null ? Instant.now() : existing.getCreatedAt());
        entity.setUpdatedAt(Instant.now());
        if (existing == null) {
            accountMapper.insert(entity);
        } else {
            accountMapper.updateById(entity);
        }
        return toAccount(entity);
    }

    public PaymentAccount getBalance(String accountId) {
        String tenantId = currentTenantRequired();
        CommonPaymentAccountEntity account = findAccountEntity(tenantId, accountId);
        if (account == null) {
            throw new BizException("PAYMENT_ACCOUNT_NOT_FOUND", "Payment account does not exist");
        }
        return toAccount(account);
    }

    @Transactional
    public SettlementRecord settleWithdraw(String accountId, BigDecimal amount, String fromCurrency,
                                           String toCurrency, String settlementType) {
        String tenantId = currentTenantRequired();
        CommonPaymentAccountEntity account = findAccountEntity(tenantId, accountId);
        if (account == null) {
            throw new BizException("PAYMENT_ACCOUNT_NOT_FOUND", "Payment account does not exist");
        }
        BigDecimal availableBalance = safeAmount(account.getBalance()).subtract(safeAmount(account.getFrozenBalance()));
        if (availableBalance.compareTo(amount) < 0) {
            throw new BizException("PAYMENT_ACCOUNT_BALANCE_INSUFFICIENT", "Insufficient balance for withdrawal");
        }

        CommonPaymentSettlementEntity settlement = new CommonPaymentSettlementEntity();
        settlement.setTenantId(tenantId);
        settlement.setSettlementId(nextBusinessId("STL"));
        settlement.setAccountId(account.getAccountId());
        settlement.setChannelCode(account.getChannelCode());
        settlement.setBusinessReference(account.getAccountId());
        settlement.setAmount(amount);
        settlement.setFromCurrency(normalizeRequired(fromCurrency, "PAYMENT_SETTLEMENT_CURRENCY_REQUIRED"));
        settlement.setToCurrency(normalizeRequired(toCurrency, "PAYMENT_SETTLEMENT_TARGET_CURRENCY_REQUIRED"));
        settlement.setSettlementType(normalizeRequired(settlementType, "PAYMENT_SETTLEMENT_TYPE_REQUIRED"));
        settlement.setStatus("PROCESSING");
        settlement.setCreatedAt(Instant.now());
        settlementMapper.insert(settlement);

        account.setBalance(safeAmount(account.getBalance()).subtract(amount));
        account.setUpdatedAt(Instant.now());
        accountMapper.updateById(account);

        return toSettlement(settlement);
    }

    @Transactional
    public SettlementRecord amazonClaim(String orderId, BigDecimal claimAmount, String reason) {
        String tenantId = currentTenantRequired();
        CommonPaymentSettlementEntity settlement = new CommonPaymentSettlementEntity();
        settlement.setTenantId(tenantId);
        settlement.setSettlementId(nextBusinessId("CLM"));
        settlement.setAccountId(normalizeRequired(orderId, "PAYMENT_CLAIM_ORDER_REQUIRED"));
        settlement.setChannelCode("AMAZON");
        settlement.setBusinessReference(orderId);
        settlement.setAmount(claimAmount);
        settlement.setFromCurrency("USD");
        settlement.setToCurrency("USD");
        settlement.setSettlementType("AMAZON_CLAIM");
        settlement.setStatus("SUBMITTED");
        settlement.setReason(trimToNull(reason));
        settlement.setCreatedAt(Instant.now());
        settlementMapper.insert(settlement);
        return toSettlement(settlement);
    }

    public ReconciliationResult reconcile(String channelCode, String periodStart, String periodEnd) {
        String tenantId = currentTenantRequired();
        requireChannel(tenantId, channelCode);
        Instant start = LocalDate.parse(periodStart).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endExclusive = LocalDate.parse(periodEnd).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<CommonPaymentTransactionEntity> transactions = transactionMapper.selectList(new LambdaQueryWrapper<CommonPaymentTransactionEntity>()
                .eq(CommonPaymentTransactionEntity::getTenantId, tenantId)
                .eq(CommonPaymentTransactionEntity::getChannelCode, channelCode)
                .ge(CommonPaymentTransactionEntity::getCreatedAt, start)
                .lt(CommonPaymentTransactionEntity::getCreatedAt, endExclusive)
                .orderByAsc(CommonPaymentTransactionEntity::getCreatedAt, CommonPaymentTransactionEntity::getTransactionId));

        BigDecimal inflow = transactions.stream()
                .filter(tx -> "INFLOW".equals(tx.getDirection()))
                .map(CommonPaymentTransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outflow = transactions.stream()
                .filter(tx -> "OUTFLOW".equals(tx.getDirection()))
                .map(CommonPaymentTransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int refundCount = (int) transactions.stream()
                .filter(tx -> hasText(tx.getRefTransactionId()))
                .count();
        List<String> discrepancies = transactions.stream()
                .filter(tx -> "PAID".equals(tx.getStatus()) && !hasText(tx.getAccountId()))
                .map(CommonPaymentTransactionEntity::getTransactionId)
                .map(id -> "UNBOUND_ACCOUNT:" + id)
                .toList();

        log.info("Reconciled payment channel: tenantId={}, channelCode={}, transactionCount={}, netAmount={}",
                tenantId, channelCode, transactions.size(), inflow.subtract(outflow));
        return new ReconciliationResult(channelCode, periodStart, periodEnd, transactions.size(),
                inflow.subtract(outflow), refundCount, discrepancies);
    }

    private PaymentTransaction doPay(String channelCode, String businessType, String businessId,
                                     BigDecimal amount, String currency, Map<String, String> payParams) {
        String tenantId = currentTenantRequired();
        CommonPaymentChannelEntity channel = requireChannel(tenantId, channelCode);
        CommonPaymentAccountEntity account = findDefaultAccount(tenantId, channelCode, currency);

        CommonPaymentTransactionEntity entity = new CommonPaymentTransactionEntity();
        entity.setTenantId(tenantId);
        entity.setTransactionId(nextBusinessId("PAY"));
        entity.setChannelCode(channel.getChannelCode());
        entity.setAccountId(account != null ? account.getAccountId() : null);
        entity.setBusinessType(normalizeRequired(businessType, "PAYMENT_BUSINESS_TYPE_REQUIRED"));
        entity.setBusinessId(normalizeRequired(businessId, "PAYMENT_BUSINESS_ID_REQUIRED"));
        entity.setAmount(requirePositive(amount, "PAYMENT_AMOUNT_INVALID"));
        entity.setCurrency(normalizeRequired(currency, "PAYMENT_CURRENCY_REQUIRED"));
        entity.setStatus("PAID");
        entity.setDirection("INFLOW");
        entity.setReason(writeJson(payParams != null ? payParams : Map.of()));
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
        transactionMapper.insert(entity);

        if (account != null) {
            account.setBalance(safeAmount(account.getBalance()).add(amount));
            account.setUpdatedAt(Instant.now());
            accountMapper.updateById(account);
        }

        log.info("Recorded payment transaction: tenantId={}, transactionId={}, channelCode={}, businessId={}, amount={}",
                tenantId, entity.getTransactionId(), channelCode, businessId, amount);
        return toTransaction(entity);
    }

    private CommonPaymentChannelEntity requireChannel(String tenantId, String channelCode) {
        CommonPaymentChannelEntity channel = findChannelEntity(tenantId, channelCode);
        if (channel == null || !Boolean.TRUE.equals(channel.getEnabled())) {
            throw new BizException("PAYMENT_CHANNEL_NOT_FOUND", "Payment channel is not available");
        }
        return channel;
    }

    private CommonPaymentChannelEntity findChannelEntity(String tenantId, String channelCode) {
        return channelMapper.selectOne(new LambdaQueryWrapper<CommonPaymentChannelEntity>()
                .eq(CommonPaymentChannelEntity::getTenantId, tenantId)
                .eq(CommonPaymentChannelEntity::getChannelCode, trimToNull(channelCode)));
    }

    private CommonPaymentAccountEntity findAccountEntity(String tenantId, String accountId) {
        return accountMapper.selectOne(new LambdaQueryWrapper<CommonPaymentAccountEntity>()
                .eq(CommonPaymentAccountEntity::getTenantId, tenantId)
                .eq(CommonPaymentAccountEntity::getAccountId, trimToNull(accountId)));
    }

    private CommonPaymentAccountEntity findDefaultAccount(String tenantId, String channelCode, String currency) {
        return accountMapper.selectList(new LambdaQueryWrapper<CommonPaymentAccountEntity>()
                        .eq(CommonPaymentAccountEntity::getTenantId, tenantId)
                        .eq(CommonPaymentAccountEntity::getChannelCode, trimToNull(channelCode))
                        .eq(CommonPaymentAccountEntity::getCurrency, trimToNull(currency))
                        .eq(CommonPaymentAccountEntity::getStatus, "ACTIVE")
                        .orderByAsc(CommonPaymentAccountEntity::getCreatedAt, CommonPaymentAccountEntity::getAccountId))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private CommonPaymentTransactionEntity findTransactionEntity(String tenantId, String transactionId) {
        return transactionMapper.selectOne(new LambdaQueryWrapper<CommonPaymentTransactionEntity>()
                .eq(CommonPaymentTransactionEntity::getTenantId, tenantId)
                .eq(CommonPaymentTransactionEntity::getTransactionId, trimToNull(transactionId)));
    }

    private PaymentChannel toChannel(CommonPaymentChannelEntity entity) {
        return new PaymentChannel(
                entity.getChannelCode(),
                entity.getChannelName(),
                entity.getChannelType(),
                Boolean.TRUE.equals(entity.getEnabled()),
                readStringMap(entity.getConfigJson()));
    }

    private PaymentTransaction toTransaction(CommonPaymentTransactionEntity entity) {
        return new PaymentTransaction(
                entity.getTransactionId(),
                entity.getTenantId(),
                entity.getChannelCode(),
                entity.getBusinessType(),
                entity.getBusinessId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getRefTransactionId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private PaymentAccount toAccount(CommonPaymentAccountEntity entity) {
        return new PaymentAccount(
                entity.getAccountId(),
                entity.getChannelCode(),
                entity.getAccountName(),
                entity.getCurrency(),
                safeAmount(entity.getBalance()),
                safeAmount(entity.getFrozenBalance()),
                entity.getStatus(),
                entity.getCreatedAt());
    }

    private SettlementRecord toSettlement(CommonPaymentSettlementEntity entity) {
        return new SettlementRecord(
                entity.getSettlementId(),
                entity.getAccountId(),
                entity.getAmount(),
                entity.getFromCurrency(),
                entity.getToCurrency(),
                entity.getSettlementType(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCompletedAt());
    }

    private Map<String, String> readStringMap(String json) {
        if (!hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            log.warn("Failed to read payment config json", ex);
            return Collections.emptyMap();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BizException("PAYMENT_SERIALIZATION_ERROR", "Payment data serialization failed");
        }
    }

    private boolean isRefundableStatus(String status) {
        return "PAID".equals(status) || "PARTIALLY_REFUNDED".equals(status);
    }

    private BigDecimal requirePositive(BigDecimal amount, String code) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(code, "Payment amount must be greater than zero");
        }
        return amount;
    }

    private String normalizeRequired(String value, String code) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BizException(code, "Required payment field is missing");
        }
        return normalized;
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String currentTenantRequired() {
        String tenantId = trimToNull(TenantContext.getTenantId());
        if (tenantId == null) {
            throw new BizException("TENANT_REQUIRED", "Tenant id is required");
        }
        return tenantId;
    }

    private String nextBusinessId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private <T> T executeInTenant(String tenantId, Supplier<T> action) {
        String previousTenant = TenantContext.getTenantId();
        String normalizedTenant = normalizeRequired(tenantId, "TENANT_REQUIRED");
        TenantContext.setTenantId(normalizedTenant);
        try {
            return action.get();
        } finally {
            TenantContext.setTenantId(previousTenant);
        }
    }

    public record PaymentChannel(String channelCode, String channelName, String channelType,
                                 boolean enabled, Map<String, String> config) {}

    public record PaymentTransaction(String transactionId, String tenantId, String channelCode,
                                     String businessType, String businessId, BigDecimal amount,
                                     String currency, String status, String refTransactionId,
                                     Instant createdAt, Instant updatedAt) {}

    public record PaymentAccount(String accountId, String channelCode, String accountName,
                                 String currency, BigDecimal balance, BigDecimal frozenBalance,
                                 String status, Instant createdAt) {
        public BigDecimal availableBalance() {
            return balance.subtract(frozenBalance);
        }
    }

    public record SettlementRecord(String settlementId, String accountId, BigDecimal amount,
                                   String fromCurrency, String toCurrency, String settlementType,
                                   String status, Instant createdAt, Instant completedAt) {}

    public record BatchPayItem(String businessType, String businessId, BigDecimal amount, String currency) {}

    public record BatchPayResult(List<String> transactionIds, BigDecimal totalAmount,
                                 int successCount, int failCount) {}

    public record ReconciliationResult(String channelCode, String periodStart, String periodEnd,
                                       int totalCount, BigDecimal totalAmount, int refundCount,
                                       List<String> discrepancies) {}
}
