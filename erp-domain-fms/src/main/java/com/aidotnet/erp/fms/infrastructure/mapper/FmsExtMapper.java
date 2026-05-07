package com.aidotnet.erp.fms.infrastructure.mapper;

import com.aidotnet.erp.fms.infrastructure.data.BillingRuleDO;
import com.aidotnet.erp.fms.infrastructure.data.CostAggregationRuleDO;
import com.aidotnet.erp.fms.infrastructure.data.CostAnomalyDO;
import com.aidotnet.erp.fms.infrastructure.data.CostAllocationResultDO;
import com.aidotnet.erp.fms.infrastructure.data.CostBreakdownDO;
import com.aidotnet.erp.fms.infrastructure.data.CurrencyRateDO;
import com.aidotnet.erp.fms.infrastructure.data.CurrencyRateSyncLogDO;
import com.aidotnet.erp.fms.infrastructure.data.FraudDetectionResultDO;
import com.aidotnet.erp.fms.infrastructure.data.ExternalFinanceVoucherDO;
import com.aidotnet.erp.fms.infrastructure.data.FinanceSyncConfigDO;
import com.aidotnet.erp.fms.infrastructure.data.InvoiceDO;
import com.aidotnet.erp.fms.infrastructure.data.InvoiceSettingDO;
import com.aidotnet.erp.fms.infrastructure.data.JournalEntryDO;
import com.aidotnet.erp.fms.infrastructure.data.ProfitDeviationAlertDO;
import com.aidotnet.erp.fms.infrastructure.data.ProfitResultDO;
import com.aidotnet.erp.fms.infrastructure.data.RiskAssessmentDO;
import com.aidotnet.erp.fms.infrastructure.data.TaxRuleDO;
import com.aidotnet.erp.fms.infrastructure.data.VatComplianceStatusDO;
import com.aidotnet.erp.fms.infrastructure.data.VoucherDO;
import com.aidotnet.erp.fms.infrastructure.data.VoucherTemplateDO;
import com.aidotnet.erp.fms.infrastructure.data.TradeComplianceResultDO;
import com.aidotnet.erp.fms.infrastructure.data.PlatformComplianceResultDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FmsExtMapper {

    void insertCostBreakdown(CostBreakdownDO breakdown);
    List<CostBreakdownDO> selectCostBreakdownsByEvent(@Param("tenantId") String tenantId, @Param("costEventId") String costEventId);
    List<CostBreakdownDO> selectCostBreakdownsByType(@Param("tenantId") String tenantId, @Param("costType") String costType);

    void insertJournalEntry(JournalEntryDO entry);
    List<JournalEntryDO> selectJournalEntriesByReference(@Param("tenantId") String tenantId, @Param("referenceType") String referenceType, @Param("referenceId") String referenceId);
    List<JournalEntryDO> selectJournalEntriesByAccount(@Param("tenantId") String tenantId, @Param("accountCode") String accountCode);

    void insertTaxRule(TaxRuleDO rule);
    void updateTaxRule(TaxRuleDO rule);
    TaxRuleDO selectTaxRule(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);
    List<TaxRuleDO> selectTaxRules(@Param("tenantId") String tenantId, @Param("countryCode") String countryCode);
    TaxRuleDO selectActiveTaxRule(@Param("tenantId") String tenantId, @Param("countryCode") String countryCode, @Param("taxType") String taxType);

    void insertInvoice(InvoiceDO invoice);
    void updateInvoice(InvoiceDO invoice);
    InvoiceDO selectInvoice(@Param("tenantId") String tenantId, @Param("invoiceId") String invoiceId);
    List<InvoiceDO> selectInvoices(@Param("tenantId") String tenantId, @Param("countryCode") String countryCode, @Param("status") String status);

    void insertVoucher(VoucherDO voucher);
    void updateVoucher(VoucherDO voucher);
    VoucherDO selectVoucher(@Param("tenantId") String tenantId, @Param("voucherId") String voucherId);
    List<VoucherDO> selectVouchers(@Param("tenantId") String tenantId, @Param("voucherType") String voucherType, @Param("status") String status);
    List<VoucherDO> selectVouchersByReference(@Param("tenantId") String tenantId, @Param("referenceType") String referenceType, @Param("referenceId") String referenceId);

    void insertInvoiceSetting(InvoiceSettingDO setting);
    void updateInvoiceSetting(InvoiceSettingDO setting);
    InvoiceSettingDO selectInvoiceSetting(@Param("tenantId") String tenantId, @Param("settingId") String settingId);
    InvoiceSettingDO selectInvoiceSettingByScope(@Param("tenantId") String tenantId, @Param("storeId") String storeId, @Param("marketplaceId") String marketplaceId);
    List<InvoiceSettingDO> selectInvoiceSettings(@Param("tenantId") String tenantId);

    void insertExternalFinanceVoucher(ExternalFinanceVoucherDO voucher);
    void updateExternalFinanceVoucher(ExternalFinanceVoucherDO voucher);
    ExternalFinanceVoucherDO selectExternalFinanceVoucher(@Param("tenantId") String tenantId, @Param("voucherId") String voucherId);
    List<ExternalFinanceVoucherDO> selectExternalFinanceVouchers(@Param("tenantId") String tenantId, @Param("syncStatus") String syncStatus);

    void insertFinanceSyncConfig(FinanceSyncConfigDO config);
    void updateFinanceSyncConfig(FinanceSyncConfigDO config);
    FinanceSyncConfigDO selectFinanceSyncConfig(@Param("tenantId") String tenantId, @Param("configId") String configId);
    FinanceSyncConfigDO selectFinanceSyncConfigBySystem(@Param("tenantId") String tenantId, @Param("financeSystem") String financeSystem);
    List<FinanceSyncConfigDO> selectFinanceSyncConfigs(@Param("tenantId") String tenantId);

    void insertCostAggregationRule(CostAggregationRuleDO rule);
    void updateCostAggregationRule(CostAggregationRuleDO rule);
    CostAggregationRuleDO selectCostAggregationRule(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);
    List<CostAggregationRuleDO> selectCostAggregationRules(@Param("tenantId") String tenantId, @Param("costSource") String costSource);

    void insertCostAllocationResult(CostAllocationResultDO result);
    List<CostAllocationResultDO> selectCostAllocationResults(@Param("tenantId") String tenantId, @Param("targetDimension") String targetDimension, @Param("targetId") String targetId);
    List<CostAllocationResultDO> selectCostAllocationResultsBySku(@Param("tenantId") String tenantId, @Param("sellerSku") String sellerSku);
    List<CostAllocationResultDO> selectCostAllocationResultsByTarget(@Param("tenantId") String tenantId, @Param("dimensionType") String dimensionType, @Param("dimensionId") String dimensionId);

    void insertCostAnomaly(CostAnomalyDO anomaly);
    void updateCostAnomaly(CostAnomalyDO anomaly);
    CostAnomalyDO selectCostAnomaly(@Param("tenantId") String tenantId, @Param("anomalyId") String anomalyId);
    CostAnomalyDO selectCostAnomalyByIdempotencyKey(@Param("tenantId") String tenantId, @Param("idempotencyKey") String idempotencyKey);
    List<CostAnomalyDO> selectCostAnomalies(@Param("tenantId") String tenantId,
                                            @Param("status") String status,
                                            @Param("sellerSku") String sellerSku,
                                            @Param("storeId") String storeId);

    void insertProfitResult(ProfitResultDO result);
    ProfitResultDO selectProfitResult(@Param("tenantId") String tenantId, @Param("resultId") String resultId);
    List<ProfitResultDO> selectProfitResults(@Param("tenantId") String tenantId, @Param("dimensionType") String dimensionType, @Param("dimensionId") String dimensionId);
    List<ProfitResultDO> selectProfitResultsBySku(@Param("tenantId") String tenantId, @Param("sellerSku") String sellerSku);
    List<ProfitResultDO> selectAllProfitResults(@Param("tenantId") String tenantId);
    List<ProfitResultDO> selectProfitResultsByDimension(@Param("tenantId") String tenantId, @Param("dimensionType") String dimensionType);

    void insertProfitDeviationAlert(ProfitDeviationAlertDO alert);
    ProfitDeviationAlertDO selectProfitDeviationAlert(@Param("tenantId") String tenantId, @Param("alertId") String alertId);
    void updateProfitDeviationAlert(ProfitDeviationAlertDO alert);
    List<ProfitDeviationAlertDO> selectProfitDeviationAlerts(@Param("tenantId") String tenantId, @Param("status") String status);

    void insertCurrencyRate(CurrencyRateDO rate);
    List<CurrencyRateDO> selectCurrencyRates(@Param("tenantId") String tenantId, @Param("fromCurrency") String fromCurrency, @Param("toCurrency") String toCurrency);
    List<CurrencyRateDO> selectCurrencyRatesByDate(@Param("tenantId") String tenantId, @Param("fromCurrency") String fromCurrency, @Param("toCurrency") String toCurrency, @Param("rateDate") java.time.LocalDate rateDate);
    List<CurrencyRateDO> selectCurrencyRateHistory(@Param("tenantId") String tenantId, @Param("fromCurrency") String fromCurrency, @Param("toCurrency") String toCurrency, @Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);

    void insertCurrencyRateSyncLog(CurrencyRateSyncLogDO syncLog);
    List<CurrencyRateSyncLogDO> selectCurrencyRateSyncLogs(@Param("tenantId") String tenantId);

    void insertBillingRule(BillingRuleDO rule);
    void updateBillingRule(BillingRuleDO rule);
    BillingRuleDO selectBillingRule(@Param("tenantId") String tenantId, @Param("ruleId") String ruleId);
    List<BillingRuleDO> selectBillingRules(@Param("tenantId") String tenantId, @Param("feeType") String feeType, @Param("platform") String platform);

    void insertVoucherTemplate(VoucherTemplateDO template);
    void updateVoucherTemplate(VoucherTemplateDO template);
    VoucherTemplateDO selectVoucherTemplate(@Param("tenantId") String tenantId, @Param("templateId") String templateId);
    List<VoucherTemplateDO> selectVoucherTemplates(@Param("tenantId") String tenantId, @Param("businessType") String businessType);

    void insertRiskAssessment(RiskAssessmentDO assessment);
    List<RiskAssessmentDO> selectRiskAssessments(@Param("tenantId") String tenantId, @Param("targetType") String targetType, @Param("riskLevel") String riskLevel);

    void insertFraudDetectionResult(FraudDetectionResultDO result);
    List<FraudDetectionResultDO> selectFraudDetectionResults(@Param("tenantId") String tenantId, @Param("status") String status);

    void insertVatComplianceStatus(VatComplianceStatusDO status);
    void updateVatComplianceStatus(VatComplianceStatusDO status);
    VatComplianceStatusDO selectVatComplianceStatus(@Param("tenantId") String tenantId, @Param("countryCode") String countryCode);
    List<VatComplianceStatusDO> selectVatComplianceStatuses(@Param("tenantId") String tenantId);

    void insertTradeComplianceResult(TradeComplianceResultDO result);
    List<TradeComplianceResultDO> selectTradeComplianceResults(@Param("tenantId") String tenantId, @Param("status") String status);

    void insertPlatformComplianceResult(PlatformComplianceResultDO result);
    List<PlatformComplianceResultDO> selectPlatformComplianceResults(@Param("tenantId") String tenantId, @Param("platform") String platform, @Param("status") String status);
}
