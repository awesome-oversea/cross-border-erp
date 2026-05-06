package com.aidotnet.erp.sys.infrastructure.mapper;

import com.aidotnet.erp.sys.infrastructure.data.PmsRecommendationDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * PMS推荐Mapper接口(PmsRecommendationMapper)
 * <p>
 * 描述: PMS推荐表的MyBatis数据访问接口，提供推荐记录的CRUD操作。
 *       支持按租户隔离查询、按域过滤、按幂等键去重、按状态查询。
 * </p>
 * <p>
 * 方法说明:
 *   - insert: 新增推荐记录
 *   - update: 更新推荐状态和结果
 *   - selectByErpReferenceId: 按ERP引用ID精确查询
 *   - selectByIdempotencyKey: 按幂等键查询(防重复提交)
 *   - selectByTenant: 查询租户下所有推荐
 *   - selectByDomain: 按域查询推荐
 *   - selectByStatus: 按状态查询推荐
 *   - deleteByErpReferenceId: 按ERP引用ID删除
 * </p>
 *
 * @author ERP系统
 * @see PmsRecommendationDO
 */
@Mapper
public interface PmsRecommendationMapper {

    void insert(PmsRecommendationDO recommendation);

    void update(PmsRecommendationDO recommendation);

    PmsRecommendationDO selectByErpReferenceId(@Param("tenantId") String tenantId,
                                                @Param("erpReferenceId") String erpReferenceId);

    PmsRecommendationDO selectByIdempotencyKey(@Param("tenantId") String tenantId,
                                                @Param("domain") String domain,
                                                @Param("idempotencyKey") String idempotencyKey);

    List<PmsRecommendationDO> selectByTenant(@Param("tenantId") String tenantId);

    List<PmsRecommendationDO> selectByDomain(@Param("tenantId") String tenantId, @Param("domain") String domain);

    List<PmsRecommendationDO> selectByStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    void deleteByErpReferenceId(@Param("tenantId") String tenantId, @Param("erpReferenceId") String erpReferenceId);
}
