package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.CostAllocationResult;
import com.aidotnet.erp.fms.domain.PaymentRequest;
import com.aidotnet.erp.fms.domain.PlatformSettlement;
import com.aidotnet.erp.fms.domain.ProfitDeviationAlert;
import com.aidotnet.erp.fms.domain.ProfitResult;
import com.aidotnet.erp.fms.infrastructure.FmsExtStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * FMS出站应用服务
 * <p>
 * 描述: 为域外系统提供财务域的标准化出站能力。
 * 1. 支付执行接口直接复用付款申请域模型，保证状态流转口径一致。
 * 2. 支付执行需保持幂等，已支付申请重复调用时返回当前状态，而不是报错。
 * 3. 结算报表接口基于真实平台结算数据生成汇总，避免下游拿到占位结果。
 * </p>
 */
@Service
public class FmsOutboundService {

    private static final String DEFAULT_OUTBOUND_OPERATOR = "system-outbound";

    private final FinanceService financeService;
    private final FmsExtStore extStore;

    public FmsOutboundService(FinanceService financeService, FmsExtStore extStore) {
        this.financeService = financeService;
        this.extStore = extStore;
    }

    public PaymentExecutionResult executePayment(String tenantId, ExecutePaymentCommand command) {
        String paymentId = resolvePaymentId(command.paymentId(), command.requestId());
        PaymentRequest existing = financeService.getPaymentRequest(tenantId, paymentId);

        // 出站支付接口需要具备天然幂等性，重复执行已支付单据时直接返回最新状态。
        PaymentRequest paymentRequest = "PAID".equals(existing.status())
                ? existing
                : financeService.payPaymentRequest(tenantId, paymentId,
                new FinanceService.PayPaymentRequestCommand(resolvePaidBy(command.paidBy()), command.paidAt()));
        return toPaymentExecutionResult(paymentRequest);
    }

    public PaymentExecutionResult fetchPaymentStatus(String tenantId, String paymentId) {
        return toPaymentExecutionResult(financeService.getPaymentRequest(tenantId, resolvePaymentId(paymentId, null)));
    }

