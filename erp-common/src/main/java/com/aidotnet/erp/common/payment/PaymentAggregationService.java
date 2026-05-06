package com.aidotnet.erp.common.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentAggregationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentAggregationService.class);

    private final Map<String, PaymentChannel> channels = new ConcurrentHashMap<>();
    private final Map<String, PaymentTransaction> transactions = new ConcurrentHashMap<>();
    private final Map<String, PaymentAccount> accounts = new ConcurrentHashMap<>();
    private final Map<String, SettlementRecord> settlements = new ConcurrentHashMap<>();

    public void registerChannel(String channelCode, String channelName, String channelType,
                                 Map<String, String> config) {
        channels.put(channelCode, new PaymentChannel(channelCode, channelName, channelType, true, config));
        log.info("Registered payment channel: code={}, name={}, type={}", channelCode, channelName, channelType);
    }

    public PaymentTransaction pay(String channelCode, String tenantId, String businessType,
                                   String businessId, BigDecimal amount, String currency,
                                   Map<String, String> payParams) {
        PaymentChannel channel = channels.get(channelCode);
        if (channel == null || !channel.enabled()) {
            throw new IllegalArgumentException("Payment channel not available: " + channelCode);
        }
        String transactionId = "PAY-" + System.currentTimeMillis();
        PaymentTransaction tx = new PaymentTransaction(
                transactionId, tenantId, channelCode, businessType, businessId,
                amount, currency, "PAID", null, Instant.now(), Instant.now()
        );
        transactions.put(transactionId, tx);
        log.info("Payment initiated: txId={}, channel={}, amount={}, currency={}", transactionId, channelCode, amount, currency);
        return tx;
    }

    public PaymentTransaction refund(String transactionId, BigDecimal refundAmount, String reason) {
        PaymentTransaction tx = transactions.get(transactionId);
        if (tx == null) {
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }
        if (!"PAID".equals(tx.status())) {
            throw new IllegalStateException("Only PAID transactions can be refunded");
        }
        if (refundAmount.compareTo(tx.amount()) > 0) {
            throw new IllegalArgumentException("Refund amount exceeds original payment");
        }
        String refundTxId = "REF-" + System.currentTimeMillis();
        PaymentTransaction refundTx = new PaymentTransaction(
                refundTxId, tx.tenantId(), tx.channelCode(), "REFUND", tx.businessId(),
                refundAmount, tx.currency(), "REFUNDED", transactionId, Instant.now(), Instant.now()
        );
        transactions.put(refundTxId, refundTx);

        boolean isFullRefund = refundAmount.compareTo(tx.amount()) == 0;
        PaymentTransaction updated = new PaymentTransaction(
                tx.transactionId(), tx.tenantId(), tx.channelCode(), tx.businessType(),
                tx.businessId(), tx.amount(), tx.currency(),
                isFullRefund ? "FULLY_REFUNDED" : "PARTIALLY_REFUNDED",
                tx.refTransactionId(), tx.createdAt(), Instant.now()
        );
        transactions.put(tx.transactionId(), updated);
        log.info("Refund processed: refundTxId={}, originalTxId={}, amount={}", refundTxId, transactionId, refundAmount);
        return refundTx;
    }

    public BatchPayResult batchPay(String tenantId, List<BatchPayItem> items, String channelCode) {
        PaymentChannel channel = channels.get(channelCode);
        if (channel == null || !channel.enabled()) {
            throw new IllegalArgumentException("Payment channel not available: " + channelCode);
        }
        List<String> transactionIds = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int successCount = 0;
        int failCount = 0;

        for (BatchPayItem item : items) {
            try {
                PaymentTransaction tx = pay(channelCode, tenantId, item.businessType(),
                        item.businessId(), item.amount(), item.currency(), null);
                transactionIds.add(tx.transactionId());
                totalAmount = totalAmount.add(item.amount());
                successCount++;
            } catch (Exception ex) {
                log.warn("Batch pay failed for item: businessId={}, error={}", item.businessId(), ex.getMessage());
                failCount++;
            }
        }
        log.info("Batch pay completed: total={}, success={}, fail={}, amount={}", items.size(), successCount, failCount, totalAmount);
        return new BatchPayResult(transactionIds, totalAmount, successCount, failCount);
    }

    public List<PaymentChannel> listChannels() {
        return new ArrayList<>(channels.values());
    }

    public ReconciliationResult reconcile(String channelCode, String periodStart, String periodEnd) {
        List<PaymentTransaction> channelTxs = transactions.values().stream()
                .filter(tx -> channelCode.equals(tx.channelCode()))
                .toList();
        int totalCount = channelTxs.size();
        BigDecimal totalAmount = channelTxs.stream()
                .filter(tx -> "PAID".equals(tx.status()))
                .map(PaymentTransaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int refundCount = (int) channelTxs.stream().filter(tx -> "REFUNDED".equals(tx.status()) || "FULLY_REFUNDED".equals(tx.status()) || "PARTIALLY_REFUNDED".equals(tx.status())).count();
        log.info("Reconciliation completed: channel={}, period={}~{}, total={}, amount={}", channelCode, periodStart, periodEnd, totalCount, totalAmount);
        return new ReconciliationResult(channelCode, periodStart, periodEnd, totalCount, totalAmount, refundCount, List.of());
    }

    public PaymentAccount registerAccount(String channelCode, String accountId, String accountName,
                                           String currency, Map<String, String> credentials) {
        PaymentAccount account = new PaymentAccount(accountId, channelCode, accountName, currency,
                BigDecimal.ZERO, BigDecimal.ZERO, "ACTIVE", Instant.now());
        accounts.put(accountId, account);
        log.info("Registered payment account: id={}, channel={}, currency={}", accountId, channelCode, currency);
        return account;
    }

    public PaymentAccount getBalance(String accountId) {
        return accounts.get(accountId);
    }

    public SettlementRecord settleWithdraw(String accountId, BigDecimal amount, String fromCurrency,
                                             String toCurrency, String settlementType) {
        PaymentAccount account = accounts.get(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        if (account.availableBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for withdrawal");
        }
        String settlementId = "STL-" + System.currentTimeMillis();
        SettlementRecord record = new SettlementRecord(
                settlementId, accountId, amount, fromCurrency, toCurrency,
                settlementType, "PROCESSING", Instant.now(), null
        );
        settlements.put(settlementId, record);

        PaymentAccount updated = new PaymentAccount(
                account.accountId(), account.channelCode(), account.accountName(),
                account.currency(), account.balance().subtract(amount),
                account.frozenBalance(), account.status(), account.createdAt()
        );
        accounts.put(accountId, updated);
        log.info("Settlement/withdraw initiated: id={}, account={}, amount={}", settlementId, accountId, amount);
        return record;
    }

    public SettlementRecord amazonClaim(String orderId, BigDecimal claimAmount, String reason) {
        String claimId = "CLM-" + System.currentTimeMillis();
        SettlementRecord record = new SettlementRecord(
                claimId, orderId, claimAmount, "USD", "USD",
                "AMAZON_CLAIM", "SUBMITTED", Instant.now(), null
        );
        settlements.put(claimId, record);
        log.info("Amazon claim submitted: id={}, orderId={}, amount={}, reason={}", claimId, orderId, claimAmount, reason);
        return record;
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
