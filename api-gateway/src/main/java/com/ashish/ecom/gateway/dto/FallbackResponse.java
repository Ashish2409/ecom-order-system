package com.ashish.ecom.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FallbackResponse {
    private LocalDateTime timestamp;
    private String message;
    private String service;
    private String correlationId;

    public static FallbackResponse of(String service, String message, String correlationId) {
        return new FallbackResponse(
                LocalDateTime.now(),
                message,
                service,
                correlationId
        );
    }
}
