package com.example.historical_figures.shared.errors;

import java.time.Instant;
import java.time.LocalDateTime;

public record ApiError(
        String message,
        String details,
        Integer errorCode,
        String path,
        String traceId,
        Instant timestamp
) {}
