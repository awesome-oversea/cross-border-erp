package com.aidotnet.erp.fms.infrastructure;

import com.aidotnet.erp.fms.domain.CostAggregationRule;
import com.aidotnet.erp.fms.domain.CostAllocationResult;
import com.aidotnet.erp.fms.domain.CostBreakdown;
import com.aidotnet.erp.fms.domain.FraudDetectionResult;
import com.aidotnet.erp.fms.domain.Invoice;
import com.aidotnet.erp.fms.domain.JournalEntry;
import com.aidotnet.erp.fms.domain.JournalEntryType;
import com.aidotnet.erp.fms.domain.ProfitDeviationAlert;
import com.aidotnet.erp.fms.domain.ProfitResult;
import com.aidotnet.erp.fms.domain.RiskAssessment;
import com.aidotnet.erp.fms.domain.TaxRule;
import com.aidotnet.erp.fms.domain.VatComplianceStatus;
import com.aidotnet.erp.fms.domain.Voucher;
import com.aidotnet.erp.fms.domain.Voucher.VoucherStatus;
import com.aidotnet.erp.fms.domain.VoucherLine;
import com.aidotnet.erp.fms.domain.VoucherLineType;
import com.aidotnet.erp.fms.domain.VoucherTemplate;
import com.aidotnet.erp.fms.domain.BillingRule;
import com.aidotnet.erp.fms.domain.CurrencyRate;
import com.aidotnet.erp.fms.domain.CurrencyRateSyncLog;
import com.aidotnet.erp.fms.domain.PlatformComplianceResult;
import com.aidotnet.erp.fms.domain.TradeComplianceResult;
import com.aidotnet.erp.fms.domain.ExternalFinanceVoucher;
import com.aidotnet.erp.fms.domain.FinanceSyncConfig;
import com.aidotnet.erp.fms.infrastructure.data.BillingRuleDO;
import com.aidotnet.erp.fms.infrastructure.data.CostAggregationRuleDO;
import com.aidotnet.erp.fms.infrastructure.data.CostAllocationResultDO;
import com.aidotnet.erp.fms.infrastructure.data.CostBreakdownDO;
import com.aidotnet.erp.fms.infrastructure.data.CurrencyRateDO;
import com.aidotnet.erp.fms.infrastructure.data.CurrencyRateSyncLogDO;
import com.aidotnet.erp.fms.infrastructure.data.FraudDetectionResultDO;
import com.aidotnet.erp.fms.infrastructure.data.InvoiceDO;
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
import com.aidotnet.erp.fms.infrastructure.mapper.FmsExtMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * FMS扩展数据存储
 * <p>
 * 描述: FMS域扩展功能数据持久化层，负责汇率、外汇交易、风险预警等
 *       业务对象与数据库之间的转换和持久化操作。
 * </p>
 *
 * @author ERP系统
 * @see FmsExtMapper
 */
@Repository
public class FmsExtStore {

    private final FmsExtMapper mapper;
    private final ObjectMapper objectMapper;

