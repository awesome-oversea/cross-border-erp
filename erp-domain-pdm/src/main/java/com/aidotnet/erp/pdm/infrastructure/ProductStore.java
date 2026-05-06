package com.aidotnet.erp.pdm.infrastructure;

import com.aidotnet.erp.pdm.domain.Brand;
import com.aidotnet.erp.pdm.domain.Category;
import com.aidotnet.erp.pdm.domain.DevStage;
import com.aidotnet.erp.pdm.domain.ProductDevelopment;
import com.aidotnet.erp.pdm.domain.ProductStatus;
import com.aidotnet.erp.pdm.domain.ProposalStatus;
import com.aidotnet.erp.pdm.domain.SelectionProposal;
import com.aidotnet.erp.pdm.domain.Sku;
import com.aidotnet.erp.pdm.domain.Spu;
import com.aidotnet.erp.pdm.infrastructure.data.BrandDO;
import com.aidotnet.erp.pdm.infrastructure.data.CategoryDO;
import com.aidotnet.erp.pdm.infrastructure.data.ProductDevelopmentDO;
import com.aidotnet.erp.pdm.infrastructure.data.SelectionProposalDO;
import com.aidotnet.erp.pdm.infrastructure.data.SkuDO;
import com.aidotnet.erp.pdm.infrastructure.data.SpuDO;
import com.aidotnet.erp.pdm.infrastructure.mapper.ProductMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * 产品主数据存储
 * <p>
 * 描述: PDM域核心数据存储层，负责SPU、SKU、类目、品牌、选品建议、
 *       产品开发等实体的CRUD操作。基于MyBatis-Plus持久化存储。
 * </p>
 * <p>
 * 数据实体:
 *   - Category: 产品类目，支持树形层级
 *   - Brand: 品牌主数据
 *   - Spu: 产品标准单元(Standard Product Unit)
 *   - Sku: 最小可售单元(Stock Keeping Unit)
 *   - SelectionProposal: 选品建议(含AI推荐)
 *   - ProductDevelopment: 产品开发流程
 * </p>
 *
 * @author ERP系统
 */
@Repository
public class ProductStore {

    /** 产品数据MyBatis映射器 */
    private final ProductMapper productMapper;

