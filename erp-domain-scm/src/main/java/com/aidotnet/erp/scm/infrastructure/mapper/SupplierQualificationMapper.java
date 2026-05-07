package com.aidotnet.erp.scm.infrastructure.mapper;

import com.aidotnet.erp.scm.infrastructure.data.SupplierQualificationDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SupplierQualificationMapper {

    void insert(SupplierQualificationDO qualification);

    void deleteBySupplier(@Param("tenantId") String tenantId, @Param("supplierId") String supplierId);

    List<SupplierQualificationDO> selectBySupplier(@Param("tenantId") String tenantId, @Param("supplierId") String supplierId);
}
