package com.ashish.ecom.order_service.service;


import com.ashish.ecom.order_service.kafka.KafkaProducerConfig;
import com.ashish.ecom.order_service.kafka.OrderEvent;
import com.ashish.ecom.order_service.model.*;
import com.ashish.ecom.order_service.dto.ProductResponse;
import com.ashish.ecom.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.List;

import com.ashish.ecom.order_service.config.WebClientConfig;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository   orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final WebClient productWebClient;
    public OrderService(OrderRepository orderRepository,
                        KafkaTemplate<String, OrderEvent> kafkaTemplate,
                        @Qualifier("productWebClient") WebClient productWebClient) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.productWebClient = productWebClient;
    }

    @Value("${services.product-url}")
    private String productUrl;

    private static final String TOPIC = "order-events";

    @Transactional
    public Order placeOrder(Long userId, Long productId, int quantity) {

        // 1. Fetch product details
        ProductResponse product;
        try {
            product = productWebClient
                    .get()
                    .uri(productUrl + "/api/products/" + productId)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Product Service unavailable or product not found");
        }

        if (product == null || !Boolean.TRUE.equals(product.getActive())) {
            throw new RuntimeException("Invalid or inactive product");
        }

        // 2. Check stock
        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        // 3. Reduce stock (separate call)
        Boolean stockUpdated = productWebClient
                .put()
                .uri(productUrl + "/api/products/" + productId + "/stock?quantity=" + quantity)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (!Boolean.TRUE.equals(stockUpdated)) {
            throw new RuntimeException("Failed to update stock");
        }

        // 4. Calculate totalAmount using REAL price
        double totalAmount = product.getPrice() * quantity;

        // 5. Save order
        Order order = Order.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        Order saved = orderRepository.save(order);

        // 6. Publish Kafka event
        publishEvent(saved);

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
