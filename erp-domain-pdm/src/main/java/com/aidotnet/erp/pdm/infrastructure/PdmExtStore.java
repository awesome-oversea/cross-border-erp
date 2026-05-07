package com.aidotnet.erp.pdm.infrastructure;

import com.aidotnet.erp.pdm.domain.CollectionStatus;
import com.aidotnet.erp.pdm.domain.ImageLibrary;
import com.aidotnet.erp.pdm.domain.IntellectualProperty;
import com.aidotnet.erp.pdm.domain.IpStatus;
import com.aidotnet.erp.pdm.domain.IpType;
import com.aidotnet.erp.pdm.domain.PlatformPriceLimit;
import com.aidotnet.erp.pdm.domain.ProductCollection;
import com.aidotnet.erp.pdm.domain.ProductStatus;
import com.aidotnet.erp.pdm.domain.ProductVariant;
import com.aidotnet.erp.pdm.domain.QualityStandard;
import com.aidotnet.erp.pdm.domain.TitleLibrary;
import com.aidotnet.erp.pdm.infrastructure.data.IntellectualPropertyDO;
import com.aidotnet.erp.pdm.infrastructure.data.ProductCollectionDO;
import com.aidotnet.erp.pdm.infrastructure.data.ProductVariantDO;
import com.aidotnet.erp.pdm.infrastructure.data.QualityStandardDO;
import com.aidotnet.erp.pdm.infrastructure.mapper.PdmExtMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * PDM扩展数据存储
 * <p>
 * 描述: PDM域扩展功能数据存储层，负责知识产权、质检标准、产品变体、
 *       产品采集等实体的CRUD操作。基于MyBatis持久化存储。
 * </p>
 * <p>
 * 数据实体:
 *   - IntellectualProperty: 知识产权记录
 *   - QualityStandard: 质检标准
 *   - ProductVariant: 产品变体
 *   - ProductCollection: 产品采集
 * </p>
 *
 * @author ERP系统
 */
@Repository
public class PdmExtStore {

    /** 扩展数据MyBatis映射器 */
    private final PdmExtMapper mapper;
    /** JSON序列化工具，用于变体图片列表的序列化/反序列化 */
    private final ObjectMapper objectMapper;

