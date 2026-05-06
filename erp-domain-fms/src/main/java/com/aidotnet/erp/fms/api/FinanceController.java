package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.FinanceService.ApprovePaymentRequestCommand;
import com.aidotnet.erp.fms.application.FinanceService;
import com.aidotnet.erp.fms.application.FinanceService.ApproveWriteOffCommand;
import com.aidotnet.erp.fms.application.FinanceService.CalculateProfitCommand;
import com.aidotnet.erp.fms.application.FinanceService.CreateForexTransactionCommand;
import com.aidotnet.erp.fms.application.FinanceService.CreatePaymentRequestCommand;
import com.aidotnet.erp.fms.application.FinanceService.CreatePlatformSettlementCommand;
import com.aidotnet.erp.fms.application.FinanceService.CreateReceivableCommand;
import com.aidotnet.erp.fms.application.FinanceService.CreateReconciliationCommand;
import com.aidotnet.erp.fms.application.FinanceService.CreateWriteOffCommand;
import com.aidotnet.erp.fms.application.FinanceService.DisputeReconciliationCommand;
import com.aidotnet.erp.fms.application.FinanceService.ImportPlatformBillCommand;
import com.aidotnet.erp.fms.application.FinanceService.PayPaymentRequestCommand;
import com.aidotnet.erp.fms.application.FinanceService.ReceivePaymentCommand;
import com.aidotnet.erp.fms.application.FinanceService.ReceivePlatformSettlementCommand;
import com.aidotnet.erp.fms.application.FinanceService.ReconcilePlatformBillCommand;
import com.aidotnet.erp.fms.application.FinanceService.RecordCostEventCommand;
import com.aidotnet.erp.fms.application.FinanceService.SaveForexRateCommand;
import com.aidotnet.erp.fms.application.FinanceService.UpdatePlatformSettlementForexCommand;
import com.aidotnet.erp.fms.application.FinanceService.UpdatePlatformSettlementWithdrawalCommand;
import com.aidotnet.erp.fms.domain.CostEvent;
import com.aidotnet.erp.fms.domain.ForexRate;
import com.aidotnet.erp.fms.domain.ForexRiskAlert;
import com.aidotnet.erp.fms.domain.ForexTransaction;
import com.aidotnet.erp.fms.domain.PaymentApproval;
import com.aidotnet.erp.fms.domain.PaymentRecord;
import com.aidotnet.erp.fms.domain.PaymentRequest;
import com.aidotnet.erp.fms.domain.PlatformBill;
import com.aidotnet.erp.fms.domain.PlatformSettlement;
import com.aidotnet.erp.fms.domain.ProfitStatement;
import com.aidotnet.erp.fms.domain.Reconciliation;
import com.aidotnet.erp.fms.domain.Receivable;
import com.aidotnet.erp.fms.domain.WriteOff;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FMS核心财务控制器
 * <p>
 * 描述: 财务域核心REST接口，提供成本事件、应收管理、付款记录、
 *       平台账单、结算对账、利润报表等全量财务操作。
 * </p>
 * <p>
 * 路径规范: /fms/api/in/v1 — 内部方向(in)，v1版本
 * </p>
 * <p>
 * 核心业务:
 *   1. 成本事件 - 记录/查询成本事件
 *   2. 应收管理 - 创建/收款/核销应收账款
 *   3. 付款审批 - 创建/审批付款请求
 *   4. 平台账单 - 查询/对账平台账单
 *   5. 结算管理 - 查询/对账平台结算
 *   6. 利润报表 - 按维度查询利润
 *   7. 对账管理 - 创建/查询对账单
 * </p>
 *
 * @author ERP系统
 * @see FinanceService
 */
