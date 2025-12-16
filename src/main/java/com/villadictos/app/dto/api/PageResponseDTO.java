package com.villadictos.app.dto.api;

import java.util.List;

/**
 * Generic paginated response wrapper
 */
public record PageResponseDTO<T>(
        List<T> content,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages) {
    public static <T> PageResponseDTO<T> from(org.springframework.data.domain.Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
