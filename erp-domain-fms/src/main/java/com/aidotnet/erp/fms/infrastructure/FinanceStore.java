package com.aidotnet.erp.fms.infrastructure;

import com.aidotnet.erp.fms.domain.CostEvent;
import com.aidotnet.erp.fms.domain.ForexRate;
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
import com.aidotnet.erp.fms.domain.Voucher;
import com.aidotnet.erp.fms.domain.Voucher.VoucherStatus;
import com.aidotnet.erp.fms.domain.VoucherLine;
import com.aidotnet.erp.fms.domain.WriteOff;
import com.aidotnet.erp.fms.infrastructure.data.CostEventDO;
import com.aidotnet.erp.fms.infrastructure.data.ForexRateDO;
import com.aidotnet.erp.fms.infrastructure.data.ForexTransactionDO;
import com.aidotnet.erp.fms.infrastructure.data.PaymentApprovalDO;
import com.aidotnet.erp.fms.infrastructure.data.PaymentRecordDO;
import com.aidotnet.erp.fms.infrastructure.data.PaymentRequestDO;
import com.aidotnet.erp.fms.infrastructure.data.PlatformBillDO;
import com.aidotnet.erp.fms.infrastructure.data.PlatformSettlementDO;
import com.aidotnet.erp.fms.infrastructure.data.ProfitStatementDO;
import com.aidotnet.erp.fms.infrastructure.data.ReconciliationDO;
import com.aidotnet.erp.fms.infrastructure.data.ReceivableDO;
import com.aidotnet.erp.fms.infrastructure.data.VoucherDO;
import com.aidotnet.erp.fms.infrastructure.data.WriteOffDO;
import com.aidotnet.erp.fms.infrastructure.mapper.FinanceMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * FMS核心数据存储
 * <p>
 * 描述: FMS域核心数据持久化层，负责成本事件、应收、付款、平台账单、
 *       结算、对账、利润报表等核心业务对象与数据库之间的转换和持久化操作。
 * </p>
 *
 * @author ERP系统
 * @see FinanceMapper
 */
@Repository
public class FinanceStore {

    private final FinanceMapper mapper;

    public FinanceStore(FinanceMapper mapper) {
        this.mapper = mapper;
    }

    public Receivable saveReceivable(Receivable receivable) {
        ReceivableDO existing = mapper.selectReceivable(receivable.tenantId(), receivable.receivableId());
        ReceivableDO data = toReceivableData(receivable);
        if (existing == null) {
            mapper.insertReceivable(data);
        } else {
            mapper.updateReceivable(data);
        }
        return receivable;
    }

    public Optional<Receivable> findReceivable(String tenantId, String receivableId) {
        return Optional.ofNullable(mapper.selectReceivable(tenantId, receivableId)).map(this::toReceivableDomain);
    }

    public Optional<Receivable> findBySource(String tenantId, String sourceType, String sourceId) {
        return Optional.ofNullable(mapper.selectReceivableBySource(tenantId, sourceType, sourceId)).map(this::toReceivableDomain);
    }

    public List<Receivable> listReceivables(String tenantId) {
        return mapper.selectReceivables(tenantId).stream().map(this::toReceivableDomain).toList();
    }

    public PaymentRecord appendPayment(PaymentRecord paymentRecord) {
        mapper.insertPayment(toPaymentData(paymentRecord));
        return paymentRecord;
    }

    public List<PaymentRecord> listPayments(String tenantId, String receivableId) {
        return mapper.selectPayments(tenantId, receivableId).stream().map(this::toPaymentDomain).toList();
    }

    public PaymentApproval appendPaymentApproval(PaymentApproval paymentApproval) {
        mapper.insertPaymentApproval(toPaymentApprovalData(paymentApproval));
        return paymentApproval;
    }

    public List<PaymentApproval> listPaymentApprovals(String tenantId, String requestId) {
        return mapper.selectPaymentApprovals(tenantId, requestId).stream().map(this::toPaymentApprovalDomain).toList();
    }

