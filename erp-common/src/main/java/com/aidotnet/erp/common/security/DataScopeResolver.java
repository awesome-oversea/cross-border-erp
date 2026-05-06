package com.aidotnet.erp.common.security;

import java.util.Set;

public interface DataScopeResolver {

    DataScope resolve(String tenantId, String userId);
}
