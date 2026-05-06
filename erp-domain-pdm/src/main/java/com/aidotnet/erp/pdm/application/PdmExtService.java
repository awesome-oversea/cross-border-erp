package com.aidotnet.erp.pdm.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.pdm.domain.CollectionStatus;
import com.aidotnet.erp.pdm.domain.IntellectualProperty;
import com.aidotnet.erp.pdm.domain.IpStatus;
import com.aidotnet.erp.pdm.domain.IpType;
import com.aidotnet.erp.pdm.domain.ProductCollection;
import com.aidotnet.erp.pdm.domain.ProductStatus;
import com.aidotnet.erp.pdm.domain.ProductVariant;
import com.aidotnet.erp.pdm.domain.QualityStandard;
import com.aidotnet.erp.pdm.infrastructure.PdmExtStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PDM扩展功能应用服务
 * <p>
 * 描述: 产品主数据扩展功能服务，负责知识产权、质检标准、产品变体、
 *       产品采集等扩展实体的业务逻辑处理。
 * </p>
 * <p>
 * 核心能力:
 *   1. 知识产权管理 - 创建/注册/标记侵权
 *   2. 质检标准管理 - 创建/更新/启停
 *   3. 产品变体管理 - 创建/激活
 *   4. 产品采集管理 - 创建/分析
 * </p>
 *
 * @author ERP系统
 */
@Service
public class PdmExtService {

    private final PdmExtStore extStore;

    /**
     * 构造函数 - 依赖注入扩展数据存储
     *
     * @param extStore PDM扩展数据存储
     */
    public PdmExtService(PdmExtStore extStore) {
        this.extStore = extStore;
    }

    /** 创建知识产权记录 */
    @Transactional
    public IntellectualProperty createIntellectualProperty(String tenantId, CreateIpCommand command) {
        Instant now = Instant.now();
        IntellectualProperty ip = new IntellectualProperty(UUID.randomUUID().toString(), tenantId, command.spuId(),
                command.type(), command.name(), command.registrationNo(), command.jurisdiction(), IpStatus.PENDING,
                command.filedAt(), command.expiresAt(), now, now);
        return extStore.saveIntellectualProperty(ip);
    }

    /** 注册知识产权(待审核→已注册) */
    @Transactional
    public IntellectualProperty registerIp(String tenantId, String ipId) {
        IntellectualProperty ip = getIntellectualProperty(tenantId, ipId);
        if (ip.status() != IpStatus.PENDING) {
            throw new BizException("IP_STATUS_INVALID", "只有待审核知识产权可以注册");
        }
        return extStore.saveIntellectualProperty(new IntellectualProperty(ip.ipId(), ip.tenantId(), ip.spuId(),
                ip.type(), ip.name(), ip.registrationNo(), ip.jurisdiction(), IpStatus.REGISTERED,
                ip.filedAt(), ip.expiresAt(), ip.createdAt(), Instant.now()));
    }

    /** 标记知识产权侵权 */
    @Transactional
    public IntellectualProperty flagIpInfringing(String tenantId, String ipId) {
        IntellectualProperty ip = getIntellectualProperty(tenantId, ipId);
        return extStore.saveIntellectualProperty(new IntellectualProperty(ip.ipId(), ip.tenantId(), ip.spuId(),
                ip.type(), ip.name(), ip.registrationNo(), ip.jurisdiction(), IpStatus.INFRINGING,
                ip.filedAt(), ip.expiresAt(), ip.createdAt(), Instant.now()));
    }

    /** 按SPU查询知识产权列表 */
    public List<IntellectualProperty> listIntellectualPropertiesBySpu(String tenantId, String spuId) {
        return extStore.listIntellectualPropertiesBySpu(tenantId, spuId);
    }

    /** 查询知识产权详情，不存在则抛出异常 */
    public IntellectualProperty getIntellectualProperty(String tenantId, String ipId) {
        return extStore.findIntellectualProperty(tenantId, ipId)
                .orElseThrow(() -> new BizException("IP_NOT_FOUND", "知识产权不存在"));
    }

    /** 创建质检标准 */
    @Transactional
    public QualityStandard createQualityStandard(String tenantId, CreateQualityStandardCommand command) {
        Instant now = Instant.now();
        QualityStandard standard = new QualityStandard(UUID.randomUUID().toString(), tenantId, command.categoryId(),
                command.name(), command.description(), command.inspectionItems(), command.acceptanceCriteria(), true, now, now);
        return extStore.saveQualityStandard(standard);
    }

    /** 更新质检标准，null字段保持原值 */
    @Transactional
    public QualityStandard updateQualityStandard(String tenantId, String standardId, UpdateQualityStandardCommand command) {
        QualityStandard existing = getQualityStandard(tenantId, standardId);
        return extStore.saveQualityStandard(new QualityStandard(existing.standardId(), existing.tenantId(),
                existing.categoryId(), command.name() != null ? command.name() : existing.name(),
                command.description() != null ? command.description() : existing.description(),
                command.inspectionItems() != null ? command.inspectionItems() : existing.inspectionItems(),
                command.acceptanceCriteria() != null ? command.acceptanceCriteria() : existing.acceptanceCriteria(),
                existing.enabled(), existing.createdAt(), Instant.now()));
    }

    /** 启停质检标准 */
    @Transactional
    public QualityStandard toggleQualityStandard(String tenantId, String standardId, boolean enabled) {
        QualityStandard existing = getQualityStandard(tenantId, standardId);
        return extStore.saveQualityStandard(new QualityStandard(existing.standardId(), existing.tenantId(),
                existing.categoryId(), existing.name(), existing.description(), existing.inspectionItems(),
                existing.acceptanceCriteria(), enabled, existing.createdAt(), Instant.now()));
    }

