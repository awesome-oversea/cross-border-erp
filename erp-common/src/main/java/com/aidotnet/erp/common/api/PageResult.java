package com.aidotnet.erp.common.api;

import java.util.List;

public record PageResult<T>(List<T> items, long total, int page, int size) {
    public int totalPages() {
        return (int) Math.ceil((double) total / size);
    }
}
