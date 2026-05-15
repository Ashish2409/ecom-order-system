package com.ashish.ecom.order_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderRequest {

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000, message = "Quantity cannot exceed 1000 per order")
    private Integer quantity;

    /**
     * Optional client-provided idempotency key (UUID recommended).
     * Same key + same body = same response (prevents duplicate orders).
     */
    @Size(max = 64, message = "Idempotency key max 64 chars")
    private String idempotencyKey;
}