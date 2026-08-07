package com.example.historical_figures.shared.errors;

import java.util.List;

public record ApiErrors(
        List<ApiError> errors
) {}
