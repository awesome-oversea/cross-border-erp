package com.aidotnet.erp.pdm.infrastructure.mapper;

import com.aidotnet.erp.pdm.infrastructure.data.CategoryDO;
import com.aidotnet.erp.pdm.infrastructure.data.BrandDO;
import com.aidotnet.erp.pdm.infrastructure.data.SpuDO;
import com.aidotnet.erp.pdm.infrastructure.data.SkuDO;
import com.aidotnet.erp.pdm.infrastructure.data.SelectionProposalDO;
import com.aidotnet.erp.pdm.infrastructure.data.ProductDevelopmentDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 产品主数据MyBatis映射器
 * <p>
 * 描述: PDM域核心数据访问层，提供类目、品牌、SPU、SKU、
 *       选品建议、产品开发等表的数据操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface ProductMapper {

    /** 新增类目 */
    void insertCategory(CategoryDO category);
    /** 按租户ID+类目ID查询类目 */
    CategoryDO selectCategory(@Param("tenantId") String tenantId, @Param("categoryId") String categoryId);
    /** 按租户ID查询类目列表 */
    List<CategoryDO> selectCategories(@Param("tenantId") String tenantId);

    /** 新增品牌 */
    void insertBrand(BrandDO brand);
    /** 按租户ID+品牌ID查询品牌 */
    BrandDO selectBrand(@Param("tenantId") String tenantId, @Param("brandId") String brandId);
    /** 按租户ID查询品牌列表 */
    List<BrandDO> selectBrands(@Param("tenantId") String tenantId);

    /** 新增SPU */
    void insertSpu(SpuDO spu);
    /** 更新SPU */
    void updateSpu(SpuDO spu);
    /** 按租户ID+SPU ID查询SPU */
    SpuDO selectSpu(@Param("tenantId") String tenantId, @Param("spuId") String spuId);
    /** 按租户ID查询SPU列表 */
    List<SpuDO> selectSpus(@Param("tenantId") String tenantId);

    /** 新增SKU */
    void insertSku(SkuDO sku);
    /** 更新SKU */
    void updateSku(SkuDO sku);
    /** 按租户ID+卖家SKU编码查询SKU */
    SkuDO selectSkuBySellerSku(@Param("tenantId") String tenantId, @Param("sellerSku") String sellerSku);
    /** 按租户ID+SPU ID查询SKU列表 */
    List<SkuDO> selectSkusBySpu(@Param("tenantId") String tenantId, @Param("spuId") String spuId);

    /** 新增选品建议 */
    void insertProposal(SelectionProposalDO proposal);
    /** 更新选品建议 */
    void updateProposal(SelectionProposalDO proposal);
    /** 按租户ID+建议ID查询选品建议 */
    SelectionProposalDO selectProposal(@Param("tenantId") String tenantId, @Param("proposalId") String proposalId);
    /** 按租户ID查询选品建议列表 */
    List<SelectionProposalDO> selectProposals(@Param("tenantId") String tenantId);
    /** 按租户ID+状态查询选品建议列表 */
    List<SelectionProposalDO> selectProposalsByStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    /** 新增产品开发记录 */
    void insertDevelopment(ProductDevelopmentDO dev);
    /** 更新产品开发记录 */
    void updateDevelopment(ProductDevelopmentDO dev);
    /** 按租户ID+开发ID查询产品开发 */
    ProductDevelopmentDO selectDevelopment(@Param("tenantId") String tenantId, @Param("devId") String devId);
    /** 按租户ID查询产品开发列表 */
    List<ProductDevelopmentDO> selectDevelopments(@Param("tenantId") String tenantId);
    /** 按租户ID+SPU ID查询产品开发列表 */
    List<ProductDevelopmentDO> selectDevelopmentsBySpu(@Param("tenantId") String tenantId, @Param("spuId") String spuId);
}
