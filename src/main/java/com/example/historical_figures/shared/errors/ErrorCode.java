package com.example.historical_figures.shared.errors;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    int getCode();
    HttpStatus getHttpStatus();
    String getMessage();
}
