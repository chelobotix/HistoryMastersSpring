package com.example.historical_figures.shared.exceptions;

import com.example.historical_figures.shared.errors.ErrorCode;
import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final String details;
    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());

        this.errorCode = errorCode;
        this.details = null;
    }

    public DomainException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage());
        
        this.errorCode = errorCode;
        this.details = details;
    }
}
