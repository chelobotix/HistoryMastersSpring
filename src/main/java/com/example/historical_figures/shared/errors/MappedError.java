package com.example.historical_figures.shared.errors;

import org.springframework.http.HttpStatus;

import java.time.Instant;

/**
 * Neutral error representation shared by web and API exception handlers.
 */
public record MappedError(
        HttpStatus status,
        String message,
        String details,
        Integer errorCode,
        String path,
        String traceId,
        Instant timestamp
) {
    public ApiError toApiError() {
        return new ApiError(message, details, errorCode, path, traceId, timestamp);
    }
}
