package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.common.exception.BizException;
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
import com.aidotnet.erp.fms.domain.ReceivableStatus;
import com.aidotnet.erp.fms.domain.WriteOff;
import com.aidotnet.erp.fms.infrastructure.FinanceStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 财务管理应用服务
 * <p>
 * 描述: 财务域核心服务，负责应收/应付管理、付款审批、核销、对账、
 *       平台账单导入、外汇管理、利润核算等业务逻辑。
 *       是整个ERP系统的财务数据中枢，支持AI成本归集能力。
 * </p>
 * <p>
 * 核心能力:
 *   1. 应收管理 - 创建/确认/收款应收单，支持部分收款
 *   2. 付款申请 - 创建/审批/支付/取消付款申请，多级审批流
 *   3. 核销管理 - 创建/审批核销单，关联付款申请自动核销
 *   4. 对账管理 - 创建/确认/争议对账单，供应商/平台对账
 *   5. 平台账单 - 导入平台账单，自动创建成本事件
 *   6. 外汇管理 - 记录汇率/外汇交易/汇率风险预警
 *   7. 利润核算 - 创建利润核算单，关联收入和成本
 *   8. 平台结算 - 记录平台结算数据
 * </p>
 * <p>
 * 业务规则:
 *   1. 应收单状态流转: DRAFT -> CONFIRMED -> PARTIALLY_PAID/PAID
 *   2. 付款金额不可超过应收金额
 *   3. 付款申请多级审批，全部审批通过后才可支付
 *   4. 核销单审批后自动更新付款申请核销状态
 *   5. 对账单确认后不可争议
 *   6. 平台账单导入时自动创建成本事件
 * </p>
 *
 * @author ERP系统
 * @see Receivable
 * @see PaymentRequest
 * @see FinanceStore
 */
@Service
public class FinanceService {

    private final FinanceStore financeStore;
    private final ObjectMapper objectMapper;

    public FinanceService(FinanceStore financeStore, ObjectMapper objectMapper) {
        this.financeStore = financeStore;
        this.objectMapper = objectMapper;
    }

    public Receivable createReceivable(String tenantId, CreateReceivableCommand command) {
        financeStore.findBySource(tenantId, command.sourceType(), command.sourceId()).ifPresent(existing -> {
            throw new BizException("RECEIVABLE_DUPLICATED", "Receivable already exists for source document");
        });
        Instant now = Instant.now();
        return financeStore.saveReceivable(new Receivable(UUID.randomUUID().toString(), tenantId, command.sourceType(),
                command.sourceId(), command.customerName(), command.currency(), command.amount(), BigDecimal.ZERO,
                ReceivableStatus.DRAFT, now, now));
    }

    public Receivable confirm(String tenantId, String receivableId) {
        Receivable receivable = getReceivable(tenantId, receivableId);
        if (receivable.status() != ReceivableStatus.DRAFT) {
            throw new BizException("RECEIVABLE_STATUS_INVALID", "Only draft receivables can be confirmed");
        }
        return update(receivable, receivable.paidAmount(), ReceivableStatus.CONFIRMED);
    }

    public Receivable receivePayment(String tenantId, String receivableId, ReceivePaymentCommand command) {
        Receivable receivable = getReceivable(tenantId, receivableId);
        if (receivable.status() != ReceivableStatus.CONFIRMED && receivable.status() != ReceivableStatus.PARTIALLY_PAID) {
            throw new BizException("RECEIVABLE_STATUS_INVALID", "Only confirmed receivables can receive payment");
        }
        BigDecimal nextPaid = receivable.paidAmount().add(command.amount());
        if (nextPaid.compareTo(receivable.amount()) > 0) {
            throw new BizException("PAYMENT_EXCEEDS_RECEIVABLE", "Payment amount exceeds receivable amount");
        }
        financeStore.appendPayment(new PaymentRecord(UUID.randomUUID().toString(), tenantId, receivableId, command.amount(),
                command.paymentMethod(), Instant.now()));
        ReceivableStatus status = nextPaid.compareTo(receivable.amount()) == 0 ? ReceivableStatus.PAID : ReceivableStatus.PARTIALLY_PAID;
        return update(receivable, nextPaid, status);
    }

    public List<Receivable> listReceivables(String tenantId) {
        return financeStore.listReceivables(tenantId);
    }

    public Receivable getReceivableBySource(String tenantId, String sourceType, String sourceId) {
        return financeStore.findBySource(tenantId, sourceType, sourceId)
                .orElseThrow(() -> new BizException("RECEIVABLE_NOT_FOUND", "Receivable does not exist"));
    }

    public List<PaymentRecord> listPayments(String tenantId, String receivableId) {
        getReceivable(tenantId, receivableId);
        return financeStore.listPayments(tenantId, receivableId);
    }

    public PaymentRequest createPaymentRequest(String tenantId, CreatePaymentRequestCommand command) {
        String requestId = hasText(command.requestId()) ? command.requestId().trim() : UUID.randomUUID().toString();
        if (financeStore.findPaymentRequest(tenantId, requestId).isPresent()) {
            throw new BizException("PAYMENT_REQUEST_DUPLICATED", "Payment request already exists");
        }
        Instant now = Instant.now();
        return financeStore.savePaymentRequest(new PaymentRequest(
                requestId,
                tenantId,
                trimToNull(command.poId()),
                trimToNull(command.supplierId()),
                command.amount(),
                command.currency().trim(),
                normalizePaymentRequestType(command.requestType()),
                "PENDING",
                command.requestedBy().trim(),
                normalizeApprovalFlow(command.approvalFlow()),
                null,
                null,
                "UNWRITTEN",
                BigDecimal.ZERO,
                now,
                now));
    }

    public List<PaymentRequest> listPaymentRequests(String tenantId) {
        return financeStore.listPaymentRequests(tenantId);
    }

    public List<PaymentRequest> listPaymentRequestsByPo(String tenantId, String poId) {
        return financeStore.listPaymentRequestsByPo(tenantId, poId);
    }

    public PaymentRequest getPaymentRequest(String tenantId, String requestId) {
        return financeStore.findPaymentRequest(tenantId, requestId)
                .orElseThrow(() -> new BizException("PAYMENT_REQUEST_NOT_FOUND", "Payment request does not exist"));
    }

    public List<PaymentApproval> listPaymentApprovals(String tenantId, String requestId) {
        getPaymentRequest(tenantId, requestId);
        return financeStore.listPaymentApprovals(tenantId, requestId);
    }

