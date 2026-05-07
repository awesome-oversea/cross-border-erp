package com.aidotnet.erp.bi.infrastructure.mapper;

import com.aidotnet.erp.bi.infrastructure.data.PmsInsightDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * BI-PMS 趋势洞察建议数据访问接口。
 */
@Mapper
public interface BiPmsMapper {

    void insertPmsInsight(PmsInsightDO insight);

    void updatePmsInsight(PmsInsightDO insight);

    PmsInsightDO selectPmsInsight(@Param("tenantId") String tenantId, @Param("insightId") String insightId);

    PmsInsightDO selectPmsInsightByIdempotencyKey(@Param("tenantId") String tenantId,
                                                  @Param("idempotencyKey") String idempotencyKey);
}
