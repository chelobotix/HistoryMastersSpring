package com.example.historical_figures.user;

import java.util.List;

public record UserResponse(
    Long id,
    String email,
    List<String> roles
) {}