    public SettlementReportResult buildSettlementReport(String tenantId, SettlementReportQuery query) {
        LocalDate periodStart = parseDate(query.periodStart(), "periodStart");
        LocalDate periodEnd = parseDate(query.periodEnd(), "periodEnd");
        YearMonth period = parsePeriod(query.period());
        if (periodStart != null && periodEnd != null && periodStart.isAfter(periodEnd)) {
            throw new BizException("SETTLEMENT_REPORT_PERIOD_INVALID", "Settlement report period start cannot be after period end");
        }

        String platform = normalizeFilter(query.platform());
        String store = normalizeFilter(query.store());
        String status = normalizeFilter(query.status());

        List<PlatformSettlement> settlements = financeService.listPlatformSettlements(tenantId).stream()
                .filter(settlement -> matchesPeriod(settlement, period, periodStart, periodEnd))
                .filter(settlement -> matchesFilter(settlement.platform(), platform))
                .filter(settlement -> matchesFilter(settlement.store(), store))
                .filter(settlement -> matchesFilter(settlement.status(), status))
                .sorted(Comparator.comparing(PlatformSettlement::settlementDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(PlatformSettlement::settlementId))
                .toList();

        Map<String, Long> statuses = settlements.stream()
                .collect(Collectors.groupingBy(PlatformSettlement::status, LinkedHashMap::new, Collectors.counting()));

        return new SettlementReportResult(
                period != null ? period.toString() : null,
                periodStart,
                periodEnd,
                platform,
                store,
                status,
                settlements.size(),
                sum(settlements.stream().map(PlatformSettlement::amount).toList()),
                sum(settlements.stream().map(PlatformSettlement::reconciledAmount).toList()),
                sum(settlements.stream().map(PlatformSettlement::receivedAmount).toList()),
                settlements.stream().mapToInt(PlatformSettlement::linkedBillCount).sum(),
                statuses,
                settlements.stream().map(this::toSettlementLineResult).toList(),
                Instant.now());
    }

    public ProfitReportResult buildProfitReport(String tenantId, ProfitReportQuery query) {
        String dimensionType = normalizeFilter(query.dimensionType());
        String dimensionId = normalizeFilter(query.dimensionId());
        String sellerSku = normalizeFilter(query.sellerSku());
        String storeId = normalizeFilter(query.storeId());
        String marketplaceId = normalizeFilter(query.marketplaceId());
        String currency = normalizeFilter(query.currency());
        String alertStatus = normalizeFilter(query.alertStatus());

        List<ProfitResult> results = loadProfitResults(tenantId, dimensionType, dimensionId).stream()
                .filter(result -> matchesFilter(result.dimensionId(), dimensionId))
                .filter(result -> matchesFilter(result.sellerSku(), sellerSku))
                .filter(result -> matchesFilter(result.storeId(), storeId))
                .filter(result -> matchesFilter(result.marketplaceId(), marketplaceId))
                .filter(result -> matchesFilter(result.currency(), currency))
                .sorted(Comparator.comparing(ProfitResult::calculatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ProfitResult::resultId))
                .toList();

        Map<String, ProfitDeviationAlert> latestAlerts = latestAlertsByDimension(
                extStore.listProfitDeviationAlerts(tenantId, null));

        List<ProfitLineResult> lines = results.stream()
                .map(result -> toProfitLineResult(result, latestAlerts.get(dimensionKey(result)), tenantId))
                .filter(line -> alertStatus == null || line.latestAlert() != null)
                .toList();

        BigDecimal totalRevenue = sum(lines.stream().map(ProfitLineResult::revenue).toList());
        BigDecimal totalCost = sum(lines.stream().map(ProfitLineResult::totalCost).toList());
        BigDecimal totalGrossProfit = sum(lines.stream().map(ProfitLineResult::grossProfit).toList());

        return new ProfitReportResult(
                dimensionType,
                dimensionId,
                sellerSku,
                storeId,
                marketplaceId,
                currency,
                alertStatus,
                lines.size(),
                totalRevenue,
                totalCost,
                totalGrossProfit,
                calculateMargin(totalRevenue, totalGrossProfit),
                (int) lines.stream().filter(line -> line.latestAlert() != null).count(),
                (int) lines.stream().filter(line -> line.latestAlert() != null
                        && "OPEN".equalsIgnoreCase(line.latestAlert().status())).count(),
                buildCurrencySummaries(lines),
                lines,
                Instant.now());
    }

    private PaymentExecutionResult toPaymentExecutionResult(PaymentRequest paymentRequest) {
        return new PaymentExecutionResult(
                paymentRequest.requestId(),
                paymentRequest.requestId(),
                paymentRequest.status(),
                paymentRequest.requestType(),
                paymentRequest.supplierId(),
                paymentRequest.amount(),
                paymentRequest.currency(),
                paymentRequest.paidBy(),
                paymentRequest.paidAt(),
                paymentRequest.writeoffStatus(),
                paymentRequest.writeoffAmount(),
                paymentRequest.updatedAt());
    }

    private SettlementLineResult toSettlementLineResult(PlatformSettlement settlement) {
        return new SettlementLineResult(
                settlement.settlementId(),
                settlement.platform(),
                settlement.store(),
                settlement.settlementType(),
                settlement.amount(),
                settlement.reconciledAmount(),
                settlement.receivedAmount(),
                settlement.linkedBillCount(),
                settlement.currency(),
                settlement.settlementDate(),
                settlement.status(),
                settlement.withdrawalStatus(),
                settlement.withdrawalReference(),
                settlement.forexStatus(),
                settlement.forexRate(),
                settlement.receivedAt(),
                settlement.updatedAt());
    }

    private ProfitLineResult toProfitLineResult(ProfitResult result, ProfitDeviationAlert latestAlert, String tenantId) {
        List<CostTraceResult> costTraces = buildCostTraces(extStore.listCostAllocationResultsByTarget(
                        tenantId, result.dimensionType(), result.dimensionId()).stream()
                .filter(allocation -> matchesTrace(allocation, result))
                .sorted(Comparator.comparing(CostAllocationResult::allocatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CostAllocationResult::costEventId, Comparator.nullsLast(String::compareTo)))
                .toList());
        return new ProfitLineResult(
                result.resultId(),
                result.dimensionType(),
                result.dimensionId(),
                result.sellerSku(),
                result.orderId(),
                result.storeId(),
                result.marketplaceId(),
                result.currency(),
                result.revenue(),
                result.totalCost(),
                result.grossProfit(),
                result.grossMargin(),
                result.costDetails(),
                latestAlert != null ? new ProfitAlertResult(
                        latestAlert.alertId(),
                        latestAlert.status(),
                        latestAlert.severity(),
                        latestAlert.expectedMargin(),
                        latestAlert.actualMargin(),
                        latestAlert.deviation(),
                        latestAlert.detectedAt(),
                        latestAlert.resolvedAt()) : null,
                costTraces);
    }

    private boolean matchesTrace(CostAllocationResult allocation, ProfitResult result) {
        Map<String, String> dimensions = allocation.dimensions();
        String allocationSku = dimensions != null ? normalizeFilter(dimensions.get("sellerSku")) : null;
        String allocationStore = dimensions != null ? normalizeFilter(dimensions.get("storeId")) : null;
        String allocationMarketplace = dimensions != null ? normalizeFilter(dimensions.get("marketplaceId")) : null;
        return matchesOptionalScope(result.sellerSku(), allocationSku)
                && matchesOptionalScope(result.storeId(), allocationStore)
                && matchesOptionalScope(result.marketplaceId(), allocationMarketplace);
    }

    private boolean matchesOptionalScope(String expected, String actual) {
        String normalizedExpected = normalizeFilter(expected);
        return normalizedExpected == null || normalizedExpected.equalsIgnoreCase(normalizeFilter(actual));
    }

    private List<CostTraceResult> buildCostTraces(List<CostAllocationResult> allocations) {
        return allocations.stream()
                .map(allocation -> {
                    Map<String, String> dimensions = allocation.dimensions();
                    return new CostTraceResult(
                            allocation.costEventId(),
                            allocation.ruleId(),
                            dimensions != null ? dimensions.get("sourceType") : null,
                            dimensions != null ? dimensions.get("sourceId") : null,
                            dimensions != null ? dimensions.get("sellerSku") : null,
                            dimensions != null ? dimensions.get("storeId") : null,
                            dimensions != null ? dimensions.get("channelCode") : null,
                            dimensions != null ? dimensions.get("marketplaceId") : null,
                            dimensions != null ? dimensions.get("costType") : null,
                            allocation.allocatedAmount(),
                            allocation.currency(),
                            allocation.exchangeRate(),
                            allocation.amountInBaseCurrency(),
                            allocation.allocatedAt());
                })
                .toList();
    }

    private List<CurrencySummaryResult> buildCurrencySummaries(List<ProfitLineResult> lines) {
        Map<String, List<ProfitLineResult>> grouped = lines.stream()
                .collect(Collectors.groupingBy(line -> normalizeFilter(line.currency()) != null ? line.currency() : "UNKNOWN",
                        LinkedHashMap::new, Collectors.toList()));
        return grouped.entrySet().stream()
                .map(entry -> {
                    BigDecimal totalRevenue = sum(entry.getValue().stream().map(ProfitLineResult::revenue).toList());
                    BigDecimal totalCost = sum(entry.getValue().stream().map(ProfitLineResult::totalCost).toList());
                    BigDecimal totalGrossProfit = sum(entry.getValue().stream().map(ProfitLineResult::grossProfit).toList());
                    return new CurrencySummaryResult(
                            entry.getKey(),
                            entry.getValue().size(),
                            totalRevenue,
                            totalCost,
                            totalGrossProfit,
                            calculateMargin(totalRevenue, totalGrossProfit));
                })
                .toList();
    }

    private Map<String, ProfitDeviationAlert> latestAlertsByDimension(List<ProfitDeviationAlert> alerts) {
        Map<String, ProfitDeviationAlert> latestAlerts = new LinkedHashMap<>();
        for (ProfitDeviationAlert alert : alerts) {
            String key = dimensionKey(alert.dimensionType(), alert.dimensionId());
            ProfitDeviationAlert existing = latestAlerts.get(key);
            if (existing == null || compareInstant(alert.detectedAt(), existing.detectedAt()) > 0) {
                latestAlerts.put(key, alert);
            }
        }
        return latestAlerts;
    }

    private int compareInstant(Instant left, Instant right) {
        Instant normalizedLeft = Optional.ofNullable(left).orElse(Instant.EPOCH);
        Instant normalizedRight = Optional.ofNullable(right).orElse(Instant.EPOCH);
        return normalizedLeft.compareTo(normalizedRight);
    }

    private String dimensionKey(ProfitResult result) {
        return dimensionKey(result.dimensionType(), result.dimensionId());
    }

    private String dimensionKey(String dimensionType, String dimensionId) {
        return (dimensionType != null ? dimensionType : "") + "#" + (dimensionId != null ? dimensionId : "");
    }

    private List<ProfitResult> loadProfitResults(String tenantId, String dimensionType, String dimensionId) {
        if (dimensionType != null && dimensionId != null) {
            return extStore.listProfitResults(tenantId, dimensionType, dimensionId);
        }
        if (dimensionType != null) {
            return extStore.listProfitResultsByDimension(tenantId, dimensionType);
        }
        return extStore.listAllProfitResults(tenantId);
    }

    private BigDecimal calculateMargin(BigDecimal revenue, BigDecimal grossProfit) {
        if (revenue == null || grossProfit == null || revenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return grossProfit.divide(revenue, 4, RoundingMode.HALF_UP);
    }

    private boolean matchesPeriod(PlatformSettlement settlement, YearMonth period, LocalDate periodStart, LocalDate periodEnd) {
        LocalDate settlementDate = settlement.settlementDate();
        if (settlementDate == null) {
            return false;
        }
        if (period != null && !YearMonth.from(settlementDate).equals(period)) {
            return false;
        }
        if (periodStart != null && settlementDate.isBefore(periodStart)) {
            return false;
        }
        return periodEnd == null || !settlementDate.isAfter(periodEnd);
    }

    private boolean matchesFilter(String actualValue, String filterValue) {
        return filterValue == null || (actualValue != null && actualValue.equalsIgnoreCase(filterValue));
    }

    private String resolvePaymentId(String paymentId, String requestId) {
        String resolved = normalizeFilter(paymentId);
        if (resolved == null) {
            resolved = normalizeFilter(requestId);
        }
        if (resolved == null) {
            throw new BizException("PAYMENT_ID_REQUIRED", "Payment id is required");
        }
        return resolved;
    }

    private String resolvePaidBy(String paidBy) {
        String operator = normalizeFilter(paidBy);
        return operator != null ? operator : DEFAULT_OUTBOUND_OPERATOR;
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private YearMonth parsePeriod(String period) {
        String normalized = normalizeFilter(period);
        if (normalized == null) {
            return null;
        }
        try {
            return YearMonth.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new BizException("SETTLEMENT_REPORT_PERIOD_INVALID", "Settlement report period must use yyyy-MM format");
        }
    }

    private LocalDate parseDate(String value, String fieldName) {
        String normalized = normalizeFilter(value);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new BizException("SETTLEMENT_REPORT_DATE_INVALID", fieldName + " must use yyyy-MM-dd format");
        }
    }

    private BigDecimal sum(List<BigDecimal> amounts) {
        return amounts.stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public record ExecutePaymentCommand(String paymentId, String requestId, String paidBy, Instant paidAt) {}

    public record PaymentExecutionResult(String paymentId,
                                         String requestId,
                                         String status,
                                         String requestType,
                                         String supplierId,
                                         BigDecimal amount,
                                         String currency,
                                         String paidBy,
                                         Instant paidAt,
                                         String writeoffStatus,
                                         BigDecimal writeoffAmount,
                                         Instant updatedAt) {}

    public record SettlementReportQuery(String period,
                                        String periodStart,
                                        String periodEnd,
                                        String platform,
                                        String store,
                                        String status) {}

    public record SettlementReportResult(String period,
                                         LocalDate periodStart,
                                         LocalDate periodEnd,
                                         String platform,
                                         String store,
                                         String status,
                                         int totalCount,
                                         BigDecimal totalAmount,
                                         BigDecimal totalReconciledAmount,
                                         BigDecimal totalReceivedAmount,
                                         int totalLinkedBillCount,
                                         Map<String, Long> statuses,
                                         List<SettlementLineResult> settlements,
                                         Instant generatedAt) {}

    public record ProfitReportQuery(String dimensionType,
                                    String dimensionId,
                                    String sellerSku,
                                    String storeId,
                                    String marketplaceId,
                                    String currency,
                                    String alertStatus) {}

    public record ProfitReportResult(String dimensionType,
                                     String dimensionId,
                                     String sellerSku,
                                     String storeId,
                                     String marketplaceId,
                                     String currency,
                                     String alertStatus,
                                     int totalCount,
                                     BigDecimal totalRevenue,
                                     BigDecimal totalCost,
                                     BigDecimal totalGrossProfit,
                                     BigDecimal avgGrossMargin,
                                     int alertCount,
                                     int openAlertCount,
                                     List<CurrencySummaryResult> currencySummaries,
                                     List<ProfitLineResult> results,
                                     Instant generatedAt) {}

    public record CurrencySummaryResult(String currency,
                                        int totalCount,
                                        BigDecimal totalRevenue,
                                        BigDecimal totalCost,
                                        BigDecimal totalGrossProfit,
                                        BigDecimal avgGrossMargin) {}

    public record ProfitLineResult(String resultId,
                                   String dimensionType,
                                   String dimensionId,
                                   String sellerSku,
                                   String orderId,
                                   String storeId,
                                   String marketplaceId,
                                   String currency,
                                   BigDecimal revenue,
                                   BigDecimal totalCost,
                                   BigDecimal grossProfit,
                                   BigDecimal grossMargin,
                                   Map<String, BigDecimal> costDetails,
                                   ProfitAlertResult latestAlert,
                                   List<CostTraceResult> costTraces) {}

    public record ProfitAlertResult(String alertId,
                                    String status,
                                    String severity,
                                    BigDecimal expectedMargin,
                                    BigDecimal actualMargin,
                                    BigDecimal deviation,
                                    Instant detectedAt,
                                    Instant resolvedAt) {}

    public record CostTraceResult(String costEventId,
                                  String ruleId,
                                  String sourceType,
                                  String sourceId,
                                  String sellerSku,
                                  String storeId,
                                  String channelCode,
                                  String marketplaceId,
                                  String costType,
                                  BigDecimal allocatedAmount,
                                  String currency,
                                  BigDecimal exchangeRate,
                                  BigDecimal amountInBaseCurrency,
                                  Instant allocatedAt) {}

    public record SettlementLineResult(String settlementId,
                                       String platform,
                                       String store,
                                       String settlementType,
                                       BigDecimal amount,
                                       BigDecimal reconciledAmount,
                                       BigDecimal receivedAmount,
                                       int linkedBillCount,
                                       String currency,
                                       LocalDate settlementDate,
                                       String status,
                                       String withdrawalStatus,
                                       String withdrawalReference,
                                       String forexStatus,
                                       BigDecimal forexRate,
                                       Instant receivedAt,
                                       Instant updatedAt) {}
}
