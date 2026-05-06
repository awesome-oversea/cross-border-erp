package com.aidotnet.erp.pdm.application;

import com.aidotnet.erp.common.context.TraceContext;
import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.pdm.domain.Brand;
import com.aidotnet.erp.pdm.domain.Category;
import com.aidotnet.erp.pdm.domain.DevStage;
import com.aidotnet.erp.pdm.domain.ProductCreatedEvent;
import com.aidotnet.erp.pdm.domain.ProductDevelopment;
import com.aidotnet.erp.pdm.domain.ProductIpAlertEvent;
import com.aidotnet.erp.pdm.domain.ProductStatus;
import com.aidotnet.erp.pdm.domain.ProposalStatus;
import com.aidotnet.erp.pdm.domain.SelectionProposal;
import com.aidotnet.erp.pdm.domain.Sku;
import com.aidotnet.erp.pdm.domain.Spu;
import com.aidotnet.erp.pdm.infrastructure.ProductStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 产品主数据应用服务
 * <p>
 * 描述: 产品开发域核心服务，负责SPU、SKU、类目、品牌、选品建议等
 *       产品主数据的增删改查业务逻辑，以及产品开发流程管理。
 *       支持AI选品建议的审核和产品知识产权合规检查。
 * </p>
 * <p>
 * 核心能力:
 *   1. SPU管理 - 创建/更新/审核/上下架产品标准单元
 *   2. SKU管理 - 创建/更新产品最小可售单元
 *   3. 类目管理 - 创建产品分类体系
 *   4. 品牌管理 - 创建品牌主数据
 *   5. 选品建议 - 创建/提交/审核AI选品建议
 *   6. 产品开发 - 创建/推进产品开发流程
 *   7. 知识产权 - IP合规检查和侵权预警
 * </p>
 * <p>
 * 业务规则:
 *   1. SPU创建时必须关联有效类目和品牌
 *   2. SPU需审核通过后才可上架
 *   3. 知识产权侵权状态的产品禁止上架
 *   4. 选品建议审核通过后可创建产品开发流程
 *   5. 产品创建事件通过DomainEventPublisher发布，供其他域订阅
 * </p>
 *
 * @author ERP系统
 */
@Service
public class ProductService {

    private final ProductStore productStore;
    private final DomainEventPublisher eventPublisher;

