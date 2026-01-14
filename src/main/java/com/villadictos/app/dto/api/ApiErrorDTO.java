package com.villadictos.app.dto.api;

import java.time.Instant;

/**
 * Standard API error response
 */
public record ApiErrorDTO(
        String error,
        String message,
        Integer status,
        Instant timestamp,
        String path) {
    public static ApiErrorDTO of(String error, String message, Integer status, String path) {
        return new ApiErrorDTO(error, message, status, Instant.now(), path);
    }
}
