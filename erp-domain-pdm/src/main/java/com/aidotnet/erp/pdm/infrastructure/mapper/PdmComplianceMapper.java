package com.aidotnet.erp.pdm.infrastructure.mapper;

import com.aidotnet.erp.pdm.infrastructure.data.SensitiveWordDO;
import com.aidotnet.erp.pdm.infrastructure.data.UpcPoolDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * PDM合规数据MyBatis映射器
 * <p>
 * 描述: PDM域合规管理数据访问层，提供敏感词和UPC码池的数据操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface PdmComplianceMapper {

    /** 新增敏感词 */
    void insertSensitiveWord(SensitiveWordDO word);
    /** 更新敏感词 */
    void updateSensitiveWord(SensitiveWordDO word);
    /** 按租户ID+词ID查询敏感词 */
    SensitiveWordDO selectSensitiveWord(@Param("tenantId") String tenantId, @Param("wordId") String wordId);
    /** 按租户ID+语言查询敏感词列表 */
    List<SensitiveWordDO> selectSensitiveWords(@Param("tenantId") String tenantId, @Param("language") String language);
    /** 查询租户下所有已启用的敏感词 */
    List<SensitiveWordDO> selectEnabledSensitiveWords(@Param("tenantId") String tenantId);

    /** 新增UPC码池记录 */
    void insertUpcPool(UpcPoolDO upc);
    /** 更新UPC码池记录 */
    void updateUpcPool(UpcPoolDO upc);
    /** 按池ID查询UPC码 */
    UpcPoolDO selectUpcByPoolId(@Param("poolId") String poolId);
    /** 按租户ID+UPC编码查询 */
    UpcPoolDO selectUpcByCode(@Param("tenantId") String tenantId, @Param("upcCode") String upcCode);
    /** 查询租户下一个可用的UPC码 */
    UpcPoolDO selectAvailableUpc(@Param("tenantId") String tenantId);
    /** 按租户ID+状态查询UPC码池列表 */
    List<UpcPoolDO> selectUpcPool(@Param("tenantId") String tenantId, @Param("status") String status);
}