    public PaymentRequest approvePaymentRequest(String tenantId, String requestId, ApprovePaymentRequestCommand command) {
        PaymentRequest request = getPaymentRequest(tenantId, requestId);
        if (!"PENDING".equals(request.status())) {
            throw new BizException("PAYMENT_REQUEST_STATUS_INVALID", "Only pending payment requests can be approved");
        }
        ArrayNode approvalFlow = parseApprovalFlow(request.approvalFlow());
        int approvedCount = countApprovedSteps(approvalFlow);
        int expectedLevel = approvedCount + 1;
        if (command.approvalLevel() != expectedLevel || command.approvalLevel() > approvalFlow.size()) {
            throw new BizException("PAYMENT_REQUEST_APPROVAL_LEVEL_INVALID", "Payment request approval level is invalid");
        }
        if (financeStore.listPaymentApprovals(tenantId, requestId).stream()
                .anyMatch(approval -> approval.approvalLevel() == command.approvalLevel())) {
            throw new BizException("PAYMENT_REQUEST_APPROVAL_DUPLICATED", "Payment request approval already exists at this level");
        }
        Instant approvedAt = command.approvedAt() != null ? command.approvedAt() : Instant.now();
        financeStore.appendPaymentApproval(new PaymentApproval(
                UUID.randomUUID().toString(),
                tenantId,
                requestId,
                command.approverId().trim(),
                command.approvalLevel(),
                "APPROVED",
                trimToNull(command.comment()),
                approvedAt,
                approvedAt));
        ObjectNode step = ((ObjectNode) approvalFlow.get(command.approvalLevel() - 1)).deepCopy();
        step.put("approvalLevel", command.approvalLevel());
        step.put("status", "APPROVED");
        step.put("approverId", command.approverId().trim());
        if (hasText(command.comment())) {
            step.put("comment", command.comment().trim());
        }
        step.put("approvedAt", approvedAt.toString());
        approvalFlow.set(command.approvalLevel() - 1, step);
        String nextStatus = countApprovedSteps(approvalFlow) == approvalFlow.size() ? "APPROVED" : "PENDING";
        return financeStore.savePaymentRequest(new PaymentRequest(
                request.requestId(),
                request.tenantId(),
                request.poId(),
                request.supplierId(),
                request.amount(),
                request.currency(),
                request.requestType(),
                nextStatus,
                request.requestedBy(),
                writeJson(approvalFlow),
                request.paidBy(),
                request.paidAt(),
                request.writeoffStatus(),
                request.writeoffAmount(),
                request.createdAt(),
                Instant.now()));
    }

    public PaymentRequest payPaymentRequest(String tenantId, String requestId, PayPaymentRequestCommand command) {
        PaymentRequest request = getPaymentRequest(tenantId, requestId);
        if ("PAID".equals(request.status())) {
            throw new BizException("PAYMENT_REQUEST_ALREADY_PAID", "Payment request already paid");
        }
        if ("CANCELLED".equals(request.status())) {
            throw new BizException("PAYMENT_REQUEST_CANCELLED", "Cancelled payment request cannot be paid");
        }
        if (!"APPROVED".equals(request.status())) {
            throw new BizException("PAYMENT_REQUEST_STATUS_INVALID", "Only approved payment requests can be paid");
        }
        return financeStore.savePaymentRequest(new PaymentRequest(
                request.requestId(),
                request.tenantId(),
                request.poId(),
                request.supplierId(),
                request.amount(),
                request.currency(),
                request.requestType(),
                "PAID",
                request.requestedBy(),
                request.approvalFlow(),
                command.paidBy().trim(),
                command.paidAt() != null ? command.paidAt() : Instant.now(),
                request.writeoffStatus(),
                request.writeoffAmount(),
                request.createdAt(),
                Instant.now()));
    }

    public PaymentRequest cancelPaymentRequest(String tenantId, String requestId) {
        PaymentRequest request = getPaymentRequest(tenantId, requestId);
        if ("PAID".equals(request.status())) {
            throw new BizException("PAYMENT_REQUEST_ALREADY_PAID", "Paid payment request cannot be cancelled");
        }
        if ("CANCELLED".equals(request.status())) {
            throw new BizException("PAYMENT_REQUEST_ALREADY_CANCELLED", "Payment request already cancelled");
        }
        return financeStore.savePaymentRequest(new PaymentRequest(
                request.requestId(),
                request.tenantId(),
                request.poId(),
                request.supplierId(),
                request.amount(),
                request.currency(),
                request.requestType(),
                "CANCELLED",
                request.requestedBy(),
                request.approvalFlow(),
                request.paidBy(),
                request.paidAt(),
                request.writeoffStatus(),
                request.writeoffAmount(),
                request.createdAt(),
                Instant.now()));
    }

    public WriteOff createWriteOff(String tenantId, CreateWriteOffCommand command) {
        String writeoffId = hasText(command.writeoffId()) ? command.writeoffId().trim() : UUID.randomUUID().toString();
        if (financeStore.findWriteOff(tenantId, writeoffId).isPresent()) {
            throw new BizException("WRITE_OFF_DUPLICATED", "Write-off already exists");
        }
        validateWriteOffType(command.type());
        String refType = normalizeReferenceType(command.refType());
        if ("PAYMENT_REQUEST".equals(refType)) {
            PaymentRequest paymentRequest = getPaymentRequest(tenantId, command.refId());
            if (!"PAID".equals(paymentRequest.status())) {
                throw new BizException("WRITE_OFF_REF_STATUS_INVALID", "Only paid payment requests can be written off");
            }
            if ("FULL".equals(paymentRequest.writeoffStatus())) {
                throw new BizException("WRITE_OFF_REF_ALREADY_COMPLETED", "Payment request is already fully written off");
            }
        }
        Instant now = Instant.now();
        return financeStore.saveWriteOff(new WriteOff(
                writeoffId,
                tenantId,
                command.type().trim().toUpperCase(Locale.ROOT),
                refType,
                command.refId().trim(),
                command.amount(),
                command.currency().trim(),
                "PENDING",
                null,
                now,
                now));
    }

    public List<WriteOff> listWriteOffs(String tenantId) {
        return financeStore.listWriteOffs(tenantId);
    }

    public WriteOff getWriteOff(String tenantId, String writeoffId) {
        return financeStore.findWriteOff(tenantId, writeoffId)
                .orElseThrow(() -> new BizException("WRITE_OFF_NOT_FOUND", "Write-off does not exist"));
    }

    public List<WriteOff> listWriteOffsByRef(String tenantId, String refType, String refId) {
        return financeStore.listWriteOffsByRef(tenantId, normalizeReferenceType(refType), refId);
    }

    public WriteOff approveWriteOff(String tenantId, String writeoffId, ApproveWriteOffCommand command) {
        WriteOff writeOff = getWriteOff(tenantId, writeoffId);
        if (!"PENDING".equals(writeOff.status())) {
            throw new BizException("WRITE_OFF_STATUS_INVALID", "Only pending write-offs can be approved");
        }
        WriteOff approved = financeStore.saveWriteOff(new WriteOff(
                writeOff.writeoffId(),
                writeOff.tenantId(),
                writeOff.type(),
                writeOff.refType(),
                writeOff.refId(),
                writeOff.amount(),
                writeOff.currency(),
                "APPROVED",
                command.approvedBy().trim(),
                writeOff.createdAt(),
                Instant.now()));
        if ("PAYMENT_REQUEST".equals(writeOff.refType())) {
            applyWriteOffToPaymentRequest(tenantId, writeOff.refId(), writeOff.amount());
        }
        return approved;
    }

