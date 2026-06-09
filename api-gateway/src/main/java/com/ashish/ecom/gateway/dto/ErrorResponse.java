package com.ashish.ecom.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String correlationId;

    public static ErrorResponse of(int status, String error, String message, String path, String correlationId) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                correlationId
        );
    }
}
