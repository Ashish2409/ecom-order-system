package com.ashish.ecom.order_service.kafka;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {
    private Long orderId;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;     // ⭐ BigDecimal, not Double
    private String status;
    private LocalDateTime timestamp;
}