    public Reconciliation createReconciliation(String tenantId, CreateReconciliationCommand command) {
        String reconId = hasText(command.reconId()) ? command.reconId().trim() : UUID.randomUUID().toString();
        if (financeStore.findReconciliation(tenantId, reconId).isPresent()) {
            throw new BizException("RECONCILIATION_DUPLICATED", "Reconciliation already exists");
        }
        BigDecimal paidAmount = command.paidAmount() != null ? command.paidAmount() : BigDecimal.ZERO;
        if (paidAmount.compareTo(BigDecimal.ZERO) < 0 || paidAmount.compareTo(command.payableAmount()) > 0) {
            throw new BizException("RECONCILIATION_AMOUNT_INVALID", "Reconciliation paid amount is invalid");
        }
        if (!hasText(command.items())) {
            throw new BizException("RECONCILIATION_ITEMS_REQUIRED", "Reconciliation items are required");
        }
        BigDecimal balance = command.payableAmount().subtract(paidAmount);
        Instant now = Instant.now();
        return financeStore.saveReconciliation(new Reconciliation(
                reconId,
                tenantId,
                normalizeReconciliationType(command.type()),
                command.partyId().trim(),
                trimToNull(command.partyName()),
                command.period().trim(),
                command.payableAmount(),
                paidAmount,
                balance,
                "PENDING",
                defaultJson(command.items()),
                null,
                now,
                now));
    }

    public List<Reconciliation> listReconciliations(String tenantId) {
        return financeStore.listReconciliations(tenantId);
    }

    public Reconciliation getReconciliation(String tenantId, String reconId) {
        return financeStore.findReconciliation(tenantId, reconId)
                .orElseThrow(() -> new BizException("RECONCILIATION_NOT_FOUND", "Reconciliation does not exist"));
    }

    public List<Reconciliation> listReconciliationsByTypeAndParty(String tenantId, String type, String partyId) {
        return financeStore.listReconciliationsByTypeAndParty(tenantId, normalizeReconciliationType(type), partyId);
    }

    public Reconciliation disputeReconciliation(String tenantId, String reconId, DisputeReconciliationCommand command) {
        Reconciliation reconciliation = getReconciliation(tenantId, reconId);
        if ("RECONCILED".equals(reconciliation.status())) {
            throw new BizException("RECONCILIATION_ALREADY_CONFIRMED", "Confirmed reconciliation cannot be disputed");
        }
        if (!hasText(command.differenceItems())) {
            throw new BizException("RECONCILIATION_DIFFERENCE_REQUIRED", "Difference items are required");
        }
        return financeStore.saveReconciliation(new Reconciliation(
                reconciliation.reconId(),
                reconciliation.tenantId(),
                reconciliation.type(),
                reconciliation.partyId(),
                reconciliation.partyName(),
                reconciliation.period(),
                reconciliation.payableAmount(),
                reconciliation.paidAmount(),
                reconciliation.balance(),
                "DISPUTED",
                reconciliation.items(),
                command.differenceItems(),
                reconciliation.createdAt(),
                Instant.now()));
    }

    public Reconciliation confirmReconciliation(String tenantId, String reconId) {
        Reconciliation reconciliation = getReconciliation(tenantId, reconId);
        if ("RECONCILED".equals(reconciliation.status())) {
            throw new BizException("RECONCILIATION_ALREADY_CONFIRMED", "Reconciliation already confirmed");
        }
        return financeStore.saveReconciliation(new Reconciliation(
                reconciliation.reconId(),
                reconciliation.tenantId(),
                reconciliation.type(),
                reconciliation.partyId(),
                reconciliation.partyName(),
                reconciliation.period(),
                reconciliation.payableAmount(),
                reconciliation.paidAmount(),
                reconciliation.balance(),
                "RECONCILED",
                reconciliation.items(),
                reconciliation.differenceItems(),
                reconciliation.createdAt(),
                Instant.now()));
    }

    public PlatformBill importPlatformBill(String tenantId, ImportPlatformBillCommand command) {
        String billId = hasText(command.billId()) ? command.billId().trim() : UUID.randomUUID().toString();
        if (financeStore.findPlatformBill(tenantId, billId).isPresent()) {
            throw new BizException("PLATFORM_BILL_DUPLICATED", "Platform bill already exists");
        }
        String normalizedBillType = normalizePlatformBillType(command.billType());
        Instant now = Instant.now();
        PlatformBill platformBill = new PlatformBill(
                billId,
                tenantId,
                command.platform().trim(),
                command.store().trim(),
                normalizedBillType,
                command.period().trim(),
                trimToNull(command.sellerSku()),
                trimToNull(command.marketplaceId()),
                hasText(command.sourceType()) ? command.sourceType().trim() : "PLATFORM_STATEMENT_LINE",
                trimToNull(command.sourceId()),
                command.currency().trim(),
                command.amount(),
                trimToNull(command.rawData()),
                "IMPORTED",
                null,
                now,
                now);
        financeStore.savePlatformBill(platformBill);
        financeStore.saveCostEvent(new CostEvent(
                UUID.randomUUID().toString(),
                tenantId,
                mapPlatformBillCostType(normalizedBillType),
                "PLATFORM_BILL",
                billId,
                trimToNull(command.sellerSku()),
                platformBill.store(),
                platformBill.platform(),
                trimToNull(command.marketplaceId()),
                command.currency().trim(),
                command.amount(),
                command.occurredAt() != null ? command.occurredAt() : now,
                now));
        return platformBill;
    }

    public List<PlatformBill> listPlatformBills(String tenantId) {
        return financeStore.listPlatformBills(tenantId);
    }

    public PlatformBill getPlatformBill(String tenantId, String billId) {
        return financeStore.findPlatformBill(tenantId, billId)
                .orElseThrow(() -> new BizException("PLATFORM_BILL_NOT_FOUND", "Platform bill does not exist"));
    }

    @Transactional
    public PlatformSettlement createPlatformSettlement(String tenantId, CreatePlatformSettlementCommand command) {
        if (command.settlementDate() == null) {
            throw new BizException("PLATFORM_SETTLEMENT_DATE_REQUIRED", "Platform settlement date is required");
        }
        String settlementId = hasText(command.settlementId()) ? command.settlementId().trim() : UUID.randomUUID().toString();
        if (financeStore.findPlatformSettlement(tenantId, settlementId).isPresent()) {
            throw new BizException("PLATFORM_SETTLEMENT_DUPLICATED", "Platform settlement already exists");
        }
        Instant now = Instant.now();
        PlatformSettlement settlement = new PlatformSettlement(
                settlementId,
                tenantId,
                command.platform().trim(),
                command.store().trim(),
                normalizePlatformSettlementType(command.settlementType()),
                command.amount(),
                BigDecimal.ZERO,
                0,
                BigDecimal.ZERO,
                command.currency().trim(),
                command.settlementDate(),
                "PENDING",
                "UNREQUESTED",
                null,
                "UNSETTLED",
                null,
                null,
                now,
                now);
        financeStore.savePlatformSettlement(settlement);
        ensureSettlementReceivable(settlement, BigDecimal.ZERO, now);
        return settlement;
    }