    /** 按类目查询质检标准列表 */
    public List<QualityStandard> listQualityStandards(String tenantId, String categoryId) {
        return extStore.listQualityStandards(tenantId, categoryId);
    }

    /** 查询质检标准详情，不存在则抛出异常 */
    public QualityStandard getQualityStandard(String tenantId, String standardId) {
        return extStore.findQualityStandard(tenantId, standardId)
                .orElseThrow(() -> new BizException("QUALITY_STANDARD_NOT_FOUND", "质检标准不存在"));
    }

    /** 创建产品变体(初始状态为DRAFT) */
    @Transactional
    public ProductVariant createProductVariant(String tenantId, CreateVariantCommand command) {
        Instant now = Instant.now();
        ProductVariant variant = new ProductVariant(UUID.randomUUID().toString(), tenantId, command.spuId(),
                command.variantName(), command.variantAttributes(), command.purchaseCost(), command.sellingPrice(),
                command.priceAdjustment(), command.sellerSku(), command.images(),
                ProductStatus.DRAFT, now, now);
        return extStore.saveProductVariant(variant);
    }

    /** 激活产品变体(草稿→上架) */
    @Transactional
    public ProductVariant activateVariant(String tenantId, String variantId) {
        ProductVariant variant = getProductVariant(tenantId, variantId);
        if (variant.status() != ProductStatus.DRAFT) {
            throw new BizException("VARIANT_STATUS_INVALID", "只有草稿变体可以激活");
        }
        return extStore.saveProductVariant(new ProductVariant(variant.variantId(), variant.tenantId(), variant.spuId(),
                variant.variantName(), variant.variantAttributes(), variant.purchaseCost(), variant.sellingPrice(),
                variant.priceAdjustment(), variant.sellerSku(), variant.images(),
                ProductStatus.ACTIVE, variant.createdAt(), Instant.now()));
    }

    /** 按SPU查询产品变体列表 */
    public List<ProductVariant> listProductVariantsBySpu(String tenantId, String spuId) {
        return extStore.listProductVariantsBySpu(tenantId, spuId);
    }

    /** 查询产品变体详情，不存在则抛出异常 */
    public ProductVariant getProductVariant(String tenantId, String variantId) {
        return extStore.findProductVariant(tenantId, variantId)
                .orElseThrow(() -> new BizException("VARIANT_NOT_FOUND", "产品变体不存在"));
    }

    /** 创建产品采集记录(初始状态为COLLECTED) */
    @Transactional
    public ProductCollection createProductCollection(String tenantId, CreateCollectionCommand command) {
        Instant now = Instant.now();
        ProductCollection collection = new ProductCollection(UUID.randomUUID().toString(), tenantId,
                command.sourcePlatform(), command.sourceUrl(), command.productName(), command.description(),
                command.images(), command.price(), command.currency(), command.categoryId(),
                command.collectedBy(), CollectionStatus.COLLECTED, now, now);
        return extStore.saveProductCollection(collection);
    }

    /** 分析产品采集(已采集→已分析) */
    @Transactional
    public ProductCollection analyzeCollection(String tenantId, String collectionId) {
        ProductCollection collection = getProductCollection(tenantId, collectionId);
        if (collection.status() != CollectionStatus.COLLECTED) {
            throw new BizException("COLLECTION_STATUS_INVALID", "只有已采集产品可以分析");
        }
        return extStore.saveProductCollection(new ProductCollection(collection.collectionId(), collection.tenantId(),
                collection.sourcePlatform(), collection.sourceUrl(), collection.productName(), collection.description(),
                collection.images(), collection.price(), collection.currency(), collection.categoryId(),
                collection.collectedBy(), CollectionStatus.ANALYZED, collection.createdAt(), Instant.now()));
    }

    /** 查询产品采集列表 */
    public List<ProductCollection> listProductCollections(String tenantId) {
        return extStore.listProductCollections(tenantId);
    }

    /** 按状态查询产品采集列表 */
    public List<ProductCollection> listProductCollectionsByStatus(String tenantId, CollectionStatus status) {
        return extStore.listProductCollectionsByStatus(tenantId, status);
    }

    /** 查询产品采集详情，不存在则抛出异常 */
    public ProductCollection getProductCollection(String tenantId, String collectionId) {
        return extStore.findProductCollection(tenantId, collectionId)
                .orElseThrow(() -> new BizException("COLLECTION_NOT_FOUND", "产品采集不存在"));
    }

    public record CreateIpCommand(String spuId, IpType type, String name, String registrationNo,
                                  String jurisdiction, Instant filedAt, Instant expiresAt) {}
    public record CreateQualityStandardCommand(String categoryId, String name, String description,
                                               String inspectionItems, String acceptanceCriteria) {}
    public record UpdateQualityStandardCommand(String name, String description, String inspectionItems, String acceptanceCriteria) {}
    public record CreateVariantCommand(String spuId, String variantName, String variantAttributes,
                                       BigDecimal purchaseCost, BigDecimal sellingPrice, BigDecimal priceAdjustment,
                                       String sellerSku, List<String> images) {}
    public record CreateCollectionCommand(String sourcePlatform, String sourceUrl, String productName,
                                          String description, String images, String price, String currency,
                                          String categoryId, String collectedBy) {}
}
