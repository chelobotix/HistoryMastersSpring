package com.example.historical_figures.auth_identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthIdentityCreateRequest(
    @NotBlank
    @Email
    String email,

    @NotNull
    AuthIdentityProvider provider,

    @NotBlank
    String uid,

    @NotNull
    Boolean emailVerified,
    
    String picture
){}