    /**
     * 构造函数 - 依赖注入产品映射器
     *
     * @param productMapper 产品数据MyBatis映射器
     */
    public ProductStore(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /** 保存类目，不存在则新增 */
    public Category saveCategory(Category category) {
        CategoryDO existing = productMapper.selectCategory(category.tenantId(), category.categoryId());
        CategoryDO data = toCategoryData(category);
        if (existing == null) {
            productMapper.insertCategory(data);
        }
        return category;
    }

    /** 保存品牌，不存在则新增 */
    public Brand saveBrand(Brand brand) {
        BrandDO existing = productMapper.selectBrand(brand.tenantId(), brand.brandId());
        BrandDO data = toBrandData(brand);
        if (existing == null) {
            productMapper.insertBrand(data);
        }
        return brand;
    }

    /** 保存SPU，存在则更新，不存在则新增 */
    public Spu saveSpu(Spu spu) {
        SpuDO existing = productMapper.selectSpu(spu.tenantId(), spu.spuId());
        SpuDO data = toSpuData(spu);
        if (existing == null) {
            productMapper.insertSpu(data);
        } else {
            productMapper.updateSpu(data);
        }
        return spu;
    }

    /** 保存SKU(按sellerSku去重)，存在则更新，不存在则新增 */
    public Sku saveSku(Sku sku) {
        SkuDO existing = productMapper.selectSkuBySellerSku(sku.tenantId(), sku.sellerSku());
        SkuDO data = toSkuData(sku);
        if (existing == null) {
            productMapper.insertSku(data);
        } else {
            productMapper.updateSku(data);
        }
        return sku;
    }

    /** 按租户ID+类目ID查找类目 */
    public Optional<Category> findCategory(String tenantId, String categoryId) {
        return Optional.ofNullable(productMapper.selectCategory(tenantId, categoryId))
                .map(this::toCategoryDomain);
    }

    /** 按租户ID+品牌ID查找品牌 */
    public Optional<Brand> findBrand(String tenantId, String brandId) {
        return Optional.ofNullable(productMapper.selectBrand(tenantId, brandId))
                .map(this::toBrandDomain);
    }

    /** 按租户ID+SPU ID查找SPU */
    public Optional<Spu> findSpu(String tenantId, String spuId) {
        return Optional.ofNullable(productMapper.selectSpu(tenantId, spuId))
                .map(this::toSpuDomain);
    }

    /** 按租户ID+卖家SKU查找SKU */
    public Optional<Sku> findSkuBySellerSku(String tenantId, String sellerSku) {
        return Optional.ofNullable(productMapper.selectSkuBySellerSku(tenantId, sellerSku))
                .map(this::toSkuDomain);
    }

    /** 按租户ID查询SPU列表 */
    public List<Spu> listSpus(String tenantId) {
        return productMapper.selectSpus(tenantId).stream()
                .map(this::toSpuDomain).collect(Collectors.toList());
    }

    /** 按租户ID+SPU ID查询SKU列表 */
    public List<Sku> listSkus(String tenantId, String spuId) {
        return productMapper.selectSkusBySpu(tenantId, spuId).stream()
                .map(this::toSkuDomain).collect(Collectors.toList());
    }

    /** 按租户ID查询类目列表 */
    public List<Category> listCategories(String tenantId) {
        return productMapper.selectCategories(tenantId).stream()
                .map(this::toCategoryDomain).collect(Collectors.toList());
    }

    /** 按租户ID查询品牌列表 */
    public List<Brand> listBrands(String tenantId) {
        return productMapper.selectBrands(tenantId).stream()
                .map(this::toBrandDomain).collect(Collectors.toList());
    }

    /** 保存选品建议，存在则更新，不存在则新增 */
    public SelectionProposal saveProposal(SelectionProposal proposal) {
        SelectionProposalDO existing = productMapper.selectProposal(proposal.tenantId(), proposal.proposalId());
        SelectionProposalDO data = toProposalData(proposal);
        if (existing == null) {
            productMapper.insertProposal(data);
        } else {
            productMapper.updateProposal(data);
        }
        return proposal;
    }

    /** 按租户ID+建议ID查找选品建议 */
    public Optional<SelectionProposal> findProposal(String tenantId, String proposalId) {
        return Optional.ofNullable(productMapper.selectProposal(tenantId, proposalId))
                .map(this::toProposalDomain);
    }

    public List<SelectionProposal> listProposals(String tenantId) {
        return productMapper.selectProposals(tenantId).stream()
                .map(this::toProposalDomain).collect(Collectors.toList());
    }

    public List<SelectionProposal> listProposalsByStatus(String tenantId, ProposalStatus status) {
        return productMapper.selectProposalsByStatus(tenantId, status.name()).stream()
                .map(this::toProposalDomain).collect(Collectors.toList());
    }

    public ProductDevelopment saveDevelopment(ProductDevelopment dev) {
        ProductDevelopmentDO existing = productMapper.selectDevelopment(dev.tenantId(), dev.devId());
        ProductDevelopmentDO data = toDevData(dev);
        if (existing == null) {
            productMapper.insertDevelopment(data);
        } else {
            productMapper.updateDevelopment(data);
        }
        return dev;
    }

    public Optional<ProductDevelopment> findDevelopment(String tenantId, String devId) {
        return Optional.ofNullable(productMapper.selectDevelopment(tenantId, devId))
                .map(this::toDevDomain);
    }

    public List<ProductDevelopment> listDevelopments(String tenantId) {
        return productMapper.selectDevelopments(tenantId).stream()
                .map(this::toDevDomain).collect(Collectors.toList());
    }

    public List<ProductDevelopment> listDevelopmentsBySpu(String tenantId, String spuId) {
        return productMapper.selectDevelopmentsBySpu(tenantId, spuId).stream()
                .map(this::toDevDomain).collect(Collectors.toList());
    }

    private CategoryDO toCategoryData(Category c) {
        CategoryDO data = new CategoryDO();
        data.setCategoryId(c.categoryId());
        data.setTenantId(c.tenantId());
        data.setName(c.name());
        data.setParentId(c.parentId());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        return data;
    }

    private Category toCategoryDomain(CategoryDO d) {
        return new Category(d.getCategoryId(), d.getTenantId(), d.getName(), d.getParentId(), d.getCreatedAt());
    }

    private BrandDO toBrandData(Brand b) {
        BrandDO data = new BrandDO();
        data.setBrandId(b.brandId());
        data.setTenantId(b.tenantId());
        data.setName(b.name());
        data.setCreatedAt(b.createdAt() != null ? b.createdAt() : Instant.now());
        return data;
    }

    private Brand toBrandDomain(BrandDO d) {
        return new Brand(d.getBrandId(), d.getTenantId(), d.getName(), d.getCreatedAt());
    }

    private SpuDO toSpuData(Spu s) {
        SpuDO data = new SpuDO();
        data.setSpuId(s.spuId());
        data.setTenantId(s.tenantId());
        data.setSku(s.sku());
        data.setTitle(s.title());
        data.setDescription(s.description());
        data.setCategoryId(s.categoryId());
        data.setBrandId(s.brandId());
        data.setStatus(s.status().name());
        data.setIpStatus(s.ipStatus());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private Spu toSpuDomain(SpuDO d) {
        return new Spu(d.getSpuId(), d.getTenantId(), d.getSku(), d.getTitle(), d.getDescription(),
                d.getCategoryId(), d.getBrandId(), ProductStatus.valueOf(d.getStatus()),
                d.getIpStatus(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private SkuDO toSkuData(Sku s) {
        SkuDO data = new SkuDO();
        data.setSkuId(s.skuId());
        data.setTenantId(s.tenantId());
        data.setSpuId(s.spuId());
        data.setSellerSku(s.sellerSku());
        data.setTitle(s.title());
        data.setWeightKg(s.weightKg());
        data.setDeclaredValue(s.declaredValue());
        data.setCurrency(s.currency());
        data.setStatus(s.status().name());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private Sku toSkuDomain(SkuDO d) {
        return new Sku(d.getSkuId(), d.getTenantId(), d.getSpuId(), d.getSellerSku(), d.getTitle(),
                d.getWeightKg(), d.getDeclaredValue(), d.getCurrency(),
                ProductStatus.valueOf(d.getStatus()), d.getCreatedAt(), d.getUpdatedAt());
    }

    private SelectionProposalDO toProposalData(SelectionProposal p) {
        SelectionProposalDO data = new SelectionProposalDO();
        data.setProposalId(p.proposalId());
        data.setTenantId(p.tenantId());
        data.setProductName(p.productName());
        data.setTitle(p.title());
        data.setCategoryId(p.categoryId());
        data.setSource(p.source());
        data.setSourceReference(p.sourceReference());
        data.setEstimatedCost(p.estimatedCost());
        data.setMarketAnalysis(p.marketAnalysis());
        data.setProfitEstimation(p.profitEstimation());
        data.setRiskAssessment(p.riskAssessment());
        data.setAiSuggested(p.aiSuggested());
        data.setStatus(p.status().name());
        data.setSubmittedBy(p.submittedBy());
        data.setReviewedBy(p.reviewedBy());
        data.setReviewComment(p.reviewComment());
        data.setCreatedAt(p.createdAt() != null ? p.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private SelectionProposal toProposalDomain(SelectionProposalDO d) {
        return new SelectionProposal(d.getProposalId(), d.getTenantId(), d.getProductName(), d.getTitle(),
                d.getCategoryId(), d.getSource(), d.getSourceReference(), d.getEstimatedCost(),
                d.getMarketAnalysis(), d.getProfitEstimation(), d.getRiskAssessment(), d.isAiSuggested(),
                ProposalStatus.valueOf(d.getStatus()), d.getSubmittedBy(), d.getReviewedBy(),
                d.getReviewComment(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private ProductDevelopmentDO toDevData(ProductDevelopment d) {
        ProductDevelopmentDO data = new ProductDevelopmentDO();
        data.setDevId(d.devId());
        data.setTenantId(d.tenantId());
        data.setSpuId(d.spuId());
        data.setProposalId(d.proposalId());
        data.setStage(d.stage().name());
        data.setStageNote(d.stageNote());
        data.setDeveloper(d.developer());
        data.setEditor(d.editor());
        data.setDesigner(d.designer());
        data.setPriority(d.priority());
        data.setStatus(d.status());
        data.setCreatedAt(d.createdAt() != null ? d.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private ProductDevelopment toDevDomain(ProductDevelopmentDO d) {
        return new ProductDevelopment(d.getDevId(), d.getTenantId(), d.getSpuId(), d.getProposalId(),
                DevStage.valueOf(d.getStage()), d.getStageNote(), d.getDeveloper(), d.getEditor(),
                d.getDesigner(), d.getPriority(), d.getStatus(), d.getCreatedAt(), d.getUpdatedAt());
    }
}
