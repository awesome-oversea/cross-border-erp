package com.aidotnet.erp.common.voucher;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台共享凭证引擎正式能力已迁移至 erp-domain-fms 平台控制器。
 * 当前类保留源代码语义，避免误删历史实现，但不再注册为运行时接口。
 */
@RequestMapping("/platform/fms/api/v1/voucher-engine")
public class VoucherEngineController {

    private final VoucherEngineService voucherEngineService;

    public VoucherEngineController(VoucherEngineService voucherEngineService) {
        this.voucherEngineService = voucherEngineService;
    }

    @PostMapping("/auto-generate")
    public Result<VoucherEngineService.Voucher> autoGenerate(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineMaps = (List<Map<String, Object>>) request.get("lines");
        List<VoucherEngineService.EntryLine> lines = lineMaps.stream()
                .map(m -> new VoucherEngineService.EntryLine(
                        (String) m.get("accountCode"),
                        (String) m.get("accountName"),
                        new BigDecimal(m.getOrDefault("debit", "0").toString()),
                        new BigDecimal(m.getOrDefault("credit", "0").toString()),
                        (String) m.getOrDefault("summary", "")
                ))
                .toList();
        VoucherEngineService.Voucher voucher = voucherEngineService.autoGenerate(
                (String) request.get("businessType"),
                (String) request.get("businessId"),
                (String) request.get("tenantId"),
                lines,
                (String) request.getOrDefault("description", "")
        );
        return Result.ok(voucher);
    }

    @GetMapping("/entries")
    public Result<List<VoucherEngineService.JournalEntry>> getEntries(@RequestParam String voucherNo) {
        return Result.ok(voucherEngineService.getEntries(voucherNo));
    }

    @GetMapping("/summary")
    public Result<VoucherEngineService.VoucherSummary> getSummary(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(voucherEngineService.summary(tenantId, startDate, endDate));
    }

    @PostMapping("/{id}/approve")
    public Result<Boolean> approve(@PathVariable String id, @RequestBody Map<String, String> request) {
        return Result.ok(voucherEngineService.approve(id, request.get("approvedBy")));
    }

    @GetMapping("/inventory-cost")
    public Result<Map<String, Object>> getInventoryCost() {
        return Result.ok(Map.of());
    }
}

/**
 * 平台共享凭证对外推送正式能力已迁移至 erp-domain-fms 平台控制器。
 * 当前类保留源代码语义，避免误删历史实现，但不再注册为运行时接口。
 */
@RequestMapping("/platform/fms/api/out/v1/voucher-engine")
class VoucherOutboundController {

    @PostMapping("/push-kingdee")
    public Result<Map<String, Object>> pushKingdee(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "QUEUED", "message", "Push to Kingdee queued"));
    }

    @PostMapping("/push-yonyou")
    public Result<Map<String, Object>> pushYonyou(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "QUEUED", "message", "Push to Yonyou queued"));
    }
}
