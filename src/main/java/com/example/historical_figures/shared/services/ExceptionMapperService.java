package com.example.historical_figures.shared.services;

import com.example.historical_figures.shared.exceptions.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Consumer;

@Service
@NoArgsConstructor
@Slf4j
public class ExceptionMapperService {

    public String handleDomainException(DomainException e, String url){
        int code = e.getErrorCode().getCode();
        String message = e.getErrorCode().getMessage();
        HttpStatus status = e.getErrorCode().getHttpStatus();
        generateDomainExceptionLogs(code, message, status, url);

        return selectView(status);
    }

    public String handleGenericException(Exception e, String url){
        String traceId = newTraceId();

        log.error("[{}] Unhandled exception at {}", traceId, url, e);

        return "errors/error500";
    }

    private String selectView(HttpStatus status) {
        if (status.is4xxClientError()) {
            return "errors/error400";
        }

        return "errors/error500";
    }

    private void generateDomainExceptionLogs(int code, String message, HttpStatus status, String url){
        String traceId = newTraceId();

        Consumer<String> logger = status.is4xxClientError() ? log::warn : log::error;

        logger.accept(String.format(
                "[%s] DomainException: url: %s, code: %s, status: %s, message: %s",
                traceId,
                url,
                code,
                status.value(),
                message
        ));
    }

    private String newTraceId() {
        return UUID.randomUUID().toString();
    }
}
