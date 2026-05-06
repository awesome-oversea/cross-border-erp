package com.aidotnet.erp.common.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageQuery(
        @Min(1) int page,
        @Min(1) @Max(500) int size,
        String sort,
        String direction
) {
    public PageQuery {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 500) size = 500;
        if (sort == null || sort.isBlank()) sort = "createdAt";
        if (direction == null || direction.isBlank()) direction = "DESC";
    }

    public long offset() {
        return (long) (page - 1) * size;
    }
}
