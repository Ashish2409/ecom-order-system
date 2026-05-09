package com.ashish.ecom.user_service.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// dto/ApiResponse.java
@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final Instant timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data)
                .timestamp(Instant.now()).build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("OK", data);
    }
}
