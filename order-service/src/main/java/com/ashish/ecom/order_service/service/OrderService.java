package com.ashish.ecom.order_service.service;


import com.ashish.ecom.order_service.kafka.KafkaProducerConfig;
import com.ashish.ecom.order_service.kafka.OrderEvent;
import com.ashish.ecom.order_service.model.*;
import com.ashish.ecom.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.List;

import com.ashish.ecom.order_service.config.WebClientConfig;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository   orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${services.product-url}")
    private String productUrl;

    private static final String TOPIC = "order-events";

    @Transactional
    public Order placeOrder(Long userId, Long productId, int quantity) {

        // 1. Check & reduce stock in Product Service (sync REST call)
        Boolean ok = webClientBuilder.build()
                .put()
                .uri(productUrl + "/api/products/" + productId
                        + "/stock?quantity=" + quantity)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (!Boolean.TRUE.equals(ok))
            throw new RuntimeException(
                    "Insufficient stock for product: " + productId);

        // 2. Save order
        Order order = Order.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .totalAmount(quantity * 999.0)   // simplified — fetch real price
                .status(OrderStatus.PENDING)
                .build();
        Order saved = orderRepository.save(order);

        // 3. Publish Kafka event (async)
        publishEvent(saved);
        log.info("Order #{} placed for user {}", saved.getId(), userId);
        return saved;
    }
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        publishEvent(updated);
        return updated;
    }

    private void publishEvent(Order o) {
        OrderEvent event = OrderEvent.builder()
                .orderId(o.getId())
                .userId(o.getUserId())
                .productId(o.getProductId())
                .quantity(o.getQuantity())
                .totalAmount(o.getTotalAmount())
                .status(o.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();
        kafkaTemplate.send(TOPIC, String.valueOf(o.getId()), event);
        log.info("Kafka event published: order={} status={}",
                o.getId(), o.getStatus());
    }


}
