package com.ashish.ecom.notification_service.kafka;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEventDTO {
    private Long orderId;
    private Long userId;
    private String userEmail;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String status;              // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private LocalDateTime timestamp;
}
