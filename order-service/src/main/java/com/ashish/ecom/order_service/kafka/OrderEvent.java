package com.ashish.ecom.order_service.kafka;

import lombok.*;
import java.time.LocalDateTime;

// This POJO is serialised to JSON and sent to Kafka topic "order-events"
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderEvent {
    private Long   orderId;
    private Long   userId;
    private Long   productId;
    private Integer quantity;
    private Double  totalAmount;
    private String  status;          // matches OrderStatus enum name
    private LocalDateTime timestamp;
}