    public List<PlatformSettlement> listPlatformSettlements(String tenantId) {
        return financeStore.listPlatformSettlements(tenantId);
    }

    public PlatformSettlement getPlatformSettlement(String tenantId, String settlementId) {
        return financeStore.findPlatformSettlement(tenantId, settlementId)
                .orElseThrow(() -> new BizException("PLATFORM_SETTLEMENT_NOT_FOUND", "Platform settlement does not exist"));
    }

    public List<PlatformBill> listPlatformBillsBySettlement(String tenantId, String settlementId) {
        getPlatformSettlement(tenantId, settlementId);
        return financeStore.listPlatformBillsBySettlementId(tenantId, settlementId);
    }

    @Transactional
    public PlatformBill reconcilePlatformBill(String tenantId, String billId, ReconcilePlatformBillCommand command) {
        PlatformBill platformBill = getPlatformBill(tenantId, billId);
        if (!hasText(command.settlementId())) {
            throw new BizException("PLATFORM_SETTLEMENT_REQUIRED", "Platform settlement id is required");
        }
        if ("SETTLED".equals(platformBill.status())) {
            throw new BizException("PLATFORM_BILL_STATUS_INVALID", "Settled platform bill cannot be reconciled again");
        }
        if ("RECONCILED".equals(platformBill.status())) {
            throw new BizException("PLATFORM_BILL_ALREADY_RECONCILED", "Platform bill already reconciled");
        }
        if (!"IMPORTED".equals(platformBill.status())) {
            throw new BizException("PLATFORM_BILL_STATUS_INVALID", "Only imported platform bills can be reconciled");
        }
        String settlementId = command.settlementId().trim();
        if (hasText(platformBill.settlementId()) && !settlementId.equals(platformBill.settlementId())) {
            throw new BizException("PLATFORM_BILL_SETTLEMENT_CONFLICT", "Platform bill is already linked to another settlement");
        }
        PlatformSettlement settlement = getPlatformSettlement(tenantId, settlementId);
        if ("PARTIALLY_RECEIVED".equals(settlement.status()) || "RECEIVED".equals(settlement.status())) {
            throw new BizException("PLATFORM_SETTLEMENT_STATUS_INVALID", "Received platform settlements cannot accept new bills");
        }
        validatePlatformSettlementBillConsistency(settlement, platformBill);
        BigDecimal projectedReconciledAmount = settlement.reconciledAmount().add(platformBill.amount());
        if (projectedReconciledAmount.compareTo(settlement.amount()) > 0) {
            throw new BizException("PLATFORM_SETTLEMENT_AMOUNT_EXCEEDED", "Reconciled amount exceeds settlement amount");
        }
        PlatformBill reconciledBill = financeStore.savePlatformBill(new PlatformBill(
                platformBill.billId(),
                platformBill.tenantId(),
                platformBill.platform(),
                platformBill.store(),
                platformBill.billType(),
                platformBill.period(),
                platformBill.sellerSku(),
                platformBill.marketplaceId(),
                platformBill.sourceType(),
                platformBill.sourceId(),
                platformBill.currency(),
                platformBill.amount(),
                platformBill.rawData(),
                "RECONCILED",
                settlementId,
                platformBill.createdAt(),
                Instant.now()));
        refreshPlatformSettlement(settlement, settlement.receivedAmount(), settlement.receivedAt());
        return reconciledBill;
    }

    @Transactional
    public PlatformSettlement receivePlatformSettlement(String tenantId, String settlementId, ReceivePlatformSettlementCommand command) {
        PlatformSettlement settlement = getPlatformSettlement(tenantId, settlementId);
        if (!"RECONCILED".equals(settlement.status()) && !"PARTIALLY_RECEIVED".equals(settlement.status())) {
            throw new BizException("PLATFORM_SETTLEMENT_STATUS_INVALID", "Only reconciled settlements can receive remittance");
        }
        if (settlement.linkedBillCount() == 0) {
            throw new BizException("PLATFORM_SETTLEMENT_EMPTY", "Platform settlement does not contain linked bills");
        }
        BigDecimal nextReceivedAmount = settlement.receivedAmount().add(command.amount());
        if (nextReceivedAmount.compareTo(settlement.amount()) > 0) {
            throw new BizException("PLATFORM_SETTLEMENT_RECEIVED_EXCEEDED", "Received amount exceeds settlement amount");
        }
        Instant receivedAt = command.receivedAt() != null ? command.receivedAt() : Instant.now();
        PlatformSettlement updatedSettlement = refreshPlatformSettlement(settlement, nextReceivedAmount, receivedAt);
        Receivable receivable = ensureSettlementReceivable(updatedSettlement, nextReceivedAmount, receivedAt);
        financeStore.appendPayment(new PaymentRecord(
                UUID.randomUUID().toString(),
                tenantId,
                receivable.receivableId(),
                command.amount(),
                command.paymentMethod().trim(),
                receivedAt));
        if ("RECEIVED".equals(updatedSettlement.status())) {
            markSettlementBillsAsSettled(tenantId, updatedSettlement.settlementId(), receivedAt);
        }
        return updatedSettlement;
    }

    public PlatformSettlement updatePlatformSettlementWithdrawal(String tenantId, String settlementId,
                                                                UpdatePlatformSettlementWithdrawalCommand command) {
        PlatformSettlement settlement = getPlatformSettlement(tenantId, settlementId);
        if (!"RECEIVED".equals(settlement.status())) {
            throw new BizException("PLATFORM_SETTLEMENT_STATUS_INVALID", "Only received settlements can start withdrawal");
        }
        String nextStatus = normalizeWithdrawalStatus(command.withdrawalStatus());
        String currentStatus = settlement.withdrawalStatus();
        if ("REQUESTED".equals(nextStatus)) {
            if (!"UNREQUESTED".equals(currentStatus)) {
                throw new BizException("PLATFORM_SETTLEMENT_WITHDRAWAL_STATUS_INVALID", "Withdrawal can only be requested once");
            }
            if (!hasText(command.withdrawalReference())) {
                throw new BizException("PLATFORM_SETTLEMENT_WITHDRAWAL_REFERENCE_REQUIRED", "Withdrawal reference is required");
            }
        } else {
            if (!"REQUESTED".equals(currentStatus)) {
                throw new BizException("PLATFORM_SETTLEMENT_WITHDRAWAL_STATUS_INVALID", "Only requested withdrawals can be completed");
            }
            if (!hasText(command.withdrawalReference()) && !hasText(settlement.withdrawalReference())) {
                throw new BizException("PLATFORM_SETTLEMENT_WITHDRAWAL_REFERENCE_REQUIRED", "Withdrawal reference is required");
            }
        }
        return financeStore.savePlatformSettlement(new PlatformSettlement(
                settlement.settlementId(),
                settlement.tenantId(),
                settlement.platform(),
                settlement.store(),
                settlement.settlementType(),
                settlement.amount(),
                settlement.reconciledAmount(),
                settlement.linkedBillCount(),
                settlement.receivedAmount(),
                settlement.currency(),
                settlement.settlementDate(),
                settlement.status(),
                nextStatus,
                hasText(command.withdrawalReference()) ? command.withdrawalReference().trim() : settlement.withdrawalReference(),
                settlement.forexStatus(),
                settlement.forexRate(),
                settlement.receivedAt(),
                settlement.createdAt(),
                Instant.now()));
    }