    public PaymentRequest savePaymentRequest(PaymentRequest paymentRequest) {
        PaymentRequestDO existing = mapper.selectPaymentRequest(paymentRequest.tenantId(), paymentRequest.requestId());
        PaymentRequestDO data = toPaymentRequestData(paymentRequest);
        if (existing == null) {
            mapper.insertPaymentRequest(data);
        } else {
            mapper.updatePaymentRequest(data);
        }
        return paymentRequest;
    }

    public Optional<PaymentRequest> findPaymentRequest(String tenantId, String requestId) {
        return Optional.ofNullable(mapper.selectPaymentRequest(tenantId, requestId)).map(this::toPaymentRequestDomain);
    }

    public List<PaymentRequest> listPaymentRequests(String tenantId) {
        return mapper.selectPaymentRequests(tenantId).stream().map(this::toPaymentRequestDomain).toList();
    }

    public List<PaymentRequest> listPaymentRequestsByPo(String tenantId, String poId) {
        return mapper.selectPaymentRequestsByPo(tenantId, poId).stream().map(this::toPaymentRequestDomain).toList();
    }

    public WriteOff saveWriteOff(WriteOff writeOff) {
        WriteOffDO existing = mapper.selectWriteOff(writeOff.tenantId(), writeOff.writeoffId());
        WriteOffDO data = toWriteOffData(writeOff);
        if (existing == null) {
            mapper.insertWriteOff(data);
        } else {
            mapper.updateWriteOff(data);
        }
        return writeOff;
    }

    public Optional<WriteOff> findWriteOff(String tenantId, String writeoffId) {
        return Optional.ofNullable(mapper.selectWriteOff(tenantId, writeoffId)).map(this::toWriteOffDomain);
    }

    public List<WriteOff> listWriteOffs(String tenantId) {
        return mapper.selectWriteOffs(tenantId).stream().map(this::toWriteOffDomain).toList();
    }

    public List<WriteOff> listWriteOffsByRef(String tenantId, String refType, String refId) {
        return mapper.selectWriteOffsByRef(tenantId, refType, refId).stream().map(this::toWriteOffDomain).toList();
    }

    public Reconciliation saveReconciliation(Reconciliation reconciliation) {
        ReconciliationDO existing = mapper.selectReconciliation(reconciliation.tenantId(), reconciliation.reconId());
        ReconciliationDO data = toReconciliationData(reconciliation);
        if (existing == null) {
            mapper.insertReconciliation(data);
        } else {
            mapper.updateReconciliation(data);
        }
        return reconciliation;
    }

    public Optional<Reconciliation> findReconciliation(String tenantId, String reconId) {
        return Optional.ofNullable(mapper.selectReconciliation(tenantId, reconId)).map(this::toReconciliationDomain);
    }

    public List<Reconciliation> listReconciliations(String tenantId) {
        return mapper.selectReconciliations(tenantId).stream().map(this::toReconciliationDomain).toList();
    }

    public List<Reconciliation> listReconciliationsByTypeAndParty(String tenantId, String type, String partyId) {
        return mapper.selectReconciliationsByTypeAndParty(tenantId, type, partyId).stream()
                .map(this::toReconciliationDomain)
                .toList();
    }

    public PlatformBill savePlatformBill(PlatformBill platformBill) {
        PlatformBillDO existing = mapper.selectPlatformBill(platformBill.tenantId(), platformBill.billId());
        PlatformBillDO data = toPlatformBillData(platformBill);
        if (existing == null) {
            mapper.insertPlatformBill(data);
        } else {
            mapper.updatePlatformBill(data);
        }
        return platformBill;
    }

    public Optional<PlatformBill> findPlatformBill(String tenantId, String billId) {
        return Optional.ofNullable(mapper.selectPlatformBill(tenantId, billId)).map(this::toPlatformBillDomain);
    }

    public List<PlatformBill> listPlatformBills(String tenantId) {
        return mapper.selectPlatformBills(tenantId).stream().map(this::toPlatformBillDomain).toList();
    }

    public List<PlatformBill> listPlatformBillsBySettlementId(String tenantId, String settlementId) {
        return mapper.selectPlatformBillsBySettlementId(tenantId, settlementId).stream()
                .map(this::toPlatformBillDomain)
                .toList();
    }

