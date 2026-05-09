package com.ashish.ecom.user_service.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

// dto/ErrorResponse.java
@Getter
@Builder
public class ErrorResponse {
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String errorCode;
    private final String message;
    private final String path;
    private final List<String> details;

    public static ErrorResponse of(HttpStatus status, String code, String msg, String path) {
        return of(status, code, msg, path, null);
    }

    public static ErrorResponse of(HttpStatus status, String code, String msg, String path, List<String> details) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .errorCode(code)
                .message(msg)
                .path(path)
                .details(details)
                .build();
    }
}