    public PlatformSettlement updatePlatformSettlementForex(String tenantId, String settlementId,
                                                           UpdatePlatformSettlementForexCommand command) {
        PlatformSettlement settlement = getPlatformSettlement(tenantId, settlementId);
        if (!"RECEIVED".equals(settlement.status())) {
            throw new BizException("PLATFORM_SETTLEMENT_STATUS_INVALID", "Only received settlements can process forex");
        }
        String nextStatus = normalizeForexStatus(command.forexStatus());
        if ("PROCESSING".equals(nextStatus)) {
            if (!"UNSETTLED".equals(settlement.forexStatus())) {
                throw new BizException("PLATFORM_SETTLEMENT_FOREX_STATUS_INVALID", "Forex processing can only start once");
            }
            if ("UNREQUESTED".equals(settlement.withdrawalStatus())) {
                throw new BizException("PLATFORM_SETTLEMENT_FOREX_STATUS_INVALID", "Withdrawal must be requested before forex processing");
            }
        } else {
            if (!"PROCESSING".equals(settlement.forexStatus())) {
                throw new BizException("PLATFORM_SETTLEMENT_FOREX_STATUS_INVALID", "Only processing forex transactions can be settled");
            }
            if (!"COMPLETED".equals(settlement.withdrawalStatus())) {
                throw new BizException("PLATFORM_SETTLEMENT_FOREX_STATUS_INVALID", "Withdrawal must be completed before forex settlement");
            }
            if (command.forexRate() == null || command.forexRate().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("PLATFORM_SETTLEMENT_FOREX_RATE_INVALID", "Forex rate is invalid");
            }
        }
        return financeStore.savePlatformSettlement(new PlatformSettlement(
                settlement.settlementId(),
                settlement.tenantId(),
                settlement.platform(),
                settlement.store(),
                settlement.settlementType(),
                settlement.amount(),
                settlement.reconciledAmount(),
                settlement.linkedBillCount(),
                settlement.receivedAmount(),
                settlement.currency(),
                settlement.settlementDate(),
                settlement.status(),
                settlement.withdrawalStatus(),
                settlement.withdrawalReference(),
                nextStatus,
                command.forexRate() != null ? command.forexRate() : settlement.forexRate(),
                settlement.receivedAt(),
                settlement.createdAt(),
                Instant.now()));
    }

    public CostEvent recordCostEvent(String tenantId, RecordCostEventCommand command) {
        validateCostType(command.costType());
        Instant occurredAt = command.occurredAt() != null ? command.occurredAt() : Instant.now();
        return financeStore.saveCostEvent(new CostEvent(UUID.randomUUID().toString(), tenantId,
                command.costType(), command.sourceType(), command.sourceId(), command.sellerSku(),
                trimToNull(command.storeId()), trimToNull(command.channelCode()),
                command.marketplaceId(), command.currency(), command.amount(), occurredAt, Instant.now()));
    }

    public List<CostEvent> listCostEvents(String tenantId) {
        return financeStore.listCostEvents(tenantId);
    }

    public CostEvent getCostEvent(String tenantId, String costEventId) {
        return financeStore.findCostEvent(tenantId, costEventId)
                .orElseThrow(() -> new BizException("COST_EVENT_NOT_FOUND", "Cost event does not exist"));
    }

    public List<CostEvent> listCostEventsBySku(String tenantId, String sellerSku) {
        return financeStore.listCostEventsBySku(tenantId, sellerSku);
    }

    public List<CostEvent> listCostEventsBySource(String tenantId, String sourceType, String sourceId) {
        return financeStore.listCostEventsBySource(tenantId, sourceType, sourceId);
    }

