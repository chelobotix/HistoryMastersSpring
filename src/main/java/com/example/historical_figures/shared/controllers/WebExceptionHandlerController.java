package com.example.historical_figures.shared.controllers;

import com.example.historical_figures.shared.exceptions.DomainException;
import com.example.historical_figures.shared.services.ExceptionMapperService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class WebExceptionHandlerController {

    private final ExceptionMapperService exceptionMapperService;

    @ExceptionHandler(DomainException.class)
    public String handleDomainException(DomainException e, HttpServletRequest request) {
        return exceptionMapperService.handleDomainException(e, request.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("Resource not found: {}", request.getRequestURI());
        return "errors/error404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception e, HttpServletRequest request) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return "errors/error500";
    }
}
