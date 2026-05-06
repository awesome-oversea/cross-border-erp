package com.aidotnet.erp.scm.infrastructure.mapper;

import com.aidotnet.erp.scm.infrastructure.data.ReplenishmentSuggestionDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * SCM域补货建议MyBatis映射器
 * <p>
 * 描述: 补货建议数据访问层，提供补货建议的CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface ReplenishmentSuggestionMapper {

    /** 新增补货建议 */
    void insert(ReplenishmentSuggestionDO suggestion);

    /** 更新补货建议 */
    void update(ReplenishmentSuggestionDO suggestion);

    /** 按租户ID+建议ID查询 */
    ReplenishmentSuggestionDO selectById(@Param("tenantId") String tenantId, @Param("suggestionId") String suggestionId);

    /** 按租户ID查询补货建议列表 */
    List<ReplenishmentSuggestionDO> selectByTenant(@Param("tenantId") String tenantId);

    /** 按租户ID查询待处理建议 */
    List<ReplenishmentSuggestionDO> selectPending(@Param("tenantId") String tenantId);
}