    /**
     * 构造函数 - 依赖注入产品存储和事件发布器
     *
     * @param productStore   产品数据存储
     * @param eventPublisher 领域事件发布器
     */
    public ProductService(ProductStore productStore, DomainEventPublisher eventPublisher) {
        this.productStore = productStore;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 创建类目
     *
     * @param tenantId 租户ID
     * @param command  创建类目命令
     * @return 新创建的类目
     */
    public Category createCategory(String tenantId, CreateCategoryCommand command) {
        Instant now = Instant.now();
        return productStore.saveCategory(new Category(UUID.randomUUID().toString(), tenantId, command.name(), command.parentId(), now));
    }

    public Brand createBrand(String tenantId, CreateBrandCommand command) {
        Instant now = Instant.now();
        return productStore.saveBrand(new Brand(UUID.randomUUID().toString(), tenantId, command.name(), now));
    }

    @Transactional
    public Spu createSpu(String tenantId, CreateSpuCommand command) {
        productStore.findCategory(tenantId, command.categoryId())
                .orElseThrow(() -> new BizException("CATEGORY_NOT_FOUND", "类目不存在"));
        productStore.findBrand(tenantId, command.brandId())
                .orElseThrow(() -> new BizException("BRAND_NOT_FOUND", "品牌不存在"));
        Instant now = Instant.now();
        Spu spu = productStore.saveSpu(new Spu(UUID.randomUUID().toString(), tenantId, command.sku(),
                command.title(), command.description(), command.categoryId(), command.brandId(),
                ProductStatus.DRAFT, "none", now, now));
        eventPublisher.publish(new ProductCreatedEvent(UUID.randomUUID().toString(), tenantId, TraceContext.getTraceId(), spu.spuId(), now));
        return spu;
    }

    @Transactional
    public Spu updateSpu(String tenantId, String spuId, UpdateSpuCommand command) {
        Spu spu = productStore.findSpu(tenantId, spuId)
                .orElseThrow(() -> new BizException("SPU_NOT_FOUND", "SPU不存在"));
        Spu updated = new Spu(spu.spuId(), spu.tenantId(),
                command.sku() != null ? command.sku() : spu.sku(),
                command.title() != null ? command.title() : spu.title(),
                command.description() != null ? command.description() : spu.description(),
                command.categoryId() != null ? command.categoryId() : spu.categoryId(),
                command.brandId() != null ? command.brandId() : spu.brandId(),
                spu.status(), spu.ipStatus(), spu.createdAt(), Instant.now());
        return productStore.saveSpu(updated);
    }

    public Spu getSpu(String tenantId, String spuId) {
        return productStore.findSpu(tenantId, spuId)
                .orElseThrow(() -> new BizException("SPU_NOT_FOUND", "SPU不存在"));
    }

    public Sku createSku(String tenantId, String spuId, CreateSkuCommand command) {
        productStore.findSpu(tenantId, spuId).orElseThrow(() -> new BizException("SPU_NOT_FOUND", "SPU不存在"));
        productStore.findSkuBySellerSku(tenantId, command.sellerSku()).ifPresent(existing -> {
            throw new BizException("SKU_DUPLICATED", "SKU已存在");
        });
        Instant now = Instant.now();
        return productStore.saveSku(new Sku(UUID.randomUUID().toString(), tenantId, spuId, command.sellerSku(),
                command.title(), command.weightKg(), command.declaredValue(), command.currency(), ProductStatus.DRAFT, now, now));
    }

    @Transactional
    public Spu publishSpu(String tenantId, String spuId) {
        Spu spu = productStore.findSpu(tenantId, spuId).orElseThrow(() -> new BizException("SPU_NOT_FOUND", "SPU不存在"));
        if (productStore.listSkus(tenantId, spuId).isEmpty()) {
            throw new BizException("SKU_REQUIRED", "上架前至少需要一个SKU");
        }
        if (spu.hasIpConflict()) {
            throw new BizException("IP_CONFLICT", "产品存在知识产权冲突，无法上架");
        }
        Spu published = new Spu(spu.spuId(), spu.tenantId(), spu.sku(), spu.title(), spu.description(),
                spu.categoryId(), spu.brandId(), ProductStatus.ACTIVE, spu.ipStatus(), spu.createdAt(), Instant.now());
        return productStore.saveSpu(published);
    }

    @Transactional
    public Spu unpublishSpu(String tenantId, String spuId) {
        Spu spu = productStore.findSpu(tenantId, spuId).orElseThrow(() -> new BizException("SPU_NOT_FOUND", "SPU不存在"));
        if (spu.status() != ProductStatus.ACTIVE) {
            throw new BizException("SPU_STATUS_INVALID", "只有上架产品可以下架");
        }
        Spu unpublished = new Spu(spu.spuId(), spu.tenantId(), spu.sku(), spu.title(), spu.description(),
                spu.categoryId(), spu.brandId(), ProductStatus.INACTIVE, spu.ipStatus(), spu.createdAt(), Instant.now());
        return productStore.saveSpu(unpublished);
    }

    @Transactional
    public Spu updateIpStatus(String tenantId, String spuId, String ipStatus) {
        Spu spu = productStore.findSpu(tenantId, spuId).orElseThrow(() -> new BizException("SPU_NOT_FOUND", "SPU不存在"));
        Spu updated = new Spu(spu.spuId(), spu.tenantId(), spu.sku(), spu.title(), spu.description(),
                spu.categoryId(), spu.brandId(), spu.status(), ipStatus, spu.createdAt(), Instant.now());
        Spu saved = productStore.saveSpu(updated);
        if ("infringing".equals(ipStatus)) {
            eventPublisher.publish(new ProductIpAlertEvent(UUID.randomUUID().toString(), tenantId,
                    TraceContext.getTraceId(), spuId, ipStatus, Instant.now()));
        }
        return saved;
    }

    @Transactional
    public SelectionProposal createProposal(String tenantId, CreateProposalCommand command) {
        Instant now = Instant.now();
        return productStore.saveProposal(new SelectionProposal(UUID.randomUUID().toString(), tenantId,
                command.productName(), command.title(), command.categoryId(), command.source(),
                command.sourceReference(), command.estimatedCost(), command.marketAnalysis(),
                command.profitEstimation(), command.riskAssessment(), command.aiSuggested(),
                ProposalStatus.DRAFT, null, null, null, now, now));
    }

    @Transactional
    public SelectionProposal createProposalFromPms(String tenantId, CreatePmsProposalCommand command) {
        Instant now = Instant.now();
        return productStore.saveProposal(new SelectionProposal(UUID.randomUUID().toString(), tenantId,
                command.productName(), command.productName(), command.categoryId(), "pms",
                command.sourceUrl(), null, command.marketAnalysis(), command.profitEstimation(),
                command.riskAssessment(), true, ProposalStatus.SUBMITTED, null, null, null, now, now));
    }

    @Transactional
    public SelectionProposal submitProposal(String tenantId, String proposalId) {
        SelectionProposal proposal = getProposal(tenantId, proposalId);
        if (!proposal.canSubmit()) {
            throw new BizException("PROPOSAL_STATUS_INVALID", "只有草稿提报可以提交");
        }
        return updateProposal(proposal, ProposalStatus.SUBMITTED, proposal.submittedBy(), proposal.reviewedBy(), proposal.reviewComment());
    }

    @Transactional
    public SelectionProposal approveProposal(String tenantId, String proposalId, String reviewedBy, String comment) {
        SelectionProposal proposal = getProposal(tenantId, proposalId);
        if (!proposal.canApprove()) {
            throw new BizException("PROPOSAL_STATUS_INVALID", "只有已提交提报可以审核");
        }
        return updateProposal(proposal, ProposalStatus.APPROVED, proposal.submittedBy(), reviewedBy, comment);
    }

    @Transactional
    public SelectionProposal rejectProposal(String tenantId, String proposalId, String reviewedBy, String comment) {
        SelectionProposal proposal = getProposal(tenantId, proposalId);
        if (!proposal.canApprove()) {
            throw new BizException("PROPOSAL_STATUS_INVALID", "只有已提交提报可以驳回");
        }
        return updateProposal(proposal, ProposalStatus.REJECTED, proposal.submittedBy(), reviewedBy, comment);
    }

    @Transactional
    public ProductDevelopment createDevelopment(String tenantId, CreateDevCommand command) {
        productStore.findSpu(tenantId, command.spuId()).orElseThrow(() -> new BizException("SPU_NOT_FOUND", "SPU不存在"));
        if (command.proposalId() != null) {
            SelectionProposal proposal = productStore.findProposal(tenantId, command.proposalId())
                    .orElseThrow(() -> new BizException("PROPOSAL_NOT_FOUND", "选品提报不存在"));
            if (proposal.status() != ProposalStatus.APPROVED) {
                throw new BizException("PROPOSAL_STATUS_INVALID", "只有已审批提报可以创建开发流程");
            }
            updateProposal(proposal, ProposalStatus.CONVERTED,
                    proposal.submittedBy(), proposal.reviewedBy(), proposal.reviewComment());
        }
        Instant now = Instant.now();
        return productStore.saveDevelopment(new ProductDevelopment(UUID.randomUUID().toString(), tenantId,
                command.spuId(), command.proposalId(), DevStage.RESEARCH, null,
                command.developer(), command.editor(), command.designer(),
                command.priority() != null ? command.priority() : 0, "active", now, now));
    }

    @Transactional
    public ProductDevelopment updateDevStage(String tenantId, String devId, DevStage stage, String note) {
        ProductDevelopment dev = productStore.findDevelopment(tenantId, devId)
                .orElseThrow(() -> new BizException("DEV_NOT_FOUND", "开发流程不存在"));
        validateStageTransition(dev, stage);
        return productStore.saveDevelopment(new ProductDevelopment(dev.devId(), dev.tenantId(), dev.spuId(),
                dev.proposalId(), stage, note, dev.developer(), dev.editor(), dev.designer(),
                dev.priority(), resolveDevelopmentStatus(dev, stage), dev.createdAt(), Instant.now()));
    }

    @Transactional
    public ProductDevelopment assignDevTeam(String tenantId, String devId, AssignDevTeamCommand command) {
        ProductDevelopment dev = productStore.findDevelopment(tenantId, devId)
                .orElseThrow(() -> new BizException("DEV_NOT_FOUND", "开发流程不存在"));
        return productStore.saveDevelopment(new ProductDevelopment(dev.devId(), dev.tenantId(), dev.spuId(),
                dev.proposalId(), dev.stage(), dev.stageNote(),
                command.developer() != null ? command.developer() : dev.developer(),
                command.editor() != null ? command.editor() : dev.editor(),
                command.designer() != null ? command.designer() : dev.designer(),
                command.priority() != null ? command.priority() : dev.priority(),
                dev.status(), dev.createdAt(), Instant.now()));
    }

    @Transactional
    public List<ProductDevelopment> batchAssignDevTeam(String tenantId, List<String> devIds, AssignDevTeamCommand command) {
        if (devIds == null || devIds.isEmpty()) {
            throw new BizException("DEV_IDS_REQUIRED", "开发流程列表不能为空");
        }
        return devIds.stream()
                .map(devId -> assignDevTeam(tenantId, devId, command))
                .toList();
    }

    public List<Spu> listSpus(String tenantId) {
        return productStore.listSpus(tenantId);
    }

    public List<Sku> listSkus(String tenantId, String spuId) {
        productStore.findSpu(tenantId, spuId).orElseThrow(() -> new BizException("SPU_NOT_FOUND", "SPU不存在"));
        return productStore.listSkus(tenantId, spuId);
    }

    public List<Category> listCategories(String tenantId) {
        return productStore.listCategories(tenantId);
    }

    public List<Brand> listBrands(String tenantId) {
        return productStore.listBrands(tenantId);
    }

    public List<SelectionProposal> listProposals(String tenantId) {
        return productStore.listProposals(tenantId);
    }

    public List<SelectionProposal> listProposalsByStatus(String tenantId, ProposalStatus status) {
        return productStore.listProposalsByStatus(tenantId, status);
    }

    public List<ProductDevelopment> listDevelopments(String tenantId) {
        return productStore.listDevelopments(tenantId);
    }

    public List<ProductDevelopment> listDevelopmentsBySpu(String tenantId, String spuId) {
        return productStore.listDevelopmentsBySpu(tenantId, spuId);
    }

    public DevelopmentStats getDevelopmentStats(String tenantId) {
        List<ProductDevelopment> developments = productStore.listDevelopments(tenantId);
        Map<DevStage, Long> stageCounts = new EnumMap<>(DevStage.class);
        Arrays.stream(DevStage.values()).forEach(stage -> stageCounts.put(stage, 0L));
        developments.forEach(dev -> stageCounts.merge(dev.stage(), 1L, Long::sum));

        Map<String, Long> developerWorkload = developments.stream()
                .filter(dev -> dev.developer() != null && !dev.developer().isBlank())
                .collect(Collectors.groupingBy(ProductDevelopment::developer, Collectors.counting()));

        long activeCount = developments.stream().filter(dev -> "active".equalsIgnoreCase(dev.status())).count();
        long completedCount = developments.stream().filter(dev -> "completed".equalsIgnoreCase(dev.status())).count();
        long cancelledCount = developments.stream().filter(dev -> "cancelled".equalsIgnoreCase(dev.status())).count();
        int averageProgressPercent = developments.isEmpty() ? 0 : (int) Math.round(
                developments.stream().mapToInt(dev -> progressPercent(dev.stage())).average().orElse(0));

        return new DevelopmentStats(
                developments.size(),
                activeCount,
                completedCount,
                cancelledCount,
                Map.copyOf(stageCounts),
                Map.copyOf(developerWorkload),
                averageProgressPercent);
    }

    private SelectionProposal getProposal(String tenantId, String proposalId) {
        return productStore.findProposal(tenantId, proposalId)
                .orElseThrow(() -> new BizException("PROPOSAL_NOT_FOUND", "选品提报不存在"));
    }

    private void validateStageTransition(ProductDevelopment development, DevStage targetStage) {
        if (targetStage == null) {
            throw new BizException("DEV_STAGE_REQUIRED", "开发阶段不能为空");
        }
        if ("cancelled".equalsIgnoreCase(development.status())) {
            throw new BizException("DEV_STATUS_INVALID", "已取消流程不可继续推进");
        }
        if ("completed".equalsIgnoreCase(development.status())
                && !(development.stage() == DevStage.LISTED && targetStage == DevStage.ARCHIVED)
                && targetStage != development.stage()) {
            throw new BizException("DEV_STATUS_INVALID", "已完成流程不可继续推进");
        }
        if (targetStage == development.stage()) {
            return;
        }
        int currentIndex = development.stage().ordinal();
        int targetIndex = targetStage.ordinal();
        if (targetIndex < currentIndex || targetIndex - currentIndex > 1) {
            throw new BizException("DEV_STAGE_INVALID", "开发阶段不允许跳级或逆行");
        }
    }

    private String resolveDevelopmentStatus(ProductDevelopment development, DevStage targetStage) {
        if (targetStage == development.stage()) {
            return development.status();
        }
        if (targetStage == DevStage.LISTED || targetStage == DevStage.ARCHIVED) {
            return "completed";
        }
        return "active";
    }

    private int progressPercent(DevStage stage) {
        return switch (stage) {
            case RESEARCH -> 0;
            case SAMPLING -> 20;
            case TESTING -> 40;
            case MASS_PRODUCTION -> 70;
            case LISTED -> 90;
            case ARCHIVED -> 100;
        };
    }

    private SelectionProposal updateProposal(SelectionProposal p, ProposalStatus status, String submittedBy, String reviewedBy, String comment) {
        return productStore.saveProposal(new SelectionProposal(p.proposalId(), p.tenantId(), p.productName(),
                p.title(), p.categoryId(), p.source(), p.sourceReference(), p.estimatedCost(),
                p.marketAnalysis(), p.profitEstimation(), p.riskAssessment(), p.aiSuggested(),
                status, submittedBy, reviewedBy, comment, p.createdAt(), Instant.now()));
    }

    public record CreateCategoryCommand(String name, String parentId) {}

    public record CreateBrandCommand(String name) {}

    public record CreateSpuCommand(String sku, String title, String description, String categoryId, String brandId) {}

    public record UpdateSpuCommand(String sku, String title, String description, String categoryId, String brandId) {}

    public record CreateSkuCommand(String sellerSku, String title, BigDecimal weightKg, BigDecimal declaredValue, String currency) {}

    public record CreateProposalCommand(String productName, String title, String categoryId, String source,
                                        String sourceReference, BigDecimal estimatedCost, String marketAnalysis,
                                        String profitEstimation, String riskAssessment, boolean aiSuggested) {}

    public record CreatePmsProposalCommand(String productName, String categoryId, String sourceUrl,
                                           String marketAnalysis, String profitEstimation, String riskAssessment) {}

    public record CreateDevCommand(String spuId, String proposalId, String developer, String editor,
                                   String designer, Integer priority) {}

    public record AssignDevTeamCommand(String developer, String editor, String designer, Integer priority) {}

    public record DevelopmentStats(int totalCount, long activeCount, long completedCount, long cancelledCount,
                                   Map<DevStage, Long> stageCounts, Map<String, Long> developerWorkload,
                                   int averageProgressPercent) {}
}
