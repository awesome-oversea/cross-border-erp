package com.aidotnet.erp.pdm.infrastructure.mapper;

import com.aidotnet.erp.pdm.infrastructure.data.IntellectualPropertyDO;
import com.aidotnet.erp.pdm.infrastructure.data.ProductCollectionDO;
import com.aidotnet.erp.pdm.infrastructure.data.ProductVariantDO;
import com.aidotnet.erp.pdm.infrastructure.data.QualityStandardDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * PDM扩展数据MyBatis映射器
 * <p>
 * 描述: PDM域扩展功能数据访问层，提供知识产权、质检标准、
 *       产品变体、产品采集等表的数据操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface PdmExtMapper {

    /** 新增知识产权记录 */
    void insertIntellectualProperty(IntellectualPropertyDO ip);
    /** 更新知识产权记录 */
    void updateIntellectualProperty(IntellectualPropertyDO ip);
    /** 按租户ID+IP ID查询知识产权 */
    IntellectualPropertyDO selectIntellectualProperty(@Param("tenantId") String tenantId, @Param("ipId") String ipId);
    /** 按租户ID+SPU ID查询知识产权列表 */
    List<IntellectualPropertyDO> selectIntellectualPropertiesBySpu(@Param("tenantId") String tenantId, @Param("spuId") String spuId);

    /** 新增质检标准 */
    void insertQualityStandard(QualityStandardDO standard);
    /** 更新质检标准 */
    void updateQualityStandard(QualityStandardDO standard);
    /** 按租户ID+标准ID查询质检标准 */
    QualityStandardDO selectQualityStandard(@Param("tenantId") String tenantId, @Param("standardId") String standardId);
    /** 按租户ID+类目ID查询质检标准列表 */
    List<QualityStandardDO> selectQualityStandards(@Param("tenantId") String tenantId, @Param("categoryId") String categoryId);

    /** 新增产品变体 */
    void insertProductVariant(ProductVariantDO variant);
    /** 更新产品变体 */
    void updateProductVariant(ProductVariantDO variant);
    /** 按租户ID+变体ID查询产品变体 */
    ProductVariantDO selectProductVariant(@Param("tenantId") String tenantId, @Param("variantId") String variantId);
    /** 按租户ID+SPU ID查询产品变体列表 */
    List<ProductVariantDO> selectProductVariantsBySpu(@Param("tenantId") String tenantId, @Param("spuId") String spuId);

    /** 新增产品采集记录 */
    void insertProductCollection(ProductCollectionDO collection);
    /** 更新产品采集记录 */
    void updateProductCollection(ProductCollectionDO collection);
    /** 按租户ID+采集ID查询产品采集 */
    ProductCollectionDO selectProductCollection(@Param("tenantId") String tenantId, @Param("collectionId") String collectionId);
    /** 按租户ID查询产品采集列表 */
    List<ProductCollectionDO> selectProductCollections(@Param("tenantId") String tenantId);
    /** 按租户ID+状态查询产品采集列表 */
    List<ProductCollectionDO> selectProductCollectionsByStatus(@Param("tenantId") String tenantId, @Param("status") String status);
}
