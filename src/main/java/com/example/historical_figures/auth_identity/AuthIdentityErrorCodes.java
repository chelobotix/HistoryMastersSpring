package com.example.historical_figures.auth_identity;

import com.example.historical_figures.shared.errors.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthIdentityErrorCodes implements ErrorCode {

    AUTH_IDENTITY_ALREADY_EXISTS(
            3001,
            HttpStatus.CONFLICT,
            "An auth identity with the given provider and uid already exists"
    ),
    AUTH_IDENTITY_PROVIDER_ALREADY_LINKED(
            3002,
            HttpStatus.CONFLICT,
            "This user already has an auth identity for the given provider"
    ),
    AUTH_IDENTITY_CANNOT_LINK_TO_UNVERIFIED_EMAIL(
            3003,
            HttpStatus.CONFLICT,
            "Oauth2 user cannot be linked to an unverified email"
    ),
    AUTH_IDENTITY_PROVIDER_NOT_ALLOWED(
            3004,
            HttpStatus.CONFLICT,
            "Oauth2 provider is not allowed"
    ),
    AUTH_IDENTITY_EMAIL_NOT_VERIFIED(
            3005,
            HttpStatus.CONFLICT,
            "Oauth2 email is not verified"
    ),
    AUTH_IDENTITY_INCONSISTENT_PROVIDER_DATA(
            3006,
            HttpStatus.CONFLICT,
            "Oauth2 inconsistent provider data"
    );

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    AuthIdentityErrorCodes(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
