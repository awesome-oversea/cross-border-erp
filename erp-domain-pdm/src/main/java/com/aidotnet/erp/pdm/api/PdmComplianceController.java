package com.aidotnet.erp.pdm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.pdm.application.PdmComplianceService;
import com.aidotnet.erp.pdm.application.PdmComplianceService.AddSensitiveWordCommand;
import com.aidotnet.erp.pdm.domain.ComplianceCheckResult;
import com.aidotnet.erp.pdm.domain.SensitiveWord;
import com.aidotnet.erp.pdm.domain.UpcPool;
import com.aidotnet.erp.pdm.domain.UpcPool.UpcStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PDM合规管理控制器
 * <p>
 * 描述: PDM域合规管理REST API，提供敏感词管理、合规检查、UPC码池管理等接口。
 *       路径前缀: /pdm/api/in/v1/compliance (内部接口)
 * </p>
 * <p>
 * 接口分组:
 *   1. 敏感词管理 - /sensitive-words (添加/删除/列表)
 *   2. 合规检查   - /check (文本合规检查)
 *   3. UPC码管理  - /upc (批量导入/分配/释放/列表)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/pdm/api/in/v1/compliance")
public class PdmComplianceController {

    private final PdmComplianceService service;

    /**
     * 构造函数 - 依赖注入合规服务
     *
     * @param service PDM合规管理应用服务
     */
    public PdmComplianceController(PdmComplianceService service) {
        this.service = service;
    }

    /** 添加敏感词 */
    @PostMapping("/sensitive-words")
    public Result<SensitiveWord> addSensitiveWord(@Valid @RequestBody AddSensitiveWordRequest request) {
        AddSensitiveWordCommand command = new AddSensitiveWordCommand(request.word(), request.category(), request.language());
        return Result.ok(service.addSensitiveWord(currentTenant(), command));
    }

    /** 删除敏感词(逻辑删除，设为禁用) */
    @DeleteMapping("/sensitive-words/{wordId}")
    public Result<Void> removeSensitiveWord(@PathVariable String wordId) {
        service.removeSensitiveWord(currentTenant(), wordId);
        return Result.ok(null);
    }

    /** 查询敏感词列表，支持按语言过滤 */
    @GetMapping("/sensitive-words")
    public Result<List<SensitiveWord>> listSensitiveWords(@RequestParam(required = false) String language) {
        return Result.ok(service.listSensitiveWords(currentTenant(), language));
    }

    /** 产品合规检查，检查标题、描述、关键词是否包含敏感词 */
    @PostMapping("/check")
    public Result<ComplianceCheckResult> checkCompliance(@Valid @RequestBody ComplianceCheckRequest request) {
        return Result.ok(service.checkCompliance(currentTenant(), request.spuId(),
                request.title(), request.description(), request.keywords()));
    }

    /** 批量导入UPC码 */
    @PostMapping("/upc/batch")
    public Result<Void> addUpcCodes(@Valid @RequestBody AddUpcCodesRequest request) {
        service.addUpcCodes(currentTenant(), request.upcCodes());
        return Result.ok(null);
    }

    /** 分配UPC码给SKU */
    @PostMapping("/upc/assign")
    public Result<UpcPool> assignUpc(@RequestParam String skuId) {
        return Result.ok(service.assignUpc(currentTenant(), skuId));
    }

    /** 释放已分配的UPC码 */
    @PostMapping("/upc/{upcCode}/release")
    public Result<UpcPool> releaseUpc(@PathVariable String upcCode) {
        return Result.ok(service.releaseUpc(currentTenant(), upcCode));
    }

    /** 查询UPC码池，支持按状态过滤 */
    @GetMapping("/upc")
    public Result<List<UpcPool>> listUpcPool(@RequestParam(required = false) UpcStatus status) {
        return Result.ok(service.listUpcPool(currentTenant(), status));
    }

    /** 获取当前租户ID，为空则抛出异常 */
    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record AddSensitiveWordRequest(@NotBlank String word, @NotBlank SensitiveWord.Category category, String language) {}
    public record ComplianceCheckRequest(String spuId, @NotBlank String title, String description, List<String> keywords) {}
    public record AddUpcCodesRequest(List<@NotBlank String> upcCodes) {}
}
