package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record DataDictionary(String dictId, String tenantId, String dictCode, String dictName, String dictType,
                             String parentCode, int sortOrder, boolean enabled, String remark,
                             Instant createdAt, Instant updatedAt) {}
