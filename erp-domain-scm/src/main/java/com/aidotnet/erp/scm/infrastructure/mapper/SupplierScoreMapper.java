package com.aidotnet.erp.scm.infrastructure.mapper;

import com.aidotnet.erp.scm.infrastructure.data.SupplierScoreDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SupplierScoreMapper {

    void insert(SupplierScoreDO score);

    void update(SupplierScoreDO score);

    SupplierScoreDO selectBySupplier(@Param("tenantId") String tenantId, @Param("supplierId") String supplierId);
}