    public ProfitStatement calculateProfit(String tenantId, CalculateProfitCommand command) {
        List<CostEvent> costs = financeStore.listCostEventsBySku(tenantId, command.sellerSku()).stream()
                .filter(cost -> command.orderId() == null || command.orderId().isBlank() || command.orderId().equals(cost.sourceId()))
                .toList();
        BigDecimal productCost = sumByType(costs, CostEvent.CostType.PRODUCT_COST);
        BigDecimal shippingCost = sumByType(costs, CostEvent.CostType.SHIPPING_COST);
        BigDecimal fbaFee = sumByType(costs, CostEvent.CostType.FBA_FEE);
        BigDecimal commission = sumByType(costs, CostEvent.CostType.COMMISSION);
        BigDecimal advertisingCost = sumByType(costs, CostEvent.CostType.ADVERTISING);
        BigDecimal otherCost = sumByType(costs, CostEvent.CostType.OTHER)
                .add(sumByType(costs, CostEvent.CostType.RETURN_COST))
                .add(sumByType(costs, CostEvent.CostType.STORAGE_FEE));
        BigDecimal totalCost = productCost.add(shippingCost).add(fbaFee).add(commission).add(advertisingCost).add(otherCost);
        BigDecimal grossProfit = command.revenue().subtract(totalCost);
        BigDecimal grossMargin = command.revenue().compareTo(BigDecimal.ZERO) > 0
                ? grossProfit.divide(command.revenue(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        return financeStore.saveProfitStatement(new ProfitStatement(UUID.randomUUID().toString(), tenantId,
                command.sellerSku(), command.marketplaceId(), command.orderId(), command.revenue(),
                productCost, shippingCost, fbaFee, commission, advertisingCost, otherCost,
                totalCost, grossProfit, grossMargin, command.currency(), Instant.now()));
    }

    public List<ProfitStatement> listProfitStatements(String tenantId) {
        return financeStore.listProfitStatements(tenantId);
    }

    public List<ProfitStatement> listProfitStatementsBySku(String tenantId, String sellerSku) {
        return financeStore.listProfitStatementsBySku(tenantId, sellerSku);
    }

    public Receivable getReceivable(String tenantId, String receivableId) {
        return financeStore.findReceivable(tenantId, receivableId)
                .orElseThrow(() -> new BizException("RECEIVABLE_NOT_FOUND", "Receivable does not exist"));
    }

    private PlatformSettlement refreshPlatformSettlement(PlatformSettlement settlement, BigDecimal receivedAmount, Instant receivedAt) {
        List<PlatformBill> linkedBills = financeStore.listPlatformBillsBySettlementId(settlement.tenantId(), settlement.settlementId());
        BigDecimal reconciledAmount = linkedBills.stream()
                .map(PlatformBill::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int linkedBillCount = linkedBills.size();
        String status = derivePlatformSettlementStatus(settlement.amount(), reconciledAmount, linkedBillCount, receivedAmount);
        return financeStore.savePlatformSettlement(new PlatformSettlement(
                settlement.settlementId(),
                settlement.tenantId(),
                settlement.platform(),
                settlement.store(),
                settlement.settlementType(),
                settlement.amount(),
                reconciledAmount,
                linkedBillCount,
                receivedAmount,
                settlement.currency(),
                settlement.settlementDate(),
                status,
                settlement.withdrawalStatus(),
                settlement.withdrawalReference(),
                settlement.forexStatus(),
                settlement.forexRate(),
                receivedAmount.compareTo(BigDecimal.ZERO) > 0 ? receivedAt : null,
                settlement.createdAt(),
                Instant.now()));
    }

    private void validatePlatformSettlementBillConsistency(PlatformSettlement settlement, PlatformBill bill) {
        if (!settlement.platform().equalsIgnoreCase(bill.platform())) {
            throw new BizException("PLATFORM_SETTLEMENT_PLATFORM_MISMATCH", "Platform bill platform does not match settlement");
        }
        if (!settlement.store().equalsIgnoreCase(bill.store())) {
            throw new BizException("PLATFORM_SETTLEMENT_STORE_MISMATCH", "Platform bill store does not match settlement");
        }
        if (!settlement.currency().equalsIgnoreCase(bill.currency())) {
            throw new BizException("PLATFORM_SETTLEMENT_CURRENCY_MISMATCH", "Platform bill currency does not match settlement");
        }
    }

    private void markSettlementBillsAsSettled(String tenantId, String settlementId, Instant settledAt) {
        for (PlatformBill bill : financeStore.listPlatformBillsBySettlementId(tenantId, settlementId)) {
            financeStore.savePlatformBill(new PlatformBill(
                    bill.billId(),
                    bill.tenantId(),
                    bill.platform(),
                    bill.store(),
                    bill.billType(),
                    bill.period(),
                    bill.sellerSku(),
                    bill.marketplaceId(),
                    bill.sourceType(),
                    bill.sourceId(),
                    bill.currency(),
                    bill.amount(),
                    bill.rawData(),
                    "SETTLED",
                    bill.settlementId(),
                    bill.createdAt(),
                    settledAt));
        }
    }

    private Receivable ensureSettlementReceivable(PlatformSettlement settlement, BigDecimal paidAmount, Instant now) {
        ReceivableStatus status = deriveReceivableStatus(settlement.amount(), paidAmount);
        return financeStore.findBySource(settlement.tenantId(), "PLATFORM_SETTLEMENT", settlement.settlementId())
                .map(existing -> financeStore.saveReceivable(new Receivable(
                        existing.receivableId(),
                        existing.tenantId(),
                        existing.sourceType(),
                        existing.sourceId(),
                        settlement.platform() + "/" + settlement.store(),
                        settlement.currency(),
                        settlement.amount(),
                        paidAmount,
                        status,
                        existing.createdAt(),
                        now)))
                .orElseGet(() -> financeStore.saveReceivable(new Receivable(
                        UUID.randomUUID().toString(),
                        settlement.tenantId(),
                        "PLATFORM_SETTLEMENT",
                        settlement.settlementId(),
                        settlement.platform() + "/" + settlement.store(),
                        settlement.currency(),
                        settlement.amount(),
                        paidAmount,
                        status,
                        now,
                        now)));
    }

    private String derivePlatformSettlementStatus(BigDecimal amount, BigDecimal reconciledAmount,
                                                  int linkedBillCount, BigDecimal receivedAmount) {
        if (receivedAmount.compareTo(amount) == 0) {
            return "RECEIVED";
        }
        if (receivedAmount.compareTo(BigDecimal.ZERO) > 0) {
            return "PARTIALLY_RECEIVED";
        }
        if (linkedBillCount > 0 && reconciledAmount.compareTo(amount) == 0) {
            return "RECONCILED";
        }
        if (reconciledAmount.compareTo(BigDecimal.ZERO) > 0) {
            return "RECONCILING";
        }
        return "PENDING";
    }

    private ReceivableStatus deriveReceivableStatus(BigDecimal amount, BigDecimal paidAmount) {
        if (paidAmount.compareTo(amount) == 0) {
            return ReceivableStatus.PAID;
        }
        if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            return ReceivableStatus.PARTIALLY_PAID;
        }
        return ReceivableStatus.CONFIRMED;
    }

    private void applyWriteOffToPaymentRequest(String tenantId, String requestId, BigDecimal amount) {
        PaymentRequest paymentRequest = getPaymentRequest(tenantId, requestId);
        if (!"PAID".equals(paymentRequest.status())) {
            throw new BizException("WRITE_OFF_REF_STATUS_INVALID", "Only paid payment requests can be written off");
        }
        BigDecimal nextWriteoffAmount = paymentRequest.writeoffAmount().add(amount);
        if (nextWriteoffAmount.compareTo(paymentRequest.amount()) > 0) {
            throw new BizException("WRITE_OFF_AMOUNT_EXCEEDS_PAYMENT_REQUEST", "Write-off amount exceeds payment request amount");
        }
        String writeoffStatus = nextWriteoffAmount.compareTo(paymentRequest.amount()) == 0 ? "FULL" : "PARTIAL";
        financeStore.savePaymentRequest(new PaymentRequest(
                paymentRequest.requestId(),
                paymentRequest.tenantId(),
                paymentRequest.poId(),
                paymentRequest.supplierId(),
                paymentRequest.amount(),
                paymentRequest.currency(),
                paymentRequest.requestType(),
                paymentRequest.status(),
                paymentRequest.requestedBy(),
                paymentRequest.approvalFlow(),
                paymentRequest.paidBy(),
                paymentRequest.paidAt(),
                writeoffStatus,
                nextWriteoffAmount,
                paymentRequest.createdAt(),
                Instant.now()));
    }

    private void validateCostType(String costType) {
        try {
            CostEvent.CostType.valueOf(costType);
        } catch (Exception ex) {
            throw new BizException("COST_TYPE_INVALID", "Cost type is invalid");
        }
    }

    private void validateWriteOffType(String type) {
        String normalized = type.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "INBOUND", "EXCEPTION", "SUPPLIER_REFUND", "REPAYMENT" -> {
            }
            default -> throw new BizException("WRITE_OFF_TYPE_INVALID", "Write-off type is invalid");
        }
    }

    private String normalizePaymentRequestType(String requestType) {
        String normalized = requestType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "PROCUREMENT", "LOGISTICS", "WAREHOUSE" -> normalized;
            default -> throw new BizException("PAYMENT_REQUEST_TYPE_INVALID", "Payment request type is invalid");
        };
    }

