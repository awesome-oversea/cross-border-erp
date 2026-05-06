package com.aidotnet.erp.pdm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.pdm.application.ProductService;
import com.aidotnet.erp.pdm.application.ProductService.AssignDevTeamCommand;
import com.aidotnet.erp.pdm.application.ProductService.CreateBrandCommand;
import com.aidotnet.erp.pdm.application.ProductService.CreateCategoryCommand;
import com.aidotnet.erp.pdm.application.ProductService.CreateDevCommand;
import com.aidotnet.erp.pdm.application.ProductService.CreatePmsProposalCommand;
import com.aidotnet.erp.pdm.application.ProductService.CreateProposalCommand;
import com.aidotnet.erp.pdm.application.ProductService.CreateSkuCommand;
import com.aidotnet.erp.pdm.application.ProductService.CreateSpuCommand;
import com.aidotnet.erp.pdm.application.ProductService.UpdateSpuCommand;
import com.aidotnet.erp.pdm.domain.Brand;
import com.aidotnet.erp.pdm.domain.Category;
import com.aidotnet.erp.pdm.domain.DevStage;
import com.aidotnet.erp.pdm.domain.ProductDevelopment;
import com.aidotnet.erp.pdm.domain.ProposalStatus;
import com.aidotnet.erp.pdm.domain.SelectionProposal;
import com.aidotnet.erp.pdm.domain.Sku;
import com.aidotnet.erp.pdm.domain.Spu;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品主数据控制器
 * <p>
 * 描述: PDM域核心REST API，提供SPU、SKU、类目、品牌、选品建议、
 *       产品开发等资源的增删改查接口。
 *       路径前缀: /pdm/api/in/v1 (内部接口)
 * </p>
 * <p>
 * 接口分组:
 *   1. 类目管理 - /categories (创建/列表)
 *   2. 品牌管理 - /brands (创建/列表)
 *   3. 产品管理 - /products (创建/更新/上下架/IP状态)
 *   4. SKU管理 - /products/{spuId}/skus (创建/列表)
 *   5. 选品建议 - /selection-proposals (创建/提交/审核/列表)
 *   6. 产品开发 - /product-developments (创建/阶段更新/团队分配)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/pdm/api/in/v1")
public class ProductController {

    private final ProductService productService;