    public PlatformSettlement savePlatformSettlement(PlatformSettlement platformSettlement) {
        PlatformSettlementDO existing = mapper.selectPlatformSettlement(platformSettlement.tenantId(), platformSettlement.settlementId());
        PlatformSettlementDO data = toPlatformSettlementData(platformSettlement);
        if (existing == null) {
            mapper.insertPlatformSettlement(data);
        } else {
            mapper.updatePlatformSettlement(data);
        }
        return platformSettlement;
    }

    public Optional<PlatformSettlement> findPlatformSettlement(String tenantId, String settlementId) {
        return Optional.ofNullable(mapper.selectPlatformSettlement(tenantId, settlementId)).map(this::toPlatformSettlementDomain);
    }

    public List<PlatformSettlement> listPlatformSettlements(String tenantId) {
        return mapper.selectPlatformSettlements(tenantId).stream().map(this::toPlatformSettlementDomain).toList();
    }

    public CostEvent saveCostEvent(CostEvent event) {
        CostEventDO existing = mapper.selectCostEvent(event.tenantId(), event.costEventId());
        CostEventDO data = toCostEventData(event);
        if (existing == null) {
            mapper.insertCostEvent(data);
        }
        return event;
    }

    public List<CostEvent> listCostEvents(String tenantId) {
        return mapper.selectCostEvents(tenantId).stream().map(this::toCostEventDomain).toList();
    }

    public Optional<CostEvent> findCostEvent(String tenantId, String costEventId) {
        return Optional.ofNullable(mapper.selectCostEvent(tenantId, costEventId)).map(this::toCostEventDomain);
    }

    public List<CostEvent> listCostEventsBySku(String tenantId, String sellerSku) {
        return mapper.selectCostEventsBySku(tenantId, sellerSku).stream().map(this::toCostEventDomain).toList();
    }

    public List<CostEvent> listCostEventsBySource(String tenantId, String sourceType, String sourceId) {
        return mapper.selectCostEventsBySource(tenantId, sourceType, sourceId).stream().map(this::toCostEventDomain).toList();
    }

    public ProfitStatement saveProfitStatement(ProfitStatement statement) {
        mapper.insertProfitStatement(toProfitStatementData(statement));
        return statement;
    }

    public List<ProfitStatement> listProfitStatements(String tenantId) {
        return mapper.selectProfitStatements(tenantId).stream().map(this::toProfitStatementDomain).toList();
    }

    public List<ProfitStatement> listProfitStatementsBySku(String tenantId, String sellerSku) {
        return mapper.selectProfitStatementsBySku(tenantId, sellerSku).stream().map(this::toProfitStatementDomain).toList();
    }

    private ReceivableDO toReceivableData(Receivable r) {
        ReceivableDO d = new ReceivableDO();
        d.setReceivableId(r.receivableId());
        d.setTenantId(r.tenantId());
        d.setSourceType(r.sourceType());
        d.setSourceId(r.sourceId());
        d.setCustomerName(r.customerName());
        d.setCurrency(r.currency());
        d.setAmount(r.amount());
        d.setPaidAmount(r.paidAmount());
        d.setStatus(r.status().name());
        d.setCreatedAt(r.createdAt());
        d.setUpdatedAt(r.updatedAt());
        return d;
    }

    private Receivable toReceivableDomain(ReceivableDO d) {
        return new Receivable(d.getReceivableId(), d.getTenantId(), d.getSourceType(), d.getSourceId(),
                d.getCustomerName(), d.getCurrency(), d.getAmount(), d.getPaidAmount(),
                ReceivableStatus.valueOf(d.getStatus()), d.getCreatedAt(), d.getUpdatedAt());
    }

    private PaymentRecordDO toPaymentData(PaymentRecord r) {
        PaymentRecordDO d = new PaymentRecordDO();
        d.setPaymentId(r.paymentId());
        d.setTenantId(r.tenantId());
        d.setReceivableId(r.receivableId());
        d.setAmount(r.amount());
        d.setPaymentMethod(r.paymentMethod());
        d.setPaidAt(r.paidAt());
        return d;
    }

    private PaymentRecord toPaymentDomain(PaymentRecordDO d) {
        return new PaymentRecord(d.getPaymentId(), d.getTenantId(), d.getReceivableId(),
                d.getAmount(), d.getPaymentMethod(), d.getPaidAt());
    }

