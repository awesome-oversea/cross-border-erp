package com.aidotnet.erp.scm.infrastructure.mapper;

import com.aidotnet.erp.scm.infrastructure.data.SupplierContactDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SupplierContactMapper {

    void insert(SupplierContactDO contact);

    void deleteBySupplier(@Param("tenantId") String tenantId, @Param("supplierId") String supplierId);

    List<SupplierContactDO> selectBySupplier(@Param("tenantId") String tenantId, @Param("supplierId") String supplierId);
}