    /**
     * 构造函数 - 依赖注入产品服务
     *
     * @param productService 产品主数据应用服务
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** 创建类目 */
    @PostMapping("/categories")
    public Result<Category> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return Result.ok(productService.createCategory(currentTenant(), new CreateCategoryCommand(request.name(), request.parentId())));
    }

    /** 查询类目列表 */
    @GetMapping("/categories")
    public Result<List<Category>> listCategories() {
        return Result.ok(productService.listCategories(currentTenant()));
    }

    /** 创建品牌 */
    @PostMapping("/brands")
    public Result<Brand> createBrand(@Valid @RequestBody CreateBrandRequest request) {
        return Result.ok(productService.createBrand(currentTenant(), new CreateBrandCommand(request.name())));
    }

    /** 查询品牌列表 */
    @GetMapping("/brands")
    public Result<List<Brand>> listBrands() {
        return Result.ok(productService.listBrands(currentTenant()));
    }

    /** 创建SPU */
    @PostMapping("/products")
    public Result<Spu> createSpu(@Valid @RequestBody CreateSpuRequest request) {
        return Result.ok(productService.createSpu(currentTenant(), new CreateSpuCommand(request.sku(), request.title(),
                request.description(), request.categoryId(), request.brandId())));
    }

    /** 查询SPU列表 */
    @GetMapping("/products")
    public Result<List<Spu>> listSpus() {
        return Result.ok(productService.listSpus(currentTenant()));
    }

    /** 查询SPU详情 */
    @GetMapping("/products/{spuId}")
    public Result<Spu> getSpu(@PathVariable String spuId) {
        return Result.ok(productService.getSpu(currentTenant(), spuId));
    }

    /** 更新SPU */
    @PutMapping("/products/{spuId}")
    public Result<Spu> updateSpu(@PathVariable String spuId, @Valid @RequestBody UpdateSpuRequest request) {
        return Result.ok(productService.updateSpu(currentTenant(), spuId, new UpdateSpuCommand(request.sku(),
                request.title(), request.description(), request.categoryId(), request.brandId())));
    }

    /** 更新SPU知识产权状态 */
    @PatchMapping("/products/{spuId}/ip-status")
    public Result<Spu> updateIpStatus(@PathVariable String spuId, @Valid @RequestBody UpdateIpStatusRequest request) {
        return Result.ok(productService.updateIpStatus(currentTenant(), spuId, request.ipStatus()));
    }

    /** 创建SKU */
    @PostMapping("/products/{spuId}/skus")
    public Result<Sku> createSku(@PathVariable String spuId, @Valid @RequestBody CreateSkuRequest request) {
        return Result.ok(productService.createSku(currentTenant(), spuId, new CreateSkuCommand(request.sellerSku(), request.title(),
                request.weightKg(), request.declaredValue(), request.currency())));
    }

    /** 查询SKU列表 */
    @GetMapping("/products/{spuId}/skus")
    public Result<List<Sku>> listSkus(@PathVariable String spuId) {
        return Result.ok(productService.listSkus(currentTenant(), spuId));
    }

    /** SPU上架 */
    @PatchMapping("/products/{spuId}/publish")
    public Result<Spu> publishSpu(@PathVariable String spuId) {
        return Result.ok(productService.publishSpu(currentTenant(), spuId));
    }

    /** SPU下架 */
    @PatchMapping("/products/{spuId}/unpublish")
    public Result<Spu> unpublishSpu(@PathVariable String spuId) {
        return Result.ok(productService.unpublishSpu(currentTenant(), spuId));
    }

    /** 创建选品建议 */
    @PostMapping("/selection-proposals")
    public Result<SelectionProposal> createProposal(@Valid @RequestBody CreateProposalRequest request) {
        return Result.ok(productService.createProposal(currentTenant(), new CreateProposalCommand(request.productName(),
                request.title(), request.categoryId(), request.source(), request.sourceReference(),
                request.estimatedCost(), request.marketAnalysis(), request.profitEstimation(),
                request.riskAssessment(), request.aiSuggested())));
    }

    /** 查询选品建议列表，支持按状态过滤 */
    @GetMapping("/selection-proposals")
    public Result<List<SelectionProposal>> listProposals(@RequestParam(required = false) String status) {
        if (status != null) {
            return Result.ok(productService.listProposalsByStatus(currentTenant(), ProposalStatus.valueOf(status)));
        }
        return Result.ok(productService.listProposals(currentTenant()));
    }

    /** 提交选品建议审核 */
    @PatchMapping("/selection-proposals/{proposalId}/submit")
    public Result<SelectionProposal> submitProposal(@PathVariable String proposalId) {
        return Result.ok(productService.submitProposal(currentTenant(), proposalId));
    }

    /** 批准选品建议 */
    @PutMapping("/selection-proposals/{proposalId}/approve")
    public Result<SelectionProposal> approveProposal(@PathVariable String proposalId, @Valid @RequestBody ReviewProposalRequest request) {
        return Result.ok(productService.approveProposal(currentTenant(), proposalId, request.reviewedBy(), request.comment()));
    }

    /** 拒绝选品建议 */
    @PutMapping("/selection-proposals/{proposalId}/reject")
    public Result<SelectionProposal> rejectProposal(@PathVariable String proposalId, @Valid @RequestBody ReviewProposalRequest request) {
        return Result.ok(productService.rejectProposal(currentTenant(), proposalId, request.reviewedBy(), request.comment()));
    }

    /** 创建产品开发流程 */
    @PostMapping("/product-developments")
    public Result<ProductDevelopment> createDevelopment(@Valid @RequestBody CreateDevRequest request) {
        return Result.ok(productService.createDevelopment(currentTenant(), new CreateDevCommand(request.spuId(),
                request.proposalId(), request.developer(), request.editor(), request.designer(), request.priority())));
    }

    /** 查询产品开发列表，支持按SPU过滤 */
    @GetMapping("/product-developments")
    public Result<List<ProductDevelopment>> listDevelopments(@RequestParam(required = false) String spuId) {
        if (spuId != null) {
            return Result.ok(productService.listDevelopmentsBySpu(currentTenant(), spuId));
        }
        return Result.ok(productService.listDevelopments(currentTenant()));
    }

    /** 查询产品开发阶段统计 */
    @GetMapping("/product-developments/stats")
    public Result<ProductService.DevelopmentStats> getDevelopmentStats() {
        return Result.ok(productService.getDevelopmentStats(currentTenant()));
    }

    /** 更新产品开发阶段 */
    @PutMapping("/product-developments/{devId}/stage")
    public Result<ProductDevelopment> updateDevStage(@PathVariable String devId, @Valid @RequestBody UpdateDevStageRequest request) {
        return Result.ok(productService.updateDevStage(currentTenant(), devId, DevStage.valueOf(request.stage()), request.note()));
    }

    /** 分配产品开发团队 */
    @PutMapping("/product-developments/{devId}/team")
    public Result<ProductDevelopment> assignDevTeam(@PathVariable String devId, @Valid @RequestBody AssignDevTeamRequest request) {
        return Result.ok(productService.assignDevTeam(currentTenant(), devId, new AssignDevTeamCommand(
                request.developer(), request.editor(), request.designer(), request.priority())));
    }

    /** 批量分配产品开发团队 */
    @PutMapping("/product-developments/team/batch")
    public Result<List<ProductDevelopment>> batchAssignDevTeam(@Valid @RequestBody BatchAssignDevTeamRequest request) {
        return Result.ok(productService.batchAssignDevTeam(currentTenant(), request.devIds(),
                new AssignDevTeamCommand(request.developer(), request.editor(), request.designer(), request.priority())));
    }

    /** 获取当前租户ID，为空则抛出异常 */
    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateCategoryRequest(@NotBlank String name, String parentId) {}
    public record CreateBrandRequest(@NotBlank String name) {}
    public record CreateSpuRequest(String sku, @NotBlank String title, String description,
                                   @NotBlank String categoryId, @NotBlank String brandId) {}
    public record UpdateSpuRequest(String sku, String title, String description, String categoryId, String brandId) {}
    public record UpdateIpStatusRequest(@NotBlank String ipStatus) {}
    public record CreateSkuRequest(@NotBlank String sellerSku, @NotBlank String title, @NotNull @Positive BigDecimal weightKg,
                                   @NotNull @Positive BigDecimal declaredValue, @NotBlank String currency) {}
    public record CreateProposalRequest(String productName, @NotBlank String title, String categoryId, String source,
                                        String sourceReference, BigDecimal estimatedCost, String marketAnalysis,
                                        String profitEstimation, String riskAssessment, boolean aiSuggested) {}
    public record ReviewProposalRequest(@NotBlank String reviewedBy, String comment) {}
    public record CreateDevRequest(@NotBlank String spuId, String proposalId, String developer,
                                   String editor, String designer, Integer priority) {}
    public record UpdateDevStageRequest(@NotBlank String stage, String note) {}
    public record AssignDevTeamRequest(String developer, String editor, String designer, Integer priority) {}
    public record BatchAssignDevTeamRequest(List<@NotBlank String> devIds, String developer, String editor,
                                            String designer, Integer priority) {}
}