@RestController
@RequestMapping("/fms/api/in/v1")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/receivables")
    public Result<Receivable> createReceivable(@Valid @RequestBody CreateReceivableRequest request) {
        return Result.ok(financeService.createReceivable(currentTenant(), new CreateReceivableCommand(
                request.sourceType(), request.sourceId(), request.customerName(), request.currency(), request.amount())));
    }

    @GetMapping("/receivables")
    public Result<List<Receivable>> listReceivables() {
        return Result.ok(financeService.listReceivables(currentTenant()));
    }

    @GetMapping("/receivables/by-source")
    public Result<Receivable> getReceivableBySource(@NotBlank String sourceType, @NotBlank String sourceId) {
        return Result.ok(financeService.getReceivableBySource(currentTenant(), sourceType, sourceId));
    }

    @PatchMapping("/receivables/{receivableId}/confirm")
    public Result<Receivable> confirm(@PathVariable String receivableId) {
        return Result.ok(financeService.confirm(currentTenant(), receivableId));
    }

    @PostMapping("/receivables/{receivableId}/payments")
    public Result<Receivable> receivePayment(@PathVariable String receivableId, @Valid @RequestBody ReceivePaymentRequest request) {
        return Result.ok(financeService.receivePayment(currentTenant(), receivableId, new ReceivePaymentCommand(request.amount(), request.paymentMethod())));
    }

    @GetMapping("/receivables/{receivableId}/payments")
    public Result<List<PaymentRecord>> listPayments(@PathVariable String receivableId) {
        return Result.ok(financeService.listPayments(currentTenant(), receivableId));
    }

    @PostMapping("/payment-requests")
    public Result<PaymentRequest> createPaymentRequest(@Valid @RequestBody CreatePaymentRequestRequest request) {
        return Result.ok(financeService.createPaymentRequest(currentTenant(), new CreatePaymentRequestCommand(
                request.requestId(), request.poId(), request.supplierId(), request.amount(), request.currency(),
                request.requestType(), request.requestedBy(),
                request.approvalFlow() != null ? request.approvalFlow().toString() : null)));
    }

    @GetMapping("/payment-requests")
    public Result<List<PaymentRequest>> listPaymentRequests() {
        return Result.ok(financeService.listPaymentRequests(currentTenant()));
    }

    @GetMapping("/payment-requests/{requestId}")
    public Result<PaymentRequest> getPaymentRequest(@PathVariable String requestId) {
        return Result.ok(financeService.getPaymentRequest(currentTenant(), requestId));
    }

    @GetMapping("/payment-requests/{requestId}/approvals")
    public Result<List<PaymentApproval>> listPaymentApprovals(@PathVariable String requestId) {
        return Result.ok(financeService.listPaymentApprovals(currentTenant(), requestId));
    }

    @PostMapping("/payment-requests/{requestId}/approve")
    public Result<PaymentRequest> approvePaymentRequest(@PathVariable String requestId,
                                                        @Valid @RequestBody ApprovePaymentRequestRequest request) {
        return Result.ok(financeService.approvePaymentRequest(currentTenant(), requestId,
                new ApprovePaymentRequestCommand(request.approverId(), request.approvalLevel(),
                        request.comment(), request.approvedAt())));
    }

    @PostMapping("/payment-requests/{requestId}/pay")
    public Result<PaymentRequest> payPaymentRequest(@PathVariable String requestId,
                                                    @Valid @RequestBody PayPaymentRequestRequest request) {
        return Result.ok(financeService.payPaymentRequest(currentTenant(), requestId,
                new PayPaymentRequestCommand(request.paidBy(), request.paidAt())));
    }

    @PostMapping("/payment-requests/{requestId}/cancel")
    public Result<PaymentRequest> cancelPaymentRequest(@PathVariable String requestId) {
        return Result.ok(financeService.cancelPaymentRequest(currentTenant(), requestId));
    }

    @PostMapping("/write-offs")
    public Result<WriteOff> createWriteOff(@Valid @RequestBody CreateWriteOffRequest request) {
        return Result.ok(financeService.createWriteOff(currentTenant(), new CreateWriteOffCommand(
                request.writeoffId(), request.type(), request.refType(), request.refId(),
                request.amount(), request.currency())));
    }

    @GetMapping("/write-offs")
    public Result<List<WriteOff>> listWriteOffs() {
        return Result.ok(financeService.listWriteOffs(currentTenant()));
    }

    @GetMapping("/write-offs/{writeoffId}")
    public Result<WriteOff> getWriteOff(@PathVariable String writeoffId) {
        return Result.ok(financeService.getWriteOff(currentTenant(), writeoffId));
    }

    @GetMapping("/write-offs/by-ref")
    public Result<List<WriteOff>> listWriteOffsByRef(@NotBlank String refType, @NotBlank String refId) {
        return Result.ok(financeService.listWriteOffsByRef(currentTenant(), refType, refId));
    }

    @PostMapping("/write-offs/{writeoffId}/approve")
    public Result<WriteOff> approveWriteOff(@PathVariable String writeoffId,
                                            @Valid @RequestBody ApproveWriteOffRequest request) {
        return Result.ok(financeService.approveWriteOff(currentTenant(), writeoffId,
                new ApproveWriteOffCommand(request.approvedBy())));
    }

    @PostMapping("/reconciliations")
    public Result<Reconciliation> createReconciliation(@Valid @RequestBody CreateReconciliationRequest request) {
        return Result.ok(financeService.createReconciliation(currentTenant(), new CreateReconciliationCommand(
                request.reconId(), request.type(), request.partyId(), request.partyName(), request.period(),
                request.payableAmount(), request.paidAmount(), request.items() != null ? request.items().toString() : null)));
    }

    @GetMapping("/reconciliations")
    public Result<List<Reconciliation>> listReconciliations() {
        return Result.ok(financeService.listReconciliations(currentTenant()));
    }

    @GetMapping("/reconciliations/{reconId}")
    public Result<Reconciliation> getReconciliation(@PathVariable String reconId) {
        return Result.ok(financeService.getReconciliation(currentTenant(), reconId));
    }

    @GetMapping("/reconciliations/supplier/{partyId}")
    public Result<List<Reconciliation>> listSupplierReconciliations(@PathVariable String partyId) {
        return Result.ok(financeService.listReconciliationsByTypeAndParty(currentTenant(), "SUPPLIER", partyId));
    }

    @GetMapping("/reconciliations/logistics/{partyId}")
    public Result<List<Reconciliation>> listLogisticsReconciliations(@PathVariable String partyId) {
        return Result.ok(financeService.listReconciliationsByTypeAndParty(currentTenant(), "LOGISTICS", partyId));
    }

    @PostMapping("/reconciliations/{reconId}/dispute")
    public Result<Reconciliation> disputeReconciliation(@PathVariable String reconId,
                                                        @RequestBody(required = false) DisputeReconciliationRequest request) {
        return Result.ok(financeService.disputeReconciliation(currentTenant(), reconId,
                new DisputeReconciliationCommand(request != null && request.differenceItems() != null
                        ? request.differenceItems().toString()
                        : null)));
    }

    @PostMapping("/reconciliations/{reconId}/confirm")
    public Result<Reconciliation> confirmReconciliation(@PathVariable String reconId) {
        return Result.ok(financeService.confirmReconciliation(currentTenant(), reconId));
    }

    @PostMapping("/platform-bills/import")
    public Result<PlatformBill> importPlatformBill(@Valid @RequestBody ImportPlatformBillRequest request) {
        return Result.ok(financeService.importPlatformBill(currentTenant(), new ImportPlatformBillCommand(
                request.billId(), request.platform(), request.store(), request.billType(), request.period(),
                request.sellerSku(), request.marketplaceId(), request.sourceType(), request.sourceId(),
                request.currency(), request.amount(), request.rawData() != null ? request.rawData().toString() : null,
                request.occurredAt())));
    }

    @GetMapping("/platform-bills")
    public Result<List<PlatformBill>> listPlatformBills() {
        return Result.ok(financeService.listPlatformBills(currentTenant()));
    }

    @GetMapping("/platform-bills/{billId}")
    public Result<PlatformBill> getPlatformBill(@PathVariable String billId) {
        return Result.ok(financeService.getPlatformBill(currentTenant(), billId));
    }

    @PostMapping("/platform-settlements")
    public Result<PlatformSettlement> createPlatformSettlement(@Valid @RequestBody CreatePlatformSettlementRequest request) {
        return Result.ok(financeService.createPlatformSettlement(currentTenant(), new CreatePlatformSettlementCommand(
                request.settlementId(), request.platform(), request.store(), request.settlementType(),
                request.amount(), request.currency(), request.settlementDate())));
    }

    @GetMapping("/platform-settlements")
    public Result<List<PlatformSettlement>> listPlatformSettlements() {
        return Result.ok(financeService.listPlatformSettlements(currentTenant()));
    }

    @GetMapping("/platform-settlements/{settlementId}")
    public Result<PlatformSettlement> getPlatformSettlement(@PathVariable String settlementId) {
        return Result.ok(financeService.getPlatformSettlement(currentTenant(), settlementId));
    }

    @GetMapping("/platform-settlements/{settlementId}/bills")
    public Result<List<PlatformBill>> listPlatformBillsBySettlement(@PathVariable String settlementId) {
        return Result.ok(financeService.listPlatformBillsBySettlement(currentTenant(), settlementId));
    }

    @PostMapping("/platform-bills/{billId}/reconcile")
    public Result<PlatformBill> reconcilePlatformBill(@PathVariable String billId,
                                                      @RequestBody(required = false) ReconcilePlatformBillRequest request) {
        return Result.ok(financeService.reconcilePlatformBill(currentTenant(), billId,
                new ReconcilePlatformBillCommand(request != null ? request.settlementId() : null)));
    }

    @PostMapping("/platform-settlements/{settlementId}/receive")
    public Result<PlatformSettlement> receivePlatformSettlement(@PathVariable String settlementId,
                                                                @Valid @RequestBody ReceivePlatformSettlementRequest request) {
        return Result.ok(financeService.receivePlatformSettlement(currentTenant(), settlementId,
                new ReceivePlatformSettlementCommand(request.amount(), request.paymentMethod(), request.receivedAt())));
    }

    @PostMapping("/platform-settlements/{settlementId}/withdraw")
    public Result<PlatformSettlement> updatePlatformSettlementWithdrawal(@PathVariable String settlementId,
                                                                         @Valid @RequestBody UpdatePlatformSettlementWithdrawalRequest request) {
        return Result.ok(financeService.updatePlatformSettlementWithdrawal(currentTenant(), settlementId,
                new UpdatePlatformSettlementWithdrawalCommand(request.withdrawalStatus(), request.withdrawalReference())));
    }

    @PostMapping("/platform-settlements/{settlementId}/forex")
    public Result<PlatformSettlement> updatePlatformSettlementForex(@PathVariable String settlementId,
                                                                    @Valid @RequestBody UpdatePlatformSettlementForexRequest request) {
        return Result.ok(financeService.updatePlatformSettlementForex(currentTenant(), settlementId,
                new UpdatePlatformSettlementForexCommand(request.forexStatus(), request.forexRate())));
    }

    @PostMapping("/cost-events")
    public Result<CostEvent> recordCostEvent(@Valid @RequestBody RecordCostEventRequest request) {
        return Result.ok(financeService.recordCostEvent(currentTenant(), new RecordCostEventCommand(
                request.costType(), request.sourceType(), request.sourceId(), request.sellerSku(),
                request.marketplaceId(), request.currency(), request.amount(), request.occurredAt())));
    }

    @GetMapping("/cost-events")
    public Result<List<CostEvent>> listCostEvents() {
        return Result.ok(financeService.listCostEvents(currentTenant()));
    }

    @GetMapping("/cost-events/{costEventId}")
    public Result<CostEvent> getCostEvent(@PathVariable String costEventId) {
        return Result.ok(financeService.getCostEvent(currentTenant(), costEventId));
    }

    @GetMapping("/cost-events/by-sku")
    public Result<List<CostEvent>> listCostEventsBySku(@NotBlank String sellerSku) {
        return Result.ok(financeService.listCostEventsBySku(currentTenant(), sellerSku));
    }

    @GetMapping("/cost-events/by-source")
    public Result<List<CostEvent>> listCostEventsBySource(@NotBlank String sourceType, @NotBlank String sourceId) {
        return Result.ok(financeService.listCostEventsBySource(currentTenant(), sourceType, sourceId));
    }

    @PostMapping("/profit-statements")
    public Result<ProfitStatement> calculateProfit(@Valid @RequestBody CalculateProfitRequest request) {
        return Result.ok(financeService.calculateProfit(currentTenant(), new CalculateProfitCommand(
                request.sellerSku(), request.marketplaceId(), request.orderId(), request.revenue(), request.currency())));
    }

    @GetMapping("/profit-statements")
    public Result<List<ProfitStatement>> listProfitStatements() {
        return Result.ok(financeService.listProfitStatements(currentTenant()));
    }

    @GetMapping("/profit-statements/by-sku")
    public Result<List<ProfitStatement>> listProfitStatementsBySku(@NotBlank String sellerSku) {
        return Result.ok(financeService.listProfitStatementsBySku(currentTenant(), sellerSku));
    }

    @PostMapping("/forex-rates")
    public Result<ForexRate> saveForexRate(@Valid @RequestBody SaveForexRateRequest request) {
        return Result.ok(financeService.saveForexRate(currentTenant(), new SaveForexRateCommand(
                request.fromCurrency(), request.toCurrency(), request.rate(),
                request.effectiveDate(), request.source())));
    }

    @GetMapping("/forex-rates")
    public Result<List<ForexRate>> listForexRates() {
        return Result.ok(financeService.listForexRates(currentTenant()));
    }

    @GetMapping("/forex-rates/latest")
    public Result<ForexRate> getLatestForexRate(@NotBlank String fromCurrency, @NotBlank String toCurrency) {
        return Result.ok(financeService.getLatestForexRate(currentTenant(), fromCurrency, toCurrency).orElse(null));
    }

    @GetMapping("/forex-rates/as-of")
    public Result<ForexRate> getForexRateAsOf(@NotBlank String fromCurrency, @NotBlank String toCurrency,
                                              @NotNull LocalDate effectiveDate) {
        return Result.ok(financeService.getForexRateAsOf(currentTenant(), fromCurrency, toCurrency, effectiveDate).orElse(null));
    }

    @GetMapping("/forex-rates/history")
    public Result<List<ForexRate>> listForexRateHistory(@NotBlank String fromCurrency, @NotBlank String toCurrency) {
        return Result.ok(financeService.listForexRateHistory(currentTenant(), fromCurrency, toCurrency));
    }

    @GetMapping("/forex-rates/risk-check")
    public Result<ForexRiskAlert> checkForexRisk(@NotBlank String fromCurrency, @NotBlank String toCurrency) {
        return Result.ok(financeService.checkForexRisk(currentTenant(), fromCurrency, toCurrency));
    }

    @PostMapping("/forex-transactions")
    public Result<ForexTransaction> createForexTransaction(@Valid @RequestBody CreateForexTransactionRequest request) {
        return Result.ok(financeService.createForexTransaction(currentTenant(), new CreateForexTransactionCommand(
                request.fromCurrency(), request.toCurrency(), request.amount(), request.rate(),
                request.fee(), request.refType(), request.refId())));
    }

    @GetMapping("/forex-transactions")
    public Result<List<ForexTransaction>> listForexTransactions() {
        return Result.ok(financeService.listForexTransactions(currentTenant()));
    }

    @GetMapping("/forex-transactions/by-ref")
    public Result<List<ForexTransaction>> listForexTransactionsByRef(@NotBlank String refType, @NotBlank String refId) {
        return Result.ok(financeService.listForexTransactionsByRef(currentTenant(), refType, refId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "绉熸埛涓嶈兘涓虹┖");
        }
        return tenantId;
    }

    public record CreateReceivableRequest(@NotBlank String sourceType, @NotBlank String sourceId,
                                          @NotBlank String customerName, @NotBlank String currency,
                                          @Positive BigDecimal amount) {}

    public record ReceivePaymentRequest(@Positive BigDecimal amount, @NotBlank String paymentMethod) {}

    public record CreatePaymentRequestRequest(String requestId, String poId, String supplierId,
                                              @Positive BigDecimal amount, @NotBlank String currency,
                                              @NotBlank String requestType, @NotBlank String requestedBy,
                                              JsonNode approvalFlow) {}

    public record ApprovePaymentRequestRequest(@NotBlank String approverId, @Positive int approvalLevel,
                                               String comment, Instant approvedAt) {}

    public record PayPaymentRequestRequest(@NotBlank String paidBy, Instant paidAt) {}

    public record CreateWriteOffRequest(String writeoffId, @NotBlank String type, @NotBlank String refType,
                                        @NotBlank String refId, @Positive BigDecimal amount,
                                        @NotBlank String currency) {}

    public record ApproveWriteOffRequest(@NotBlank String approvedBy) {}

    public record CreateReconciliationRequest(String reconId, @NotBlank String type, @NotBlank String partyId,
                                              String partyName, @Pattern(regexp = "\\d{4}-\\d{2}") String period,
                                              @Positive BigDecimal payableAmount, BigDecimal paidAmount,
                                              JsonNode items) {}

    public record DisputeReconciliationRequest(JsonNode differenceItems) {}

    public record ImportPlatformBillRequest(String billId,
                                            @NotBlank String platform,
                                            @NotBlank String store,
                                            @NotBlank String billType,
                                            @Pattern(regexp = "\\d{4}-\\d{2}") String period,
                                            String sellerSku,
                                            String marketplaceId,
                                            String sourceType,
                                            String sourceId,
                                            @NotBlank String currency,
                                            @Positive BigDecimal amount,
                                            JsonNode rawData,
                                            Instant occurredAt) {}

    public record CreatePlatformSettlementRequest(String settlementId,
                                                  @NotBlank String platform,
                                                  @NotBlank String store,
                                                  @NotBlank String settlementType,
                                                  @Positive BigDecimal amount,
                                                  @NotBlank String currency,
                                                  @NotNull LocalDate settlementDate) {}

    public record ReconcilePlatformBillRequest(String settlementId) {}

    public record ReceivePlatformSettlementRequest(@Positive BigDecimal amount,
                                                   @NotBlank String paymentMethod,
                                                   Instant receivedAt) {}

    public record UpdatePlatformSettlementWithdrawalRequest(@NotBlank String withdrawalStatus,
                                                            String withdrawalReference) {}

    public record UpdatePlatformSettlementForexRequest(@NotBlank String forexStatus,
                                                       BigDecimal forexRate) {}

    public record RecordCostEventRequest(@NotBlank String costType, String sourceType, String sourceId,
                                         @NotBlank String sellerSku, String marketplaceId,
                                         @NotBlank String currency, @Positive BigDecimal amount,
                                         Instant occurredAt) {}

    public record CalculateProfitRequest(@NotBlank String sellerSku, String marketplaceId, String orderId,
                                         @Positive BigDecimal revenue, @NotBlank String currency) {}

    public record SaveForexRateRequest(@NotBlank String fromCurrency, @NotBlank String toCurrency,
                                       @Positive BigDecimal rate, @NotNull LocalDate effectiveDate,
                                       String source) {}

    public record CreateForexTransactionRequest(@NotBlank String fromCurrency, @NotBlank String toCurrency,
                                                @Positive BigDecimal amount, @Positive BigDecimal rate,
                                                BigDecimal fee, String refType, String refId) {}
}
