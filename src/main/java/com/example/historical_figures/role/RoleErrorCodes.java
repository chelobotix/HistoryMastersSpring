package com.example.historical_figures.role;

import com.example.historical_figures.shared.errors.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RoleErrorCodes implements ErrorCode {

    ROLE_NOT_FOUND(2001, HttpStatus.NOT_FOUND, "Role not found");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    RoleErrorCodes(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
