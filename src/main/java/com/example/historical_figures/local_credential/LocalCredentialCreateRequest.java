package com.example.historical_figures.local_credential;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocalCredentialCreateRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 6, max = 20)
        String password
) {}