    public FmsExtStore(FmsExtMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public CostBreakdown saveCostBreakdown(CostBreakdown breakdown) {
        mapper.insertCostBreakdown(toBreakdownData(breakdown));
        return breakdown;
    }

    public List<CostBreakdown> listCostBreakdownsByEvent(String tenantId, String costEventId) {
        return mapper.selectCostBreakdownsByEvent(tenantId, costEventId).stream().map(this::toBreakdownDomain).collect(Collectors.toList());
    }

    public List<CostBreakdown> listCostBreakdownsByType(String tenantId, String costType) {
        return mapper.selectCostBreakdownsByType(tenantId, costType).stream().map(this::toBreakdownDomain).collect(Collectors.toList());
    }

    public JournalEntry saveJournalEntry(JournalEntry entry) {
        mapper.insertJournalEntry(toJournalEntryData(entry));
        return entry;
    }

    public List<JournalEntry> listJournalEntriesByReference(String tenantId, String referenceType, String referenceId) {
        return mapper.selectJournalEntriesByReference(tenantId, referenceType, referenceId).stream().map(this::toJournalEntryDomain).collect(Collectors.toList());
    }

    public List<JournalEntry> listJournalEntriesByAccount(String tenantId, String accountCode) {
        return mapper.selectJournalEntriesByAccount(tenantId, accountCode).stream().map(this::toJournalEntryDomain).collect(Collectors.toList());
    }

    public TaxRule saveTaxRule(TaxRule rule) {
        TaxRuleDO existing = mapper.selectTaxRule(rule.tenantId(), rule.ruleId());
        TaxRuleDO data = toTaxRuleData(rule);
        if (existing == null) {
            mapper.insertTaxRule(data);
        } else {
            mapper.updateTaxRule(data);
        }
        return rule;
    }

    public Optional<TaxRule> findTaxRule(String tenantId, String ruleId) {
        return Optional.ofNullable(mapper.selectTaxRule(tenantId, ruleId)).map(this::toTaxRuleDomain);
    }

    public List<TaxRule> listTaxRules(String tenantId, String countryCode) {
        return mapper.selectTaxRules(tenantId, countryCode).stream().map(this::toTaxRuleDomain).collect(Collectors.toList());
    }

    public Optional<TaxRule> findActiveTaxRule(String tenantId, String countryCode, String taxType) {
        return Optional.ofNullable(mapper.selectActiveTaxRule(tenantId, countryCode, taxType)).map(this::toTaxRuleDomain);
    }

    private CostBreakdownDO toBreakdownData(CostBreakdown b) {
        CostBreakdownDO data = new CostBreakdownDO();
        data.setBreakdownId(b.breakdownId());
        data.setTenantId(b.tenantId());
        data.setCostEventId(b.costEventId());
        data.setCostType(b.costType());
        data.setCostCategory(b.costCategory());
        data.setAmount(b.amount());
        data.setCurrency(b.currency());
        data.setExchangeRate(b.exchangeRate());
        data.setAmountInBaseCurrency(b.amountInBaseCurrency());
        data.setRemark(b.remark());
        data.setCreatedAt(b.createdAt() != null ? b.createdAt() : Instant.now());
        return data;
    }

    private CostBreakdown toBreakdownDomain(CostBreakdownDO d) {
        return new CostBreakdown(d.getBreakdownId(), d.getTenantId(), d.getCostEventId(), d.getCostType(),
                d.getCostCategory(), d.getAmount(), d.getCurrency(), d.getExchangeRate(), d.getAmountInBaseCurrency(), d.getRemark(), d.getCreatedAt());
    }

    private JournalEntryDO toJournalEntryData(JournalEntry e) {
        JournalEntryDO data = new JournalEntryDO();
        data.setEntryId(e.entryId());
        data.setTenantId(e.tenantId());
        data.setAccountCode(e.accountCode());
        data.setAccountName(e.accountName());
        data.setType(e.type().name());
        data.setAmount(e.amount());
        data.setCurrency(e.currency());
        data.setReferenceType(e.referenceType());
        data.setReferenceId(e.referenceId());
        data.setRemark(e.remark());
        data.setEntryDate(e.entryDate());
        data.setCreatedAt(e.createdAt() != null ? e.createdAt() : Instant.now());
        return data;
    }

    private JournalEntry toJournalEntryDomain(JournalEntryDO d) {
        return new JournalEntry(d.getEntryId(), d.getTenantId(), d.getAccountCode(), d.getAccountName(),
                JournalEntryType.valueOf(d.getType()), d.getAmount(), d.getCurrency(), d.getReferenceType(),
                d.getReferenceId(), d.getRemark(), d.getEntryDate(), d.getCreatedAt());
    }

    private TaxRuleDO toTaxRuleData(TaxRule r) {
        TaxRuleDO data = new TaxRuleDO();
        data.setRuleId(r.ruleId());
        data.setTenantId(r.tenantId());
        data.setCountryCode(r.countryCode());
        data.setTaxType(r.taxType());
        data.setTaxRate(r.taxRate());
        data.setTaxCategory(r.taxCategory());
        data.setEnabled(r.enabled());
        data.setEffectiveFrom(r.effectiveFrom());
        data.setEffectiveTo(r.effectiveTo());
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private TaxRule toTaxRuleDomain(TaxRuleDO d) {
        return new TaxRule(d.getRuleId(), d.getTenantId(), d.getCountryCode(), d.getTaxType(), d.getTaxRate(),
                d.getTaxCategory(), d.isEnabled(), d.getEffectiveFrom(), d.getEffectiveTo(), d.getCreatedAt(), d.getUpdatedAt());
    }

    public Invoice saveInvoice(Invoice invoice) {
        InvoiceDO existing = mapper.selectInvoice(invoice.tenantId(), invoice.invoiceId());
        InvoiceDO data = toInvoiceData(invoice);
        if (existing == null) {
            mapper.insertInvoice(data);
        } else {
            mapper.updateInvoice(data);
        }
        return invoice;
    }

    public Optional<Invoice> findInvoice(String tenantId, String invoiceId) {
        return Optional.ofNullable(mapper.selectInvoice(tenantId, invoiceId)).map(this::toInvoiceDomain);
    }

    public List<Invoice> listInvoices(String tenantId, String countryCode, String status) {
        return mapper.selectInvoices(tenantId, countryCode, status).stream().map(this::toInvoiceDomain).collect(Collectors.toList());
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
        return mapper.selectVouchers(tenantId, voucherType, status).stream().map(this::toVoucherDomain).collect(Collectors.toList());
    }

    public List<Voucher> listVouchersByReference(String tenantId, String referenceType, String referenceId) {
        return mapper.selectVouchersByReference(tenantId, referenceType, referenceId).stream().map(this::toVoucherDomain).collect(Collectors.toList());
    }

    private InvoiceDO toInvoiceData(Invoice i) {
        InvoiceDO data = new InvoiceDO();
        data.setInvoiceId(i.invoiceId());
        data.setTenantId(i.tenantId());
        data.setInvoiceNumber(i.invoiceNumber());
        data.setInvoiceType(i.invoiceType());
        data.setCustomerId(i.customerId());
        data.setCustomerName(i.customerName());
        data.setCountryCode(i.countryCode());
        data.setCurrency(i.currency());
        data.setSubtotalAmount(i.subtotalAmount());
        data.setTaxAmount(i.taxAmount());
        data.setTotalAmount(i.totalAmount());
        data.setTaxIdNumber(i.taxIdNumber());
        data.setStatus(i.status());
        data.setVoucherId(i.voucherId());
        data.setInvoiceDate(i.invoiceDate());
        data.setDueDate(i.dueDate());
        data.setRemark(i.remark());
        data.setCreatedAt(i.createdAt() != null ? i.createdAt() : Instant.now());
        data.setUpdatedAt(i.updatedAt() != null ? i.updatedAt() : Instant.now());
        return data;
    }

    private Invoice toInvoiceDomain(InvoiceDO d) {
        return new Invoice(d.getInvoiceId(), d.getTenantId(), d.getInvoiceNumber(), d.getInvoiceType(),
                d.getCustomerId(), d.getCustomerName(), d.getCountryCode(), d.getCurrency(),
                d.getSubtotalAmount(), d.getTaxAmount(), d.getTotalAmount(), d.getTaxIdNumber(),
                d.getStatus(), d.getVoucherId(), d.getInvoiceDate(), d.getDueDate(), d.getRemark(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private VoucherDO toVoucherData(Voucher v) {
        VoucherDO data = new VoucherDO();
        data.setVoucherId(v.voucherId());
        data.setTenantId(v.tenantId());
        data.setVoucherNumber(v.voucherNumber());
        data.setVoucherType(v.voucherType());
        data.setReferenceType(v.referenceType());
        data.setReferenceId(v.referenceId());
        data.setCurrency(v.currency());
        data.setTotalDebit(v.totalDebit());
        data.setTotalCredit(v.totalCredit());
        data.setStatus(v.status().name());
        try {
            data.setLinesJson(objectMapper.writeValueAsString(v.lines()));
        } catch (JsonProcessingException e) {
            data.setLinesJson("[]");
        }
        data.setVoucherDate(v.voucherDate());
        data.setPostedBy(v.postedBy());
        data.setPostedAt(v.postedAt());
        data.setExportBatchId(v.exportBatchId());
        data.setCreatedAt(v.createdAt() != null ? v.createdAt() : Instant.now());
        data.setUpdatedAt(v.updatedAt() != null ? v.updatedAt() : Instant.now());
        return data;
    }

    private Voucher toVoucherDomain(VoucherDO d) {
        List<VoucherLine> lines;
        try {
            lines = objectMapper.readValue(d.getLinesJson(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            lines = List.of();
        }
        return new Voucher(d.getVoucherId(), d.getTenantId(), d.getVoucherNumber(), d.getVoucherType(),
                d.getReferenceType(), d.getReferenceId(), d.getCurrency(), d.getTotalDebit(), d.getTotalCredit(),
                VoucherStatus.valueOf(d.getStatus()), lines, d.getVoucherDate(), d.getPostedBy(), d.getPostedAt(),
                d.getExportBatchId(), d.getCreatedAt(), d.getUpdatedAt());
    }

    public CostAggregationRule saveCostAggregationRule(CostAggregationRule rule) {
        CostAggregationRuleDO existing = mapper.selectCostAggregationRule(rule.tenantId(), rule.ruleId());
        CostAggregationRuleDO data = toCostAggregationRuleData(rule);
        if (existing == null) {
            mapper.insertCostAggregationRule(data);
        } else {
            mapper.updateCostAggregationRule(data);
        }
        return rule;
    }

    public Optional<CostAggregationRule> findCostAggregationRule(String tenantId, String ruleId) {
        return Optional.ofNullable(mapper.selectCostAggregationRule(tenantId, ruleId)).map(this::toCostAggregationRuleDomain);
    }

    public List<CostAggregationRule> listCostAggregationRules(String tenantId, String costSource) {
        return mapper.selectCostAggregationRules(tenantId, costSource).stream().map(this::toCostAggregationRuleDomain).collect(Collectors.toList());
    }

    public CostAllocationResult saveCostAllocationResult(CostAllocationResult result) {
        mapper.insertCostAllocationResult(toCostAllocationResultData(result));
        return result;
    }

    public List<CostAllocationResult> listCostAllocationResults(String tenantId, String targetDimension, String targetId) {
        return mapper.selectCostAllocationResults(tenantId, targetDimension, targetId).stream().map(this::toCostAllocationResultDomain).collect(Collectors.toList());
    }

    public List<CostAllocationResult> listCostAllocationResultsBySku(String tenantId, String sellerSku) {
        return mapper.selectCostAllocationResultsBySku(tenantId, sellerSku).stream().map(this::toCostAllocationResultDomain).collect(Collectors.toList());
    }

    public List<CostAllocationResult> listCostAllocationResultsByTarget(String tenantId, String dimensionType, String dimensionId) {
        return mapper.selectCostAllocationResultsByTarget(tenantId, dimensionType, dimensionId).stream().map(this::toCostAllocationResultDomain).collect(Collectors.toList());
    }

    public ProfitResult saveProfitResult(ProfitResult result) {
        mapper.insertProfitResult(toProfitResultData(result));
        return result;
    }

    public Optional<ProfitResult> findProfitResult(String tenantId, String resultId) {
        return Optional.ofNullable(mapper.selectProfitResult(tenantId, resultId)).map(this::toProfitResultDomain);
    }

    public List<ProfitResult> listProfitResults(String tenantId, String dimensionType, String dimensionId) {
        return mapper.selectProfitResults(tenantId, dimensionType, dimensionId).stream().map(this::toProfitResultDomain).collect(Collectors.toList());
    }

    public List<ProfitResult> listProfitResultsBySku(String tenantId, String sellerSku) {
        return mapper.selectProfitResultsBySku(tenantId, sellerSku).stream().map(this::toProfitResultDomain).collect(Collectors.toList());
    }

    public List<ProfitResult> listAllProfitResults(String tenantId) {
        return mapper.selectAllProfitResults(tenantId).stream().map(this::toProfitResultDomain).collect(Collectors.toList());
    }

    public List<ProfitResult> listProfitResultsByDimension(String tenantId, String dimensionType) {
        return mapper.selectProfitResultsByDimension(tenantId, dimensionType).stream().map(this::toProfitResultDomain).collect(Collectors.toList());
    }

    public ProfitDeviationAlert saveProfitDeviationAlert(ProfitDeviationAlert alert) {
        ProfitDeviationAlertDO existing = mapper.selectProfitDeviationAlert(alert.tenantId(), alert.alertId());
        ProfitDeviationAlertDO data = toProfitDeviationAlertData(alert);
        if (existing == null) {
            mapper.insertProfitDeviationAlert(data);
        } else {
            mapper.updateProfitDeviationAlert(data);
        }
        return alert;
    }

    public Optional<ProfitDeviationAlert> findProfitDeviationAlert(String tenantId, String alertId) {
        return Optional.ofNullable(mapper.selectProfitDeviationAlert(tenantId, alertId)).map(this::toProfitDeviationAlertDomain);
    }

    public List<ProfitDeviationAlert> listProfitDeviationAlerts(String tenantId, String status) {
        return mapper.selectProfitDeviationAlerts(tenantId, status).stream().map(this::toProfitDeviationAlertDomain).collect(Collectors.toList());
    }

    private CostAggregationRuleDO toCostAggregationRuleData(CostAggregationRule r) {
        CostAggregationRuleDO data = new CostAggregationRuleDO();
        data.setRuleId(r.ruleId());
        data.setTenantId(r.tenantId());
        data.setRuleName(r.ruleName());
        data.setCostSource(r.costSource());
        data.setCostCategory(r.costCategory());
        data.setAllocationMethod(r.allocationMethod());
        data.setAllocationBasis(r.allocationBasis());
        data.setTargetDimension(r.targetDimension());
        data.setEnabled(r.enabled());
        data.setPriority(r.priority());
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private CostAggregationRule toCostAggregationRuleDomain(CostAggregationRuleDO d) {
        return new CostAggregationRule(d.getRuleId(), d.getTenantId(), d.getRuleName(), d.getCostSource(),
                d.getCostCategory(), d.getAllocationMethod(), d.getAllocationBasis(), d.getTargetDimension(),
                d.isEnabled(), d.getPriority(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private CostAllocationResultDO toCostAllocationResultData(CostAllocationResult r) {
        CostAllocationResultDO data = new CostAllocationResultDO();
        data.setResultId(r.resultId());
        data.setTenantId(r.tenantId());
        data.setRuleId(r.ruleId());
        data.setCostEventId(r.costEventId());
        data.setTargetDimension(r.targetDimension());
        data.setTargetId(r.targetId());
        data.setAllocatedAmount(r.allocatedAmount());
        data.setCurrency(r.currency());
        data.setExchangeRate(r.exchangeRate());
        data.setAmountInBaseCurrency(r.amountInBaseCurrency());
        try {
            data.setDimensionsJson(r.dimensions() != null ? objectMapper.writeValueAsString(r.dimensions()) : "{}");
        } catch (JsonProcessingException e) {
            data.setDimensionsJson("{}");
        }
        data.setAllocatedAt(r.allocatedAt() != null ? r.allocatedAt() : Instant.now());
        return data;
    }

    private CostAllocationResult toCostAllocationResultDomain(CostAllocationResultDO d) {
        Map<String, String> dimensions;
        try {
            dimensions = objectMapper.readValue(d.getDimensionsJson() != null ? d.getDimensionsJson() : "{}", new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            dimensions = Map.of();
        }
        return new CostAllocationResult(d.getResultId(), d.getTenantId(), d.getRuleId(), d.getCostEventId(),
                d.getTargetDimension(), d.getTargetId(), d.getAllocatedAmount(), d.getCurrency(),
                d.getExchangeRate(), d.getAmountInBaseCurrency(), dimensions, d.getAllocatedAt());
    }

    private ProfitResultDO toProfitResultData(ProfitResult r) {
        ProfitResultDO data = new ProfitResultDO();
        data.setResultId(r.resultId());
        data.setTenantId(r.tenantId());
        data.setDimensionType(r.dimensionType());
        data.setDimensionId(r.dimensionId());
        data.setSellerSku(r.sellerSku());
        data.setOrderId(r.orderId());
        data.setStoreId(r.storeId());
        data.setMarketplaceId(r.marketplaceId());
        data.setRevenue(r.revenue());
        data.setProductCost(r.productCost());
        data.setShippingCost(r.shippingCost());
        data.setFbaFee(r.fbaFee());
        data.setCommission(r.commission());
        data.setAdvertisingCost(r.advertisingCost());
        data.setReturnCost(r.returnCost());
        data.setStorageFee(r.storageFee());
        data.setPackagingCost(r.packagingCost());
        data.setCustomDuty(r.customDuty());
        data.setOtherCost(r.otherCost());
        data.setTotalCost(r.totalCost());
        data.setGrossProfit(r.grossProfit());
        data.setGrossMargin(r.grossMargin());
        data.setCurrency(r.currency());
        data.setExchangeRate(r.exchangeRate());
        data.setAmountInBaseCurrency(r.amountInBaseCurrency());
        try {
            data.setCostDetailsJson(r.costDetails() != null ? objectMapper.writeValueAsString(r.costDetails()) : "{}");
        } catch (JsonProcessingException e) {
            data.setCostDetailsJson("{}");
        }
        data.setCalculatedAt(r.calculatedAt() != null ? r.calculatedAt() : Instant.now());
        return data;
    }

    private ProfitResult toProfitResultDomain(ProfitResultDO d) {
        Map<String, BigDecimal> costDetails;
        try {
            costDetails = objectMapper.readValue(d.getCostDetailsJson() != null ? d.getCostDetailsJson() : "{}", new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            costDetails = Map.of();
        }
        return new ProfitResult(d.getResultId(), d.getTenantId(), d.getDimensionType(), d.getDimensionId(),
                d.getSellerSku(), d.getOrderId(), d.getStoreId(), d.getMarketplaceId(), d.getRevenue(),
                d.getProductCost(), d.getShippingCost(), d.getFbaFee(), d.getCommission(), d.getAdvertisingCost(),
                d.getReturnCost(), d.getStorageFee(), d.getPackagingCost(), d.getCustomDuty(), d.getOtherCost(),
                d.getTotalCost(), d.getGrossProfit(), d.getGrossMargin(), d.getCurrency(), d.getExchangeRate(),
                d.getAmountInBaseCurrency(), d.getCalculatedAt(), costDetails);
    }

    private ProfitDeviationAlertDO toProfitDeviationAlertData(ProfitDeviationAlert a) {
        ProfitDeviationAlertDO data = new ProfitDeviationAlertDO();
        data.setAlertId(a.alertId());
        data.setTenantId(a.tenantId());
        data.setDimensionType(a.dimensionType());
        data.setDimensionId(a.dimensionId());
        data.setSellerSku(a.sellerSku());
        data.setExpectedMargin(a.expectedMargin());
        data.setActualMargin(a.actualMargin());
        data.setDeviation(a.deviation());
        data.setDeviationThreshold(a.deviationThreshold());
        data.setSeverity(a.severity());
        data.setStatus(a.status());
        data.setDetectedAt(a.detectedAt());
        data.setResolvedAt(a.resolvedAt());
        return data;
    }

    private ProfitDeviationAlert toProfitDeviationAlertDomain(ProfitDeviationAlertDO d) {
        return new ProfitDeviationAlert(d.getAlertId(), d.getTenantId(), d.getDimensionType(), d.getDimensionId(),
                d.getSellerSku(), d.getExpectedMargin(), d.getActualMargin(), d.getDeviation(),
                d.getDeviationThreshold(), d.getSeverity(), d.getStatus(), d.getDetectedAt(), d.getResolvedAt());
    }

    public CurrencyRate saveCurrencyRate(CurrencyRate rate) {
        mapper.insertCurrencyRate(toCurrencyRateData(rate));
        return rate;
    }

    public List<CurrencyRate> listCurrencyRates(String tenantId, String fromCurrency, String toCurrency) {
        return mapper.selectCurrencyRates(tenantId, fromCurrency, toCurrency).stream().map(this::toCurrencyRateDomain).collect(Collectors.toList());
    }

    public List<CurrencyRate> listCurrencyRatesByDate(String tenantId, String fromCurrency, String toCurrency, java.time.LocalDate rateDate) {
        return mapper.selectCurrencyRatesByDate(tenantId, fromCurrency, toCurrency, rateDate).stream().map(this::toCurrencyRateDomain).collect(Collectors.toList());
    }

    public List<CurrencyRate> listCurrencyRateHistory(String tenantId, String fromCurrency, String toCurrency,
                                                       java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return mapper.selectCurrencyRateHistory(tenantId, fromCurrency, toCurrency, startDate, endDate).stream().map(this::toCurrencyRateDomain).collect(Collectors.toList());
    }

    public CurrencyRateSyncLog saveCurrencyRateSyncLog(CurrencyRateSyncLog syncLog) {
        mapper.insertCurrencyRateSyncLog(toCurrencyRateSyncLogData(syncLog));
        return syncLog;
    }

    public List<CurrencyRateSyncLog> listCurrencyRateSyncLogs(String tenantId) {
        return mapper.selectCurrencyRateSyncLogs(tenantId).stream().map(this::toCurrencyRateSyncLogDomain).collect(Collectors.toList());
    }

    private CurrencyRateDO toCurrencyRateData(CurrencyRate r) {
        CurrencyRateDO data = new CurrencyRateDO();
        data.setRateId(r.rateId());
        data.setTenantId(r.tenantId());
        data.setFromCurrency(r.fromCurrency());
        data.setToCurrency(r.toCurrency());
        data.setRate(r.rate());
        data.setSource(r.source());
        data.setRateDate(r.rateDate());
        data.setSyncedAt(r.syncedAt());
        return data;
    }

    private CurrencyRate toCurrencyRateDomain(CurrencyRateDO d) {
        return new CurrencyRate(d.getRateId(), d.getTenantId(), d.getFromCurrency(), d.getToCurrency(),
                d.getRate(), d.getSource(), d.getRateDate(), d.getSyncedAt());
    }

    private CurrencyRateSyncLogDO toCurrencyRateSyncLogData(CurrencyRateSyncLog s) {
        CurrencyRateSyncLogDO data = new CurrencyRateSyncLogDO();
        data.setSyncId(s.syncId());
        data.setTenantId(s.tenantId());
        data.setSource(s.source());
        data.setStatus(s.status());
        data.setTotalRates(s.totalRates());
        data.setSuccessCount(s.successCount());
        data.setFailCount(s.failCount());
        data.setStartedAt(s.startedAt());
        data.setCompletedAt(s.completedAt());
        data.setErrorMessage(s.errorMessage());
        return data;
    }

    private CurrencyRateSyncLog toCurrencyRateSyncLogDomain(CurrencyRateSyncLogDO d) {
        return new CurrencyRateSyncLog(d.getSyncId(), d.getTenantId(), d.getSource(), d.getStatus(),
                d.getTotalRates(), d.getSuccessCount(), d.getFailCount(), d.getStartedAt(), d.getCompletedAt(), d.getErrorMessage());
    }

    public BillingRule saveBillingRule(BillingRule rule) {
        BillingRuleDO existing = mapper.selectBillingRule(rule.tenantId(), rule.ruleId());
        BillingRuleDO data = toBillingRuleData(rule);
        if (existing == null) {
            mapper.insertBillingRule(data);
        } else {
            mapper.updateBillingRule(data);
        }
        return rule;
    }

    public Optional<BillingRule> findBillingRule(String tenantId, String ruleId) {
        return Optional.ofNullable(mapper.selectBillingRule(tenantId, ruleId)).map(this::toBillingRuleDomain);
    }

    public List<BillingRule> listBillingRules(String tenantId, String feeType, String platform) {
        return mapper.selectBillingRules(tenantId, feeType, platform).stream().map(this::toBillingRuleDomain).collect(Collectors.toList());
    }

    private BillingRuleDO toBillingRuleData(BillingRule r) {
        BillingRuleDO data = new BillingRuleDO();
        data.setRuleId(r.ruleId());
        data.setTenantId(r.tenantId());
        data.setRuleName(r.ruleName());
        data.setFeeType(r.feeType());
        data.setPlatform(r.platform());
        data.setCategory(r.category());
        data.setRate(r.rate());
        data.setMinAmount(r.minAmount());
        data.setMaxAmount(r.maxAmount());
        data.setCalculationMethod(r.calculationMethod());
        data.setEnabled(r.enabled());
        data.setPriority(r.priority());
        data.setCreatedAt(r.createdAt());
        data.setUpdatedAt(r.updatedAt());
        return data;
    }

    private BillingRule toBillingRuleDomain(BillingRuleDO d) {
        return new BillingRule(d.getRuleId(), d.getTenantId(), d.getRuleName(), d.getFeeType(),
                d.getPlatform(), d.getCategory(), d.getRate(), d.getMinAmount(), d.getMaxAmount(),
                d.getCalculationMethod(), d.isEnabled(), d.getPriority(), d.getCreatedAt(), d.getUpdatedAt());
    }

    public VoucherTemplate saveVoucherTemplate(VoucherTemplate template) {
        VoucherTemplateDO existing = mapper.selectVoucherTemplate(template.tenantId(), template.templateId());
        VoucherTemplateDO data = toVoucherTemplateData(template);
        if (existing == null) {
            mapper.insertVoucherTemplate(data);
        } else {
            mapper.updateVoucherTemplate(data);
        }
        return template;
    }

    public List<VoucherTemplate> listVoucherTemplates(String tenantId, String businessType) {
        return mapper.selectVoucherTemplates(tenantId, businessType).stream().map(this::toVoucherTemplateDomain).collect(Collectors.toList());
    }

    private VoucherTemplateDO toVoucherTemplateData(VoucherTemplate t) {
        VoucherTemplateDO data = new VoucherTemplateDO();
        data.setTemplateId(t.templateId());
        data.setTenantId(t.tenantId());
        data.setTemplateName(t.templateName());
        data.setBusinessType(t.businessType());
        data.setDebitAccount(t.debitAccount());
        data.setCreditAccount(t.creditAccount());
        data.setDescription(t.description());
        data.setEnabled(t.enabled());
        data.setCreatedAt(t.createdAt());
        data.setUpdatedAt(t.updatedAt());
        return data;
    }

    private VoucherTemplate toVoucherTemplateDomain(VoucherTemplateDO d) {
        return new VoucherTemplate(d.getTemplateId(), d.getTenantId(), d.getTemplateName(), d.getBusinessType(),
                d.getDebitAccount(), d.getCreditAccount(), d.getDescription(), d.isEnabled(), d.getCreatedAt(), d.getUpdatedAt());
    }

    public RiskAssessment saveRiskAssessment(RiskAssessment assessment) {
        mapper.insertRiskAssessment(toRiskAssessmentData(assessment));
        return assessment;
    }

    public List<RiskAssessment> listRiskAssessments(String tenantId, String targetType, String riskLevel) {
        return mapper.selectRiskAssessments(tenantId, targetType, riskLevel).stream().map(this::toRiskAssessmentDomain).collect(Collectors.toList());
    }

    public FraudDetectionResult saveFraudDetectionResult(FraudDetectionResult result) {
        mapper.insertFraudDetectionResult(toFraudDetectionResultData(result));
        return result;
    }

    public List<FraudDetectionResult> listFraudDetectionResults(String tenantId, String status) {
        return mapper.selectFraudDetectionResults(tenantId, status).stream().map(this::toFraudDetectionResultDomain).collect(Collectors.toList());
    }

    public VatComplianceStatus saveVatComplianceStatus(VatComplianceStatus status) {
        VatComplianceStatusDO existing = mapper.selectVatComplianceStatus(status.tenantId(), status.countryCode());
        VatComplianceStatusDO data = toVatComplianceStatusData(status);
        if (existing == null) {
            mapper.insertVatComplianceStatus(data);
        } else {
            mapper.updateVatComplianceStatus(data);
        }
        return status;
    }

    public Optional<VatComplianceStatus> findVatComplianceStatus(String tenantId, String countryCode) {
        return Optional.ofNullable(mapper.selectVatComplianceStatus(tenantId, countryCode)).map(this::toVatComplianceStatusDomain);
    }

    public List<VatComplianceStatus> listVatComplianceStatuses(String tenantId) {
        return mapper.selectVatComplianceStatuses(tenantId).stream().map(this::toVatComplianceStatusDomain).collect(Collectors.toList());
    }

    private RiskAssessmentDO toRiskAssessmentData(RiskAssessment a) {
        RiskAssessmentDO data = new RiskAssessmentDO();
        data.setAssessmentId(a.assessmentId());
        data.setTenantId(a.tenantId());
        data.setTargetType(a.targetType());
        data.setTargetId(a.targetId());
        data.setRiskScore(a.riskScore());
        data.setRiskLevel(a.riskLevel());
        try { data.setRiskFactorsJson(objectMapper.writeValueAsString(a.riskFactors())); } catch (Exception e) { data.setRiskFactorsJson("{}"); }
        data.setRecommendation(a.recommendation());
        data.setAssessedAt(a.assessedAt());
        return data;
    }

    private RiskAssessment toRiskAssessmentDomain(RiskAssessmentDO d) {
        Map<String, Object> factors;
        try { factors = objectMapper.readValue(d.getRiskFactorsJson() != null ? d.getRiskFactorsJson() : "{}", new TypeReference<>() {}); } catch (Exception e) { factors = Map.of(); }
        return new RiskAssessment(d.getAssessmentId(), d.getTenantId(), d.getTargetType(), d.getTargetId(),
                d.getRiskScore(), d.getRiskLevel(), factors, d.getRecommendation(), d.getAssessedAt());
    }

    private FraudDetectionResultDO toFraudDetectionResultData(FraudDetectionResult r) {
        FraudDetectionResultDO data = new FraudDetectionResultDO();
        data.setResultId(r.resultId());
        data.setTenantId(r.tenantId());
        data.setOrderId(r.orderId());
        data.setBuyerId(r.buyerId());
        data.setDetectionType(r.detectionType());
        data.setSeverity(r.severity());
        data.setDescription(r.description());
        try { data.setIndicatorsJson(objectMapper.writeValueAsString(r.indicators())); } catch (Exception e) { data.setIndicatorsJson("{}"); }
        data.setStatus(r.status());
        data.setDetectedAt(r.detectedAt());
        return data;
    }

    private FraudDetectionResult toFraudDetectionResultDomain(FraudDetectionResultDO d) {
        Map<String, Object> indicators;
        try { indicators = objectMapper.readValue(d.getIndicatorsJson() != null ? d.getIndicatorsJson() : "{}", new TypeReference<>() {}); } catch (Exception e) { indicators = Map.of(); }
        return new FraudDetectionResult(d.getResultId(), d.getTenantId(), d.getOrderId(), d.getBuyerId(),
                d.getDetectionType(), d.getSeverity(), d.getDescription(), indicators, d.getStatus(), d.getDetectedAt());
    }

    private VatComplianceStatusDO toVatComplianceStatusData(VatComplianceStatus s) {
        VatComplianceStatusDO data = new VatComplianceStatusDO();
        data.setStatusId(s.statusId());
        data.setTenantId(s.tenantId());
        data.setCountryCode(s.countryCode());
        data.setVatNumber(s.vatNumber());
        data.setRegistrationStatus(s.registrationStatus());
        data.setFilingStatus(s.filingStatus());
        data.setNextFilingDate(s.nextFilingDate());
        data.setRegistrationDate(s.registrationDate());
        data.setExpiryDate(s.expiryDate());
        try { data.setDetailsJson(objectMapper.writeValueAsString(s.details())); } catch (Exception e) { data.setDetailsJson("{}"); }
        data.setUpdatedAt(s.updatedAt());
        return data;
    }

    private VatComplianceStatus toVatComplianceStatusDomain(VatComplianceStatusDO d) {
        Map<String, String> details;
        try { details = objectMapper.readValue(d.getDetailsJson() != null ? d.getDetailsJson() : "{}", new TypeReference<>() {}); } catch (Exception e) { details = Map.of(); }
        return new VatComplianceStatus(d.getStatusId(), d.getTenantId(), d.getCountryCode(), d.getVatNumber(),
                d.getRegistrationStatus(), d.getFilingStatus(), d.getNextFilingDate(), d.getRegistrationDate(),
                d.getExpiryDate(), details, d.getUpdatedAt());
    }

    public TradeComplianceResult saveTradeComplianceResult(TradeComplianceResult result) {
        TradeComplianceResultDO data = toTradeComplianceResultData(result);
        mapper.insertTradeComplianceResult(data);
        return result;
    }

    public List<TradeComplianceResult> listTradeComplianceResults(String tenantId, String status) {
        return mapper.selectTradeComplianceResults(tenantId, status).stream()
                .map(this::toTradeComplianceResultDomain).collect(Collectors.toList());
    }

    public PlatformComplianceResult savePlatformComplianceResult(PlatformComplianceResult result) {
        PlatformComplianceResultDO data = toPlatformComplianceResultData(result);
        mapper.insertPlatformComplianceResult(data);
        return result;
    }

    public List<PlatformComplianceResult> listPlatformComplianceResults(String tenantId, String platform, String status) {
        return mapper.selectPlatformComplianceResults(tenantId, platform, status).stream()
                .map(this::toPlatformComplianceResultDomain).collect(Collectors.toList());
    }

    private TradeComplianceResultDO toTradeComplianceResultData(TradeComplianceResult r) {
        TradeComplianceResultDO data = new TradeComplianceResultDO();
        data.setResultId(r.resultId());
        data.setTenantId(r.tenantId());
        data.setOrderId(r.orderId());
        data.setSellerSku(r.sellerSku());
        data.setHsCode(r.hsCode());
        data.setOriginCountry(r.originCountry());
        data.setDestinationCountry(r.destinationCountry());
        data.setStatus(r.status());
        try { data.setViolationsJson(objectMapper.writeValueAsString(r.violations())); } catch (Exception e) { data.setViolationsJson("[]"); }
        try { data.setWarningsJson(objectMapper.writeValueAsString(r.warnings())); } catch (Exception e) { data.setWarningsJson("[]"); }
        try { data.setDetailsJson(objectMapper.writeValueAsString(r.details())); } catch (Exception e) { data.setDetailsJson("{}"); }
        data.setCheckedAt(r.checkedAt());
        return data;
    }

    private TradeComplianceResult toTradeComplianceResultDomain(TradeComplianceResultDO d) {
        List<String> violations;
        try { violations = objectMapper.readValue(d.getViolationsJson() != null ? d.getViolationsJson() : "[]", new TypeReference<>() {}); } catch (Exception e) { violations = List.of(); }
        List<String> warnings;
        try { warnings = objectMapper.readValue(d.getWarningsJson() != null ? d.getWarningsJson() : "[]", new TypeReference<>() {}); } catch (Exception e) { warnings = List.of(); }
        Map<String, Object> details;
        try { details = objectMapper.readValue(d.getDetailsJson() != null ? d.getDetailsJson() : "{}", new TypeReference<>() {}); } catch (Exception e) { details = Map.of(); }
        return new TradeComplianceResult(d.getResultId(), d.getTenantId(), d.getOrderId(), d.getSellerSku(),
                d.getHsCode(), d.getOriginCountry(), d.getDestinationCountry(), d.getStatus(),
                violations, warnings, details, d.getCheckedAt());
    }

    private PlatformComplianceResultDO toPlatformComplianceResultData(PlatformComplianceResult r) {
        PlatformComplianceResultDO data = new PlatformComplianceResultDO();
        data.setResultId(r.resultId());
        data.setTenantId(r.tenantId());
        data.setPlatform(r.platform());
        data.setListingId(r.listingId());
        data.setSellerSku(r.sellerSku());
        data.setStatus(r.status());
        try { data.setViolationsJson(objectMapper.writeValueAsString(r.violations())); } catch (Exception e) { data.setViolationsJson("[]"); }
        try { data.setWarningsJson(objectMapper.writeValueAsString(r.warnings())); } catch (Exception e) { data.setWarningsJson("[]"); }
        try { data.setDetailsJson(objectMapper.writeValueAsString(r.details())); } catch (Exception e) { data.setDetailsJson("{}"); }
        data.setCheckedAt(r.checkedAt());
        return data;
    }

    private PlatformComplianceResult toPlatformComplianceResultDomain(PlatformComplianceResultDO d) {
        List<String> violations;
        try { violations = objectMapper.readValue(d.getViolationsJson() != null ? d.getViolationsJson() : "[]", new TypeReference<>() {}); } catch (Exception e) { violations = List.of(); }
        List<String> warnings;
        try { warnings = objectMapper.readValue(d.getWarningsJson() != null ? d.getWarningsJson() : "[]", new TypeReference<>() {}); } catch (Exception e) { warnings = List.of(); }
        Map<String, Object> details;
        try { details = objectMapper.readValue(d.getDetailsJson() != null ? d.getDetailsJson() : "{}", new TypeReference<>() {}); } catch (Exception e) { details = Map.of(); }
        return new PlatformComplianceResult(d.getResultId(), d.getTenantId(), d.getPlatform(), d.getListingId(),
                d.getSellerSku(), d.getStatus(), violations, warnings, details, d.getCheckedAt());
    }

    private final Map<String, ExternalFinanceVoucher> extFinanceVoucherStore = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, FinanceSyncConfig> financeSyncConfigStore = new java.util.concurrent.ConcurrentHashMap<>();

    public void saveExternalFinanceVoucher(ExternalFinanceVoucher voucher) {
        extFinanceVoucherStore.put(voucher.voucherId(), voucher);
    }

    public Optional<ExternalFinanceVoucher> findExternalFinanceVoucher(String tenantId, String voucherId) {
        return extFinanceVoucherStore.values().stream()
                .filter(v -> v.tenantId().equals(tenantId) && v.voucherId().equals(voucherId))
                .findFirst();
    }

    public List<ExternalFinanceVoucher> listExternalFinanceVouchers(String tenantId, String syncStatus) {
        return extFinanceVoucherStore.values().stream()
                .filter(v -> v.tenantId().equals(tenantId))
                .filter(v -> syncStatus == null || v.syncStatus().equals(syncStatus))
                .collect(Collectors.toList());
    }

    public void saveFinanceSyncConfig(FinanceSyncConfig config) {
        financeSyncConfigStore.put(config.configId(), config);
    }

    public Optional<FinanceSyncConfig> findFinanceSyncConfig(String tenantId, String configId) {
        return financeSyncConfigStore.values().stream()
                .filter(c -> c.tenantId().equals(tenantId) && c.configId().equals(configId))
                .findFirst();
    }

    public Optional<FinanceSyncConfig> findFinanceSyncConfigBySystem(String tenantId, String financeSystem) {
        return financeSyncConfigStore.values().stream()
                .filter(c -> c.tenantId().equals(tenantId) && c.financeSystem().equals(financeSystem))
                .findFirst();
    }

    public List<FinanceSyncConfig> listFinanceSyncConfigs(String tenantId) {
        return financeSyncConfigStore.values().stream()
                .filter(c -> c.tenantId().equals(tenantId))
                .collect(Collectors.toList());
    }
}
