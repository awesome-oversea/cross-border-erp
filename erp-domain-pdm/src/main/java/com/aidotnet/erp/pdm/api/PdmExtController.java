package com.aidotnet.erp.pdm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.pdm.application.PdmExtService;
import com.aidotnet.erp.pdm.application.PdmExtService.CreateIpCommand;
import com.aidotnet.erp.pdm.application.PdmExtService.CreateQualityStandardCommand;
import com.aidotnet.erp.pdm.application.PdmExtService.CreateVariantCommand;
import com.aidotnet.erp.pdm.application.PdmExtService.UpdateQualityStandardCommand;
import com.aidotnet.erp.pdm.domain.IntellectualProperty;
import com.aidotnet.erp.pdm.domain.IpType;
import com.aidotnet.erp.pdm.domain.ProductVariant;
import com.aidotnet.erp.pdm.domain.QualityStandard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PDM扩展功能控制器
 * <p>
 * 描述: PDM域扩展功能REST API，提供知识产权、质检标准、产品变体等
 *       扩展实体的管理接口。
 *       路径前缀: /pdm/api/in/v1 (内部接口)
 * </p>
 * <p>
 * 接口分组:
 *   1. 知识产权管理 - /intellectual-properties (创建/注册/查询)
 *   2. 质检标准管理 - /quality-standards (创建/更新/启停/查询)
 *   3. 产品变体管理 - /product-variants (创建/激活/查询)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/pdm/api/in/v1")
public class PdmExtController {

    private final PdmExtService pdmExtService;

    /**
     * 构造函数 - 依赖注入PDM扩展服务
     *
     * @param pdmExtService PDM扩展功能应用服务
     */
    public PdmExtController(PdmExtService pdmExtService) {
        this.pdmExtService = pdmExtService;
    }

    /** 创建知识产权记录 */
    @PostMapping("/intellectual-properties")
    public Result<IntellectualProperty> createIntellectualProperty(@Valid @RequestBody CreateIpRequest request) {
        return Result.ok(pdmExtService.createIntellectualProperty(currentTenant(), new CreateIpCommand(
                request.spuId(), request.type(), request.name(), request.registrationNo(),
                request.jurisdiction(), request.filedAt(), request.expiresAt())));
    }

    /** 注册知识产权(待审核→已注册) */
    @PostMapping("/intellectual-properties/{ipId}/register")
    public Result<IntellectualProperty> registerIp(@PathVariable String ipId) {
        return Result.ok(pdmExtService.registerIp(currentTenant(), ipId));
    }

    /** 按SPU查询知识产权列表 */
    @GetMapping("/intellectual-properties")
    public Result<List<IntellectualProperty>> listIntellectualProperties(@RequestParam String spuId) {
        return Result.ok(pdmExtService.listIntellectualPropertiesBySpu(currentTenant(), spuId));
    }

    /** 查询知识产权详情 */
    @GetMapping("/intellectual-properties/{ipId}")
    public Result<IntellectualProperty> getIntellectualProperty(@PathVariable String ipId) {
        return Result.ok(pdmExtService.getIntellectualProperty(currentTenant(), ipId));
    }

    /** 创建质检标准 */
    @PostMapping("/quality-standards")
    public Result<QualityStandard> createQualityStandard(@Valid @RequestBody CreateQualityStandardRequest request) {
        return Result.ok(pdmExtService.createQualityStandard(currentTenant(), new CreateQualityStandardCommand(
                request.categoryId(), request.name(), request.description(), request.inspectionItems(), request.acceptanceCriteria())));
    }

    /** 更新质检标准 */
    @PutMapping("/quality-standards/{standardId}")
    public Result<QualityStandard> updateQualityStandard(@PathVariable String standardId,
                                                         @Valid @RequestBody UpdateQualityStandardRequest request) {
        return Result.ok(pdmExtService.updateQualityStandard(currentTenant(), standardId,
                new UpdateQualityStandardCommand(request.name(), request.description(), request.inspectionItems(), request.acceptanceCriteria())));
    }

    /** 启停质检标准 */
    @PostMapping("/quality-standards/{standardId}/toggle")
    public Result<QualityStandard> toggleQualityStandard(@PathVariable String standardId, @RequestParam boolean enabled) {
        return Result.ok(pdmExtService.toggleQualityStandard(currentTenant(), standardId, enabled));
    }

    /** 按类目查询质检标准列表 */
    @GetMapping("/quality-standards")
    public Result<List<QualityStandard>> listQualityStandards(@RequestParam String categoryId) {
        return Result.ok(pdmExtService.listQualityStandards(currentTenant(), categoryId));
    }

    /** 查询质检标准详情 */
    @GetMapping("/quality-standards/{standardId}")
    public Result<QualityStandard> getQualityStandard(@PathVariable String standardId) {
        return Result.ok(pdmExtService.getQualityStandard(currentTenant(), standardId));
    }

    /** 创建产品变体 */
    @PostMapping("/product-variants")
    public Result<ProductVariant> createProductVariant(@Valid @RequestBody CreateVariantRequest request) {
        return Result.ok(pdmExtService.createProductVariant(currentTenant(), new CreateVariantCommand(
                request.spuId(), request.variantName(), request.variantAttributes(),
                request.purchaseCost(), request.sellingPrice(), request.priceAdjustment(),
                request.sellerSku(), request.images())));
    }

    /** 激活产品变体(草稿→上架) */
    @PostMapping("/product-variants/{variantId}/activate")
    public Result<ProductVariant> activateVariant(@PathVariable String variantId) {
        return Result.ok(pdmExtService.activateVariant(currentTenant(), variantId));
    }

    /** 按SPU查询产品变体列表 */
    @GetMapping("/product-variants")
    public Result<List<ProductVariant>> listProductVariants(@RequestParam String spuId) {
        return Result.ok(pdmExtService.listProductVariantsBySpu(currentTenant(), spuId));
    }

    /** 查询产品变体详情 */
    @GetMapping("/product-variants/{variantId}")
    public Result<ProductVariant> getProductVariant(@PathVariable String variantId) {
        return Result.ok(pdmExtService.getProductVariant(currentTenant(), variantId));
    }

    /** 获取当前租户ID，为空则抛出异常 */
    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateIpRequest(@NotBlank String spuId, IpType type, @NotBlank String name, String registrationNo,
                                  String jurisdiction, Instant filedAt, Instant expiresAt) {}
    public record CreateQualityStandardRequest(@NotBlank String categoryId, @NotBlank String name, String description,
                                               String inspectionItems, String acceptanceCriteria) {}
    public record UpdateQualityStandardRequest(String name, String description, String inspectionItems, String acceptanceCriteria) {}
    public record CreateVariantRequest(@NotBlank String spuId, @NotBlank String variantName, String variantAttributes,
                                       BigDecimal purchaseCost, BigDecimal sellingPrice, BigDecimal priceAdjustment,
                                       String sellerSku, List<String> images) {}
}