    private PaymentApprovalDO toPaymentApprovalData(PaymentApproval approval) {
        PaymentApprovalDO d = new PaymentApprovalDO();
        d.setApprovalId(approval.approvalId());
        d.setTenantId(approval.tenantId());
        d.setRequestId(approval.requestId());
        d.setApproverId(approval.approverId());
        d.setApprovalLevel(approval.approvalLevel());
        d.setStatus(approval.status());
        d.setComment(approval.comment());
        d.setApprovedAt(approval.approvedAt());
        d.setCreatedAt(approval.createdAt());
        return d;
    }

    private PaymentApproval toPaymentApprovalDomain(PaymentApprovalDO d) {
        return new PaymentApproval(d.getApprovalId(), d.getTenantId(), d.getRequestId(), d.getApproverId(),
                d.getApprovalLevel(), d.getStatus(), d.getComment(), d.getApprovedAt(), d.getCreatedAt());
    }

    private PaymentRequestDO toPaymentRequestData(PaymentRequest r) {
        PaymentRequestDO d = new PaymentRequestDO();
        d.setRequestId(r.requestId());
        d.setTenantId(r.tenantId());
        d.setPoId(r.poId());
        d.setSupplierId(r.supplierId());
        d.setAmount(r.amount());
        d.setCurrency(r.currency());
        d.setRequestType(r.requestType());
        d.setStatus(r.status());
        d.setRequestedBy(r.requestedBy());
        d.setApprovalFlow(r.approvalFlow());
        d.setPaidBy(r.paidBy());
        d.setPaidAt(r.paidAt());
        d.setWriteoffStatus(r.writeoffStatus());
        d.setWriteoffAmount(r.writeoffAmount());
        d.setCreatedAt(r.createdAt());
        d.setUpdatedAt(r.updatedAt());
        return d;
    }

