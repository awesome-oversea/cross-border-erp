package com.aidotnet.erp.scm.domain;

import java.util.List;

public record SupplierProfile(
        Supplier supplier,
        List<SupplierContact> contacts,
        List<SupplierQualification> qualifications,
        SupplierScore score
) {}
