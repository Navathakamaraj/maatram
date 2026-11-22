package org.maatram.dto;

import java.util.List;

public record PaginatedResponse<T>(List<T> items, long total, int page, int pageSize) {
    public static <T> PaginatedResponse<T> of(List<T> rows, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 20;
        int start = (page - 1) * pageSize;
        List<T> items;
        if (start >= rows.size()) {
            items = List.of();
        } else {
            items = rows.subList(start, Math.min(start + pageSize, rows.size()));
        }
        return new PaginatedResponse<>(items, rows.size(), page, pageSize);
    }
}