    private PaymentRequest toPaymentRequestDomain(PaymentRequestDO d) {
        return new PaymentRequest(d.getRequestId(), d.getTenantId(), d.getPoId(), d.getSupplierId(),
                d.getAmount(), d.getCurrency(), d.getRequestType(), d.getStatus(), d.getRequestedBy(),
                d.getApprovalFlow(), d.getPaidBy(), d.getPaidAt(), d.getWriteoffStatus(),
                d.getWriteoffAmount(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private WriteOffDO toWriteOffData(WriteOff w) {
        WriteOffDO d = new WriteOffDO();
        d.setWriteoffId(w.writeoffId());
        d.setTenantId(w.tenantId());
        d.setType(w.type());
        d.setRefType(w.refType());
        d.setRefId(w.refId());
        d.setAmount(w.amount());
        d.setCurrency(w.currency());
        d.setStatus(w.status());
        d.setApprovedBy(w.approvedBy());
        d.setCreatedAt(w.createdAt());
        d.setUpdatedAt(w.updatedAt());
        return d;
    }

    private WriteOff toWriteOffDomain(WriteOffDO d) {
        return new WriteOff(d.getWriteoffId(), d.getTenantId(), d.getType(), d.getRefType(),
                d.getRefId(), d.getAmount(), d.getCurrency(), d.getStatus(), d.getApprovedBy(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private ReconciliationDO toReconciliationData(Reconciliation r) {
        ReconciliationDO d = new ReconciliationDO();
        d.setReconId(r.reconId());
        d.setTenantId(r.tenantId());
        d.setType(r.type());
        d.setPartyId(r.partyId());
        d.setPartyName(r.partyName());
        d.setPeriod(r.period());
        d.setPayableAmount(r.payableAmount());
        d.setPaidAmount(r.paidAmount());
        d.setBalance(r.balance());
        d.setStatus(r.status());
        d.setItems(r.items());
        d.setDifferenceItems(r.differenceItems());
        d.setCreatedAt(r.createdAt());
        d.setUpdatedAt(r.updatedAt());
        return d;
    }

    private Reconciliation toReconciliationDomain(ReconciliationDO d) {
        return new Reconciliation(d.getReconId(), d.getTenantId(), d.getType(), d.getPartyId(), d.getPartyName(),
                d.getPeriod(), d.getPayableAmount(), d.getPaidAmount(), d.getBalance(), d.getStatus(),
                d.getItems(), d.getDifferenceItems(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private PlatformBillDO toPlatformBillData(PlatformBill bill) {
        PlatformBillDO data = new PlatformBillDO();
        data.setBillId(bill.billId());
        data.setTenantId(bill.tenantId());
        data.setPlatform(bill.platform());
        data.setStore(bill.store());
        data.setBillType(bill.billType());
        data.setPeriod(bill.period());
        data.setSellerSku(bill.sellerSku());
        data.setMarketplaceId(bill.marketplaceId());
        data.setSourceType(bill.sourceType());
        data.setSourceId(bill.sourceId());
        data.setCurrency(bill.currency());
        data.setAmount(bill.amount());
        data.setRawData(bill.rawData());
        data.setStatus(bill.status());
        data.setSettlementId(bill.settlementId());
        data.setCreatedAt(bill.createdAt());
        data.setUpdatedAt(bill.updatedAt());
        return data;
    }

    private PlatformBill toPlatformBillDomain(PlatformBillDO data) {
        return new PlatformBill(data.getBillId(), data.getTenantId(), data.getPlatform(), data.getStore(),
                data.getBillType(), data.getPeriod(), data.getSellerSku(), data.getMarketplaceId(),
                data.getSourceType(), data.getSourceId(), data.getCurrency(), data.getAmount(),
                data.getRawData(), data.getStatus(), data.getSettlementId(), data.getCreatedAt(), data.getUpdatedAt());
    }

    private PlatformSettlementDO toPlatformSettlementData(PlatformSettlement settlement) {
        PlatformSettlementDO data = new PlatformSettlementDO();
        data.setSettlementId(settlement.settlementId());
        data.setTenantId(settlement.tenantId());
        data.setPlatform(settlement.platform());
        data.setStore(settlement.store());
        data.setSettlementType(settlement.settlementType());
        data.setAmount(settlement.amount());
        data.setReconciledAmount(settlement.reconciledAmount());
        data.setLinkedBillCount(settlement.linkedBillCount());
        data.setReceivedAmount(settlement.receivedAmount());
        data.setCurrency(settlement.currency());
        data.setSettlementDate(settlement.settlementDate());
        data.setStatus(settlement.status());
        data.setWithdrawalStatus(settlement.withdrawalStatus());
        data.setWithdrawalReference(settlement.withdrawalReference());
        data.setForexStatus(settlement.forexStatus());
        data.setForexRate(settlement.forexRate());
        data.setReceivedAt(settlement.receivedAt());
        data.setCreatedAt(settlement.createdAt());
        data.setUpdatedAt(settlement.updatedAt());
        return data;
    }

    private PlatformSettlement toPlatformSettlementDomain(PlatformSettlementDO data) {
        return new PlatformSettlement(data.getSettlementId(), data.getTenantId(), data.getPlatform(), data.getStore(),
                data.getSettlementType(), data.getAmount(), data.getReconciledAmount(),
                data.getLinkedBillCount() != null ? data.getLinkedBillCount() : 0, data.getReceivedAmount(),
                data.getCurrency(), data.getSettlementDate(), data.getStatus(), data.getWithdrawalStatus(),
                data.getWithdrawalReference(), data.getForexStatus(), data.getForexRate(), data.getReceivedAt(),
                data.getCreatedAt(), data.getUpdatedAt());
    }

    private CostEventDO toCostEventData(CostEvent e) {
        CostEventDO d = new CostEventDO();
        d.setCostEventId(e.costEventId());
        d.setTenantId(e.tenantId());
        d.setCostType(e.costType());
        d.setSourceType(e.sourceType());
        d.setSourceId(e.sourceId());
        d.setSellerSku(e.sellerSku());
        d.setStoreId(e.storeId());
        d.setChannelCode(e.channelCode());
        d.setMarketplaceId(e.marketplaceId());
        d.setCurrency(e.currency());
        d.setAmount(e.amount());
        d.setOccurredAt(e.occurredAt());
        d.setCreatedAt(e.createdAt());
        return d;
    }

    private CostEvent toCostEventDomain(CostEventDO d) {
        return new CostEvent(d.getCostEventId(), d.getTenantId(), d.getCostType(),
                d.getSourceType(), d.getSourceId(), d.getSellerSku(),
                d.getStoreId(), d.getChannelCode(), d.getMarketplaceId(),
                d.getCurrency(), d.getAmount(), d.getOccurredAt(), d.getCreatedAt());
    }

    private ProfitStatementDO toProfitStatementData(ProfitStatement s) {
        ProfitStatementDO d = new ProfitStatementDO();
        d.setStatementId(s.statementId());
        d.setTenantId(s.tenantId());
        d.setSellerSku(s.sellerSku());
        d.setMarketplaceId(s.marketplaceId());
        d.setOrderId(s.orderId());
        d.setRevenue(s.revenue());
        d.setProductCost(s.productCost());
        d.setShippingCost(s.shippingCost());
        d.setFbaFee(s.fbaFee());
        d.setCommission(s.commission());
        d.setAdvertisingCost(s.advertisingCost());
        d.setOtherCost(s.otherCost());
        d.setTotalCost(s.totalCost());
        d.setGrossProfit(s.grossProfit());
        d.setGrossMargin(s.grossMargin());
        d.setCurrency(s.currency());
        d.setCreatedAt(s.createdAt());
        return d;
    }

    private ProfitStatement toProfitStatementDomain(ProfitStatementDO d) {
        return new ProfitStatement(d.getStatementId(), d.getTenantId(), d.getSellerSku(), d.getMarketplaceId(),
                d.getOrderId(), d.getRevenue(), d.getProductCost(), d.getShippingCost(), d.getFbaFee(),
                d.getCommission(), d.getAdvertisingCost(), d.getOtherCost(), d.getTotalCost(),
                d.getGrossProfit(), d.getGrossMargin(), d.getCurrency(), d.getCreatedAt());
    }

    public ForexRate saveForexRate(ForexRate forexRate) {
        ForexRateDO existing = mapper.selectForexRateByUnique(forexRate.tenantId(), forexRate.fromCurrency(),
                forexRate.toCurrency(), forexRate.effectiveDate(), forexRate.source());
        ForexRateDO data = toForexRateData(forexRate);
        if (existing == null) {
            mapper.insertForexRate(data);
        }
        return forexRate;
    }

    public Optional<ForexRate> findLatestForexRate(String tenantId, String fromCurrency, String toCurrency) {
        return Optional.ofNullable(mapper.selectLatestForexRate(tenantId, fromCurrency, toCurrency))
                .map(this::toForexRateDomain);
    }

    public Optional<ForexRate> findLatestForexRateAsOf(String tenantId, String fromCurrency, String toCurrency, LocalDate effectiveDate) {
        return Optional.ofNullable(mapper.selectLatestForexRateAsOf(tenantId, fromCurrency, toCurrency, effectiveDate))
                .map(this::toForexRateDomain);
    }

    public List<ForexRate> listForexRates(String tenantId) {
        return mapper.selectForexRates(tenantId).stream().map(this::toForexRateDomain).toList();
    }

    public List<ForexRate> listForexRateHistory(String tenantId, String fromCurrency, String toCurrency) {
        return mapper.selectForexRateHistory(tenantId, fromCurrency, toCurrency).stream().map(this::toForexRateDomain).toList();
    }

    private ForexRateDO toForexRateData(ForexRate r) {
        ForexRateDO d = new ForexRateDO();
        d.setRateId(r.rateId());
        d.setTenantId(r.tenantId());
        d.setFromCurrency(r.fromCurrency());
        d.setToCurrency(r.toCurrency());
        d.setRate(r.rate());
        d.setEffectiveDate(r.effectiveDate());
        d.setSource(r.source());
        d.setCreatedAt(r.createdAt() != null ? r.createdAt() : java.time.Instant.now());
        return d;
    }

    private ForexRate toForexRateDomain(ForexRateDO d) {
        return new ForexRate(d.getRateId(), d.getTenantId(), d.getFromCurrency(), d.getToCurrency(),
                d.getRate(), d.getEffectiveDate(), d.getSource(), d.getCreatedAt());
    }

    public ForexTransaction saveForexTransaction(ForexTransaction transaction) {
        mapper.insertForexTransaction(toForexTransactionData(transaction));
        return transaction;
    }

    public List<ForexTransaction> listForexTransactions(String tenantId) {
        return mapper.selectForexTransactions(tenantId).stream().map(this::toForexTransactionDomain).toList();
    }

    public List<ForexTransaction> listForexTransactionsByRef(String tenantId, String refType, String refId) {
        return mapper.selectForexTransactionsByRef(tenantId, refType, refId).stream().map(this::toForexTransactionDomain).toList();
    }

    private ForexTransactionDO toForexTransactionData(ForexTransaction t) {
        ForexTransactionDO d = new ForexTransactionDO();
        d.setForexId(t.forexId());
        d.setTenantId(t.tenantId());
        d.setFromCurrency(t.fromCurrency());
        d.setToCurrency(t.toCurrency());
        d.setAmount(t.amount());
        d.setRate(t.rate());
        d.setFee(t.fee());
        d.setConvertedAmount(t.convertedAmount());
        d.setRefType(t.refType());
        d.setRefId(t.refId());
        d.setCreatedAt(t.createdAt() != null ? t.createdAt() : java.time.Instant.now());
        return d;
    }

    private ForexTransaction toForexTransactionDomain(ForexTransactionDO d) {
        return new ForexTransaction(d.getForexId(), d.getTenantId(), d.getFromCurrency(), d.getToCurrency(),
                d.getAmount(), d.getRate(), d.getFee(), d.getConvertedAmount(), d.getRefType(), d.getRefId(), d.getCreatedAt());
    }

    public Voucher saveVoucher(Voucher voucher) {
        VoucherDO existing = mapper.selectVoucher(voucher.tenantId(), voucher.voucherId());
        VoucherDO data = toVoucherData(voucher);
        if (existing == null) {
            mapper.insertVoucher(data);
        } else {
            mapper.updateVoucher(data);
        }
        return voucher;
    }

    public Optional<Voucher> findVoucher(String tenantId, String voucherId) {
        return Optional.ofNullable(mapper.selectVoucher(tenantId, voucherId)).map(this::toVoucherDomain);
    }

    public List<Voucher> listVouchers(String tenantId, String voucherType, String status) {
        return mapper.selectVouchers(tenantId, voucherType, status).stream().map(this::toVoucherDomain).toList();
    }

    private VoucherDO toVoucherData(Voucher v) {
        VoucherDO d = new VoucherDO();
        d.setVoucherId(v.voucherId());
        d.setTenantId(v.tenantId());
        d.setVoucherNumber(v.voucherNumber());
        d.setVoucherType(v.voucherType());
        d.setReferenceType(v.referenceType());
        d.setReferenceId(v.referenceId());
        d.setCurrency(v.currency());
        d.setTotalDebit(v.totalDebit());
        d.setTotalCredit(v.totalCredit());
        d.setStatus(v.status().name());
        try { d.setLinesJson(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(v.lines())); } catch (Exception e) { d.setLinesJson("[]"); }
        d.setVoucherDate(v.voucherDate());
        d.setPostedBy(v.postedBy());
        d.setPostedAt(v.postedAt());
        d.setExportBatchId(v.exportBatchId());
        d.setCreatedAt(v.createdAt());
        d.setUpdatedAt(v.updatedAt());
        return d;
    }

    private Voucher toVoucherDomain(VoucherDO d) {
        List<VoucherLine> lines;
        try { lines = new com.fasterxml.jackson.databind.ObjectMapper().readValue(d.getLinesJson() != null ? d.getLinesJson() : "[]", new com.fasterxml.jackson.core.type.TypeReference<>() {}); } catch (Exception e) { lines = List.of(); }
        return new Voucher(d.getVoucherId(), d.getTenantId(), d.getVoucherNumber(), d.getVoucherType(),
                d.getReferenceType(), d.getReferenceId(), d.getCurrency(), d.getTotalDebit(), d.getTotalCredit(),
                VoucherStatus.valueOf(d.getStatus()), lines, d.getVoucherDate(), d.getPostedBy(), d.getPostedAt(),
                d.getExportBatchId(), d.getCreatedAt(), d.getUpdatedAt());
    }
}