    private String normalizeApprovalFlow(String approvalFlow) {
        if (!hasText(approvalFlow)) {
            throw new BizException("PAYMENT_REQUEST_APPROVAL_FLOW_REQUIRED", "Payment request approval flow is required");
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(approvalFlow);
            if (!(jsonNode instanceof ArrayNode arrayNode) || arrayNode.isEmpty()) {
                throw new BizException("PAYMENT_REQUEST_APPROVAL_FLOW_INVALID", "Payment request approval flow is invalid");
            }
            ArrayNode normalized = objectMapper.createArrayNode();
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode step = arrayNode.get(i);
                ObjectNode stepNode = step != null && step.isObject() ? (ObjectNode) step.deepCopy() : objectMapper.createObjectNode();
                if (!hasText(stepNode.path("node").asText(null))) {
                    if (step != null && step.isTextual()) {
                        stepNode.put("node", step.asText());
                    } else {
                        stepNode.put("node", "LEVEL-" + (i + 1));
                    }
                }
                stepNode.put("approvalLevel", i + 1);
                stepNode.put("status", "APPROVED".equalsIgnoreCase(stepNode.path("status").asText()) ? "APPROVED" : "PENDING");
                normalized.add(stepNode);
            }
            if (countApprovedSteps(normalized) == normalized.size()) {
                throw new BizException("PAYMENT_REQUEST_APPROVAL_FLOW_INVALID", "Payment request approval flow cannot be pre-approved");
            }
            return writeJson(normalized);
        } catch (JsonProcessingException ex) {
            throw new BizException("PAYMENT_REQUEST_APPROVAL_FLOW_INVALID", "Payment request approval flow is invalid");
        }
    }

    private String normalizeReferenceType(String refType) {
        String normalized = refType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "PAYMENT_REQUEST", "PURCHASE_ORDER", "SHIPMENT", "REFUND" -> normalized;
            default -> throw new BizException("WRITE_OFF_REF_TYPE_INVALID", "Write-off reference type is invalid");
        };
    }

    private String normalizeReconciliationType(String type) {
        String normalized = type.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "SUPPLIER", "LOGISTICS", "PLATFORM" -> normalized;
            default -> throw new BizException("RECONCILIATION_TYPE_INVALID", "Reconciliation type is invalid");
        };
    }

    private String normalizePlatformBillType(String billType) {
        String normalized = billType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "COMMISSION", "ADVERTISING", "FBA_FEE", "STORAGE_FEE",
                    "RETURN_COST", "REFUND", "PAYMENT_FEE", "TAX", "OTHER" -> normalized;
            default -> throw new BizException("PLATFORM_BILL_TYPE_INVALID", "Platform bill type is invalid");
        };
    }

    private String normalizePlatformSettlementType(String settlementType) {
        String normalized = settlementType.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "REGULAR", "ADJUSTMENT", "REIMBURSEMENT", "RECOVERY", "OTHER" -> normalized;
            default -> throw new BizException("PLATFORM_SETTLEMENT_TYPE_INVALID", "Platform settlement type is invalid");
        };
    }

    private String normalizeWithdrawalStatus(String withdrawalStatus) {
        String normalized = withdrawalStatus.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "REQUESTED", "COMPLETED" -> normalized;
            default -> throw new BizException("PLATFORM_SETTLEMENT_WITHDRAWAL_STATUS_INVALID", "Platform settlement withdrawal status is invalid");
        };
    }

    private String normalizeForexStatus(String forexStatus) {
        String normalized = forexStatus.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "PROCESSING", "SETTLED" -> normalized;
            default -> throw new BizException("PLATFORM_SETTLEMENT_FOREX_STATUS_INVALID", "Platform settlement forex status is invalid");
        };
    }

    private String mapPlatformBillCostType(String billType) {
        return switch (billType) {
            case "COMMISSION" -> CostEvent.CostType.COMMISSION.name();
            case "ADVERTISING" -> CostEvent.CostType.ADVERTISING.name();
            case "FBA_FEE" -> CostEvent.CostType.FBA_FEE.name();
            case "STORAGE_FEE" -> CostEvent.CostType.STORAGE_FEE.name();
            case "RETURN_COST", "REFUND" -> CostEvent.CostType.RETURN_COST.name();
            case "PAYMENT_FEE", "TAX", "OTHER" -> CostEvent.CostType.OTHER.name();
            default -> throw new BizException("PLATFORM_BILL_TYPE_INVALID", "Platform bill type is invalid");
        };
    }

    private BigDecimal sumByType(List<CostEvent> costs, CostEvent.CostType type) {
        return costs.stream()
                .filter(event -> event.costType().equals(type.name()))
                .map(CostEvent::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Receivable update(Receivable receivable, BigDecimal paidAmount, ReceivableStatus status) {
        return financeStore.saveReceivable(new Receivable(receivable.receivableId(), receivable.tenantId(),
                receivable.sourceType(), receivable.sourceId(), receivable.customerName(), receivable.currency(),
                receivable.amount(), paidAmount, status, receivable.createdAt(), Instant.now()));
    }

    private String defaultJson(String value) {
        return hasText(value) ? value : "[]";
    }

    private ArrayNode parseApprovalFlow(String approvalFlow) {
        try {
            JsonNode jsonNode = objectMapper.readTree(approvalFlow);
            if (jsonNode instanceof ArrayNode arrayNode && !arrayNode.isEmpty()) {
                return arrayNode;
            }
        } catch (JsonProcessingException ignored) {
        }
        throw new BizException("PAYMENT_REQUEST_APPROVAL_FLOW_INVALID", "Payment request approval flow is invalid");
    }

    private int countApprovedSteps(ArrayNode approvalFlow) {
        int count = 0;
        for (JsonNode step : approvalFlow) {
            if ("APPROVED".equalsIgnoreCase(step.path("status").asText())) {
                count++;
            }
        }
        return count;
    }

    private String writeJson(JsonNode jsonNode) {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException ex) {
            throw new BizException("PAYMENT_REQUEST_APPROVAL_FLOW_INVALID", "Payment request approval flow is invalid");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    public record CreateReceivableCommand(String sourceType, String sourceId, String customerName, String currency, BigDecimal amount) {}

    public record ReceivePaymentCommand(BigDecimal amount, String paymentMethod) {}

    public record CreatePaymentRequestCommand(String requestId, String poId, String supplierId, BigDecimal amount,
                                              String currency, String requestType, String requestedBy, String approvalFlow) {}

    public record ApprovePaymentRequestCommand(String approverId, int approvalLevel, String comment, Instant approvedAt) {}

    public record PayPaymentRequestCommand(String paidBy, Instant paidAt) {}

    public record CreateWriteOffCommand(String writeoffId, String type, String refType, String refId,
                                        BigDecimal amount, String currency) {}

    public record ApproveWriteOffCommand(String approvedBy) {}

    public record CreateReconciliationCommand(String reconId, String type, String partyId, String partyName,
                                              String period, BigDecimal payableAmount, BigDecimal paidAmount,
                                              String items) {}

    public record DisputeReconciliationCommand(String differenceItems) {}

    public record ImportPlatformBillCommand(String billId, String platform, String store, String billType,
                                            String period, String sellerSku, String marketplaceId,
                                            String sourceType, String sourceId, String currency,
                                            BigDecimal amount, String rawData, Instant occurredAt) {}

    public record CreatePlatformSettlementCommand(String settlementId, String platform, String store,
                                                  String settlementType, BigDecimal amount, String currency,
                                                  LocalDate settlementDate) {}

    public record ReconcilePlatformBillCommand(String settlementId) {}

    public record ReceivePlatformSettlementCommand(BigDecimal amount, String paymentMethod, Instant receivedAt) {}

    public record UpdatePlatformSettlementWithdrawalCommand(String withdrawalStatus, String withdrawalReference) {}

    public record UpdatePlatformSettlementForexCommand(String forexStatus, BigDecimal forexRate) {}

    public record RecordCostEventCommand(String costType, String sourceType, String sourceId, String sellerSku,
                                         String storeId, String channelCode, String marketplaceId,
                                         String currency, BigDecimal amount, Instant occurredAt) {}

    public record CalculateProfitCommand(String sellerSku, String marketplaceId, String orderId,
                                         BigDecimal revenue, String currency) {}

    public ForexRate saveForexRate(String tenantId, SaveForexRateCommand command) {
        Instant now = Instant.now();
        return financeStore.saveForexRate(new ForexRate(UUID.randomUUID().toString(), tenantId,
                normalizeCurrency(command.fromCurrency()), normalizeCurrency(command.toCurrency()), command.rate(),
                command.effectiveDate(), normalizeForexRateSource(command.source()), now));
    }

    public Optional<ForexRate> getLatestForexRate(String tenantId, String fromCurrency, String toCurrency) {
        return financeStore.findLatestForexRate(tenantId, normalizeCurrency(fromCurrency), normalizeCurrency(toCurrency));
    }

    public Optional<ForexRate> getForexRateAsOf(String tenantId, String fromCurrency, String toCurrency, LocalDate effectiveDate) {
        return financeStore.findLatestForexRateAsOf(tenantId, normalizeCurrency(fromCurrency), normalizeCurrency(toCurrency), effectiveDate);
    }

    public List<ForexRate> listForexRates(String tenantId) {
        return financeStore.listForexRates(tenantId);
    }

    public List<ForexRate> listForexRateHistory(String tenantId, String fromCurrency, String toCurrency) {
        return financeStore.listForexRateHistory(tenantId, normalizeCurrency(fromCurrency), normalizeCurrency(toCurrency));
    }

    public ForexRiskAlert checkForexRisk(String tenantId, String fromCurrency, String toCurrency) {
        String normalizedFromCurrency = normalizeCurrency(fromCurrency);
        String normalizedToCurrency = normalizeCurrency(toCurrency);
        List<ForexRate> history = financeStore.listForexRateHistory(tenantId, normalizedFromCurrency, normalizedToCurrency);
        if (history.size() < 2) {
            return new ForexRiskAlert(normalizedFromCurrency, normalizedToCurrency, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, null, null, "INSUFFICIENT_DATA");
        }
        ForexRate latest = history.get(0);
        ForexRate previous = history.get(1);
        BigDecimal changeRatio = latest.rate().subtract(previous.rate())
                .divide(previous.rate(), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        String level = "LOW";
        BigDecimal absChange = changeRatio.abs();
        if (absChange.compareTo(BigDecimal.valueOf(5)) >= 0) {
            level = "HIGH";
        } else if (absChange.compareTo(BigDecimal.valueOf(2)) >= 0) {
            level = "MEDIUM";
        }
        return new ForexRiskAlert(normalizedFromCurrency, normalizedToCurrency, latest.rate(), previous.rate(),
                changeRatio, latest.effectiveDate(), previous.effectiveDate(), level);
    }

    /**
     * FMS统一外汇中心批量换汇能力，面向店铺利润分析、回款换汇测算等场景。
     */
    public ForexConversionBatchResult convertForexBatch(String tenantId, ForexConversionBatchCommand command) {
        if (command.conversions() == null || command.conversions().isEmpty()) {
            throw new BizException("FOREX_CONVERSION_EMPTY", "Forex conversion request must contain at least one item");
        }
        List<ForexConversionResult> conversions = command.conversions().stream()
                .map(conversion -> convertForex(tenantId, conversion))
                .toList();
        return new ForexConversionBatchResult(conversions.size(), conversions);
    }

    public ForexTransaction createForexTransaction(String tenantId, CreateForexTransactionCommand command) {
        BigDecimal convertedAmount = command.amount().multiply(command.rate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = command.fee() != null ? command.fee() : BigDecimal.ZERO;
        return financeStore.saveForexTransaction(new ForexTransaction(UUID.randomUUID().toString(), tenantId,
                normalizeCurrency(command.fromCurrency()), normalizeCurrency(command.toCurrency()), command.amount(), command.rate(),
                fee, convertedAmount, command.refType(), command.refId(), Instant.now()));
    }

    public List<ForexTransaction> listForexTransactions(String tenantId) {
        return financeStore.listForexTransactions(tenantId);
    }

    public List<ForexTransaction> listForexTransactionsByRef(String tenantId, String refType, String refId) {
        return financeStore.listForexTransactionsByRef(tenantId, refType, refId);
    }

    private ForexConversionResult convertForex(String tenantId, ForexConversionCommand command) {
        String fromCurrency = normalizeCurrency(command.fromCurrency());
        String toCurrency = normalizeCurrency(command.toCurrency());
        Optional<ForexRate> rate = command.effectiveDate() == null
                ? getLatestForexRate(tenantId, fromCurrency, toCurrency)
                : getForexRateAsOf(tenantId, fromCurrency, toCurrency, command.effectiveDate());
        ForexRate resolvedRate = rate.orElseThrow(() -> new BizException("FOREX_RATE_NOT_FOUND",
                "Forex rate does not exist for " + fromCurrency + " -> " + toCurrency));
        BigDecimal convertedAmount = command.amount().multiply(resolvedRate.rate())
                .setScale(2, RoundingMode.HALF_UP);
        return new ForexConversionResult(fromCurrency, toCurrency, command.amount(), resolvedRate.rate(),
                convertedAmount, resolvedRate.effectiveDate(), resolvedRate.source());
    }

    private String normalizeCurrency(String currency) {
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeForexRateSource(String source) {
        return hasText(source) ? source.trim().toUpperCase(Locale.ROOT) : "MANUAL";
    }

    public record SaveForexRateCommand(String fromCurrency, String toCurrency, BigDecimal rate,
                                       LocalDate effectiveDate, String source) {}

    public record ForexConversionBatchCommand(List<ForexConversionCommand> conversions) {}

    public record ForexConversionCommand(String fromCurrency, String toCurrency,
                                         BigDecimal amount, LocalDate effectiveDate) {}

    public record ForexConversionBatchResult(int totalCount, List<ForexConversionResult> conversions) {}

    public record ForexConversionResult(String fromCurrency, String toCurrency,
                                        BigDecimal amount, BigDecimal rate,
                                        BigDecimal convertedAmount, LocalDate effectiveDate,
                                        String source) {}

    public record CreateForexTransactionCommand(String fromCurrency, String toCurrency, BigDecimal amount,
                                                BigDecimal rate, BigDecimal fee, String refType, String refId) {}
}