    /**
     * 构造函数 - 依赖注入映射器和JSON工具
     *
     * @param mapper        扩展数据MyBatis映射器
     * @param objectMapper  JSON序列化工具
     */
    public PdmExtStore(PdmExtMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /** 保存知识产权，存在则更新，不存在则新增 */
    public IntellectualProperty saveIntellectualProperty(IntellectualProperty ip) {
        IntellectualPropertyDO existing = mapper.selectIntellectualProperty(ip.tenantId(), ip.ipId());
        IntellectualPropertyDO data = toIpData(ip);
        if (existing == null) {
            mapper.insertIntellectualProperty(data);
        } else {
            mapper.updateIntellectualProperty(data);
        }
        return ip;
    }

    public Optional<IntellectualProperty> findIntellectualProperty(String tenantId, String ipId) {
        return Optional.ofNullable(mapper.selectIntellectualProperty(tenantId, ipId)).map(this::toIpDomain);
    }

    public List<IntellectualProperty> listIntellectualPropertiesBySpu(String tenantId, String spuId) {
        return mapper.selectIntellectualPropertiesBySpu(tenantId, spuId).stream().map(this::toIpDomain).collect(Collectors.toList());
    }

    public QualityStandard saveQualityStandard(QualityStandard standard) {
        QualityStandardDO existing = mapper.selectQualityStandard(standard.tenantId(), standard.standardId());
        QualityStandardDO data = toQualityStandardData(standard);
        if (existing == null) {
            mapper.insertQualityStandard(data);
        } else {
            mapper.updateQualityStandard(data);
        }
        return standard;
    }

    public Optional<QualityStandard> findQualityStandard(String tenantId, String standardId) {
        return Optional.ofNullable(mapper.selectQualityStandard(tenantId, standardId)).map(this::toQualityStandardDomain);
    }

    public List<QualityStandard> listQualityStandards(String tenantId, String categoryId) {
        return mapper.selectQualityStandards(tenantId, categoryId).stream().map(this::toQualityStandardDomain).collect(Collectors.toList());
    }

    public ProductVariant saveProductVariant(ProductVariant variant) {
        ProductVariantDO existing = mapper.selectProductVariant(variant.tenantId(), variant.variantId());
        ProductVariantDO data = toVariantData(variant);
        if (existing == null) {
            mapper.insertProductVariant(data);
        } else {
            mapper.updateProductVariant(data);
        }
        return variant;
    }

    public Optional<ProductVariant> findProductVariant(String tenantId, String variantId) {
        return Optional.ofNullable(mapper.selectProductVariant(tenantId, variantId)).map(this::toVariantDomain);
    }

    public List<ProductVariant> listProductVariantsBySpu(String tenantId, String spuId) {
        return mapper.selectProductVariantsBySpu(tenantId, spuId).stream().map(this::toVariantDomain).collect(Collectors.toList());
    }

    public ProductCollection saveProductCollection(ProductCollection collection) {
        ProductCollectionDO existing = mapper.selectProductCollection(collection.tenantId(), collection.collectionId());
        ProductCollectionDO data = toCollectionData(collection);
        if (existing == null) {
            mapper.insertProductCollection(data);
        } else {
            mapper.updateProductCollection(data);
        }
        return collection;
    }

    public Optional<ProductCollection> findProductCollection(String tenantId, String collectionId) {
        return Optional.ofNullable(mapper.selectProductCollection(tenantId, collectionId)).map(this::toCollectionDomain);
    }

    public List<ProductCollection> listProductCollections(String tenantId) {
        return mapper.selectProductCollections(tenantId).stream().map(this::toCollectionDomain).collect(Collectors.toList());
    }

    public List<ProductCollection> listProductCollectionsByStatus(String tenantId, CollectionStatus status) {
        return mapper.selectProductCollectionsByStatus(tenantId, status.name()).stream().map(this::toCollectionDomain).collect(Collectors.toList());
    }

    private IntellectualPropertyDO toIpData(IntellectualProperty ip) {
        IntellectualPropertyDO data = new IntellectualPropertyDO();
        data.setIpId(ip.ipId());
        data.setTenantId(ip.tenantId());
        data.setSpuId(ip.spuId());
        data.setType(ip.type().name());
        data.setName(ip.name());
        data.setRegistrationNo(ip.registrationNo());
        data.setJurisdiction(ip.jurisdiction());
        data.setStatus(ip.status().name());
        data.setFiledAt(ip.filedAt());
        data.setExpiresAt(ip.expiresAt());
        data.setCreatedAt(ip.createdAt() != null ? ip.createdAt() : Instant.now());
        data.setUpdatedAt(ip.updatedAt() != null ? ip.updatedAt() : Instant.now());
        return data;
    }

    private IntellectualProperty toIpDomain(IntellectualPropertyDO d) {
        return new IntellectualProperty(d.getIpId(), d.getTenantId(), d.getSpuId(), IpType.valueOf(d.getType()),
                d.getName(), d.getRegistrationNo(), d.getJurisdiction(), IpStatus.valueOf(d.getStatus()),
                d.getFiledAt(), d.getExpiresAt(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private QualityStandardDO toQualityStandardData(QualityStandard s) {
        QualityStandardDO data = new QualityStandardDO();
        data.setStandardId(s.standardId());
        data.setTenantId(s.tenantId());
        data.setCategoryId(s.categoryId());
        data.setName(s.name());
        data.setDescription(s.description());
        data.setInspectionItems(s.inspectionItems());
        data.setAcceptanceCriteria(s.acceptanceCriteria());
        data.setEnabled(s.enabled());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        data.setUpdatedAt(s.updatedAt() != null ? s.updatedAt() : Instant.now());
        return data;
    }

    private QualityStandard toQualityStandardDomain(QualityStandardDO d) {
        return new QualityStandard(d.getStandardId(), d.getTenantId(), d.getCategoryId(), d.getName(),
                d.getDescription(), d.getInspectionItems(), d.getAcceptanceCriteria(), d.isEnabled(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private ProductVariantDO toVariantData(ProductVariant v) {
        ProductVariantDO data = new ProductVariantDO();
        data.setVariantId(v.variantId());
        data.setTenantId(v.tenantId());
        data.setSpuId(v.spuId());
        data.setVariantName(v.variantName());
        data.setVariantAttributes(v.variantAttributes());
        data.setPurchaseCost(v.purchaseCost());
        data.setSellingPrice(v.sellingPrice());
        data.setPriceAdjustment(v.priceAdjustment());
        data.setSellerSku(v.sellerSku());
        data.setImages(serializeList(v.images()));
        data.setStatus(v.status().name());
        data.setCreatedAt(v.createdAt() != null ? v.createdAt() : Instant.now());
        data.setUpdatedAt(v.updatedAt() != null ? v.updatedAt() : Instant.now());
        return data;
    }

    private ProductVariant toVariantDomain(ProductVariantDO d) {
        return new ProductVariant(d.getVariantId(), d.getTenantId(), d.getSpuId(), d.getVariantName(),
                d.getVariantAttributes(), d.getPurchaseCost(), d.getSellingPrice(), d.getPriceAdjustment(),
                d.getSellerSku(), deserializeList(d.getImages()),
                ProductStatus.valueOf(d.getStatus()), d.getCreatedAt(), d.getUpdatedAt());
    }

    private ProductCollectionDO toCollectionData(ProductCollection c) {
        ProductCollectionDO data = new ProductCollectionDO();
        data.setCollectionId(c.collectionId());
        data.setTenantId(c.tenantId());
        data.setSourcePlatform(c.sourcePlatform());
        data.setSourceUrl(c.sourceUrl());
        data.setProductName(c.productName());
        data.setDescription(c.description());
        data.setImages(c.images());
        data.setPrice(c.price());
        data.setCurrency(c.currency());
        data.setCategoryId(c.categoryId());
        data.setCollectedBy(c.collectedBy());
        data.setStatus(c.status().name());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        data.setUpdatedAt(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        return data;
    }

    private ProductCollection toCollectionDomain(ProductCollectionDO d) {
        return new ProductCollection(d.getCollectionId(), d.getTenantId(), d.getSourcePlatform(),
                d.getSourceUrl(), d.getProductName(), d.getDescription(), d.getImages(),
                d.getPrice(), d.getCurrency(), d.getCategoryId(), d.getCollectedBy(),
                CollectionStatus.valueOf(d.getStatus()), d.getCreatedAt(), d.getUpdatedAt());
    }

    // ========== 标题库/图片库/限价管理(内存存储) ==========

    private final Map<String, TitleLibrary> titleStore = new ConcurrentHashMap<>();
    private final Map<String, ImageLibrary> imageStore = new ConcurrentHashMap<>();
    private final Map<String, PlatformPriceLimit> priceLimitStore = new ConcurrentHashMap<>();

    public TitleLibrary saveTitle(TitleLibrary title) {
        titleStore.put(title.titleId(), title);
        return title;
    }

    public Optional<TitleLibrary> findTitle(String tenantId, String titleId) {
        return Optional.ofNullable(titleStore.get(titleId))
                .filter(t -> t.tenantId().equals(tenantId));
    }

    public List<TitleLibrary> listTitles(String tenantId, String spuId) {
        return titleStore.values().stream()
                .filter(t -> t.tenantId().equals(tenantId) && t.spuId().equals(spuId))
                .collect(Collectors.toList());
    }

    public ImageLibrary saveImage(ImageLibrary image) {
        imageStore.put(image.imageId(), image);
        return image;
    }

    public List<ImageLibrary> listImages(String tenantId, String spuId) {
        return imageStore.values().stream()
                .filter(img -> img.tenantId().equals(tenantId) && img.spuId().equals(spuId))
                .collect(Collectors.toList());
    }

    public List<ImageLibrary> listImagesByType(String tenantId, String spuId, String imageType) {
        return imageStore.values().stream()
                .filter(img -> img.tenantId().equals(tenantId) && img.spuId().equals(spuId)
                        && img.imageType().equals(imageType))
                .collect(Collectors.toList());
    }

    public PlatformPriceLimit savePriceLimit(PlatformPriceLimit limit) {
        priceLimitStore.put(limit.limitId(), limit);
        return limit;
    }

    public List<PlatformPriceLimit> listPriceLimits(String tenantId, String spuId) {
        return priceLimitStore.values().stream()
                .filter(l -> l.tenantId().equals(tenantId) && l.spuId().equals(spuId))
                .collect(Collectors.toList());
    }

    private String serializeList(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<String> deserializeList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
