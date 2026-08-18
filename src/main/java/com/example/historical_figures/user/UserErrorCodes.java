package com.example.historical_figures.user;

import com.example.historical_figures.shared.errors.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCodes implements ErrorCode {

    USER_NOT_FOUND(1001, HttpStatus.NOT_FOUND, "User not found"),
    USER_ALREADY_EXISTS(1002, HttpStatus.CONFLICT, "A user with the given data already exists, link it in settings menu"),
    USER_INVALID_DATA(1003, HttpStatus.UNPROCESSABLE_ENTITY, "User data is invalid"),
    USER_INVALID_LOCALE(1004, HttpStatus.UNPROCESSABLE_ENTITY, "User locale is invalid"),
    USER_NOT_LOGGED_IN(1005, HttpStatus.UNAUTHORIZED, "User is not logged in");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    UserErrorCodes(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
