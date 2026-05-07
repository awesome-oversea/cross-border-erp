package com.aidotnet.erp.fms.infrastructure.mapper;

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
import java.util.List;
import java.time.LocalDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * FMS核心数据Mapper接口
 * <p>
 * 描述: 财务域核心数据访问层，负责应收、付款、对账、平台账单、
 *       结算、汇率、外汇交易、成本事件、利润报表、凭证等CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface FinanceMapper {

    /** 新增应收 */
    void insertReceivable(ReceivableDO receivable);
    /** 更新应收 */
    void updateReceivable(ReceivableDO receivable);
    /** 按租户和应收ID查询 */
    ReceivableDO selectReceivable(@Param("tenantId") String tenantId, @Param("receivableId") String receivableId);
    /** 按租户和来源查询应收 */
    ReceivableDO selectReceivableBySource(@Param("tenantId") String tenantId, @Param("sourceType") String sourceType, @Param("sourceId") String sourceId);
    /** 按租户查询应收列表 */
    List<ReceivableDO> selectReceivables(@Param("tenantId") String tenantId);

    /** 新增付款记录 */
    void insertPayment(PaymentRecordDO payment);
    /** 按租户和应收ID查询付款记录 */
    List<PaymentRecordDO> selectPayments(@Param("tenantId") String tenantId, @Param("receivableId") String receivableId);

    /** 新增付款审批 */
    void insertPaymentApproval(PaymentApprovalDO paymentApproval);
    /** 按租户和请求ID查询付款审批 */
    List<PaymentApprovalDO> selectPaymentApprovals(@Param("tenantId") String tenantId, @Param("requestId") String requestId);

    /** 新增付款请求 */
    void insertPaymentRequest(PaymentRequestDO paymentRequest);
    /** 更新付款请求 */
    void updatePaymentRequest(PaymentRequestDO paymentRequest);
    /** 按租户和请求ID查询付款请求 */
    PaymentRequestDO selectPaymentRequest(@Param("tenantId") String tenantId, @Param("requestId") String requestId);
    /** 按租户查询付款请求列表 */
    List<PaymentRequestDO> selectPaymentRequests(@Param("tenantId") String tenantId);
    /** 按租户和采购单查询付款请求列表 */
    List<PaymentRequestDO> selectPaymentRequestsByPo(@Param("tenantId") String tenantId, @Param("poId") String poId);

    /** 新增核销 */
    void insertWriteOff(WriteOffDO writeOff);
    /** 更新核销 */
    void updateWriteOff(WriteOffDO writeOff);
    /** 按租户和核销ID查询 */
    WriteOffDO selectWriteOff(@Param("tenantId") String tenantId, @Param("writeoffId") String writeoffId);
    /** 按租户查询核销列表 */
    List<WriteOffDO> selectWriteOffs(@Param("tenantId") String tenantId);
    /** 按租户和关联查询核销列表 */
    List<WriteOffDO> selectWriteOffsByRef(@Param("tenantId") String tenantId,
                                          @Param("refType") String refType,
                                          @Param("refId") String refId);

    /** 新增对账单 */
    void insertReconciliation(ReconciliationDO reconciliation);
    /** 更新对账单 */
    void updateReconciliation(ReconciliationDO reconciliation);
    /** 按租户和对账ID查询 */
    ReconciliationDO selectReconciliation(@Param("tenantId") String tenantId, @Param("reconId") String reconId);
    /** 按租户查询对账单列表 */
    List<ReconciliationDO> selectReconciliations(@Param("tenantId") String tenantId);
    /** 按租户和类型+对账方查询对账单 */
    List<ReconciliationDO> selectReconciliationsByTypeAndParty(@Param("tenantId") String tenantId,
                                                               @Param("type") String type,
                                                               @Param("partyId") String partyId);

    /** 新增平台账单 */
    void insertPlatformBill(PlatformBillDO platformBill);
    /** 更新平台账单 */
    void updatePlatformBill(PlatformBillDO platformBill);
    /** 按租户和账单ID查询 */
    PlatformBillDO selectPlatformBill(@Param("tenantId") String tenantId, @Param("billId") String billId);
    /** 按租户查询平台账单列表 */
    List<PlatformBillDO> selectPlatformBills(@Param("tenantId") String tenantId);
    /** 按租户和结算ID查询平台账单 */
    List<PlatformBillDO> selectPlatformBillsBySettlementId(@Param("tenantId") String tenantId,
                                                           @Param("settlementId") String settlementId);

    /** 新增平台结算 */
    void insertPlatformSettlement(PlatformSettlementDO platformSettlement);
    /** 更新平台结算 */
    void updatePlatformSettlement(PlatformSettlementDO platformSettlement);
    /** 按租户和结算ID查询 */
    PlatformSettlementDO selectPlatformSettlement(@Param("tenantId") String tenantId, @Param("settlementId") String settlementId);
    /** 按租户查询平台结算列表 */
    List<PlatformSettlementDO> selectPlatformSettlements(@Param("tenantId") String tenantId);

    /** 新增汇率 */
    void insertForexRate(ForexRateDO forexRate);
    /** 按唯一键查询汇率 */
    ForexRateDO selectForexRateByUnique(@Param("tenantId") String tenantId,
                                        @Param("fromCurrency") String fromCurrency,
                                        @Param("toCurrency") String toCurrency,
                                        @Param("effectiveDate") LocalDate effectiveDate,
                                        @Param("source") String source);
    /** 查询最新汇率 */
    ForexRateDO selectLatestForexRate(@Param("tenantId") String tenantId,
                                      @Param("fromCurrency") String fromCurrency,
                                      @Param("toCurrency") String toCurrency);
    /** 查询指定日期最新汇率 */
    ForexRateDO selectLatestForexRateAsOf(@Param("tenantId") String tenantId,
                                          @Param("fromCurrency") String fromCurrency,
                                          @Param("toCurrency") String toCurrency,
                                          @Param("effectiveDate") LocalDate effectiveDate);
    /** 按租户查询汇率列表 */
    List<ForexRateDO> selectForexRates(@Param("tenantId") String tenantId);
    /** 查询汇率历史 */
    List<ForexRateDO> selectForexRateHistory(@Param("tenantId") String tenantId,
                                             @Param("fromCurrency") String fromCurrency,
                                             @Param("toCurrency") String toCurrency);

    /** 新增外汇交易 */
    void insertForexTransaction(ForexTransactionDO forexTransaction);
    /** 按租户查询外汇交易列表 */
    List<ForexTransactionDO> selectForexTransactions(@Param("tenantId") String tenantId);
    /** 按租户和关联查询外汇交易 */
    List<ForexTransactionDO> selectForexTransactionsByRef(@Param("tenantId") String tenantId,
                                                          @Param("refType") String refType,
                                                          @Param("refId") String refId);

    /** 新增成本事件 */
    void insertCostEvent(CostEventDO event);
    /** 按租户和成本事件ID查询 */
    CostEventDO selectCostEvent(@Param("tenantId") String tenantId, @Param("costEventId") String costEventId);
    /** 按租户查询成本事件列表 */
    List<CostEventDO> selectCostEvents(@Param("tenantId") String tenantId);
    /** 按租户和SKU查询成本事件 */
    List<CostEventDO> selectCostEventsBySku(@Param("tenantId") String tenantId, @Param("sellerSku") String sellerSku);
    /** 按租户和来源查询成本事件 */
    List<CostEventDO> selectCostEventsBySource(@Param("tenantId") String tenantId, @Param("sourceType") String sourceType, @Param("sourceId") String sourceId);

    /** 新增利润报表 */
    void insertProfitStatement(ProfitStatementDO statement);
    /** 按租户查询利润报表列表 */
    List<ProfitStatementDO> selectProfitStatements(@Param("tenantId") String tenantId);
    /** 按租户和SKU查询利润报表 */
    List<ProfitStatementDO> selectProfitStatementsBySku(@Param("tenantId") String tenantId, @Param("sellerSku") String sellerSku);

    /** 新增凭证 */
    void insertVoucher(VoucherDO voucher);
    /** 更新凭证 */
    void updateVoucher(VoucherDO voucher);
    /** 按租户和凭证ID查询 */
    VoucherDO selectVoucher(@Param("tenantId") String tenantId, @Param("voucherId") String voucherId);
    /** 按租户和类型+状态查询凭证列表 */
    List<VoucherDO> selectVouchers(@Param("tenantId") String tenantId, @Param("voucherType") String voucherType, @Param("status") String status);
}
