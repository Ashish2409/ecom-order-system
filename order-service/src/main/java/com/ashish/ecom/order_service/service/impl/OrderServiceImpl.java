package com.ashish.ecom.order_service.service.impl;

import com.ashish.ecom.order_service.dto.*;
import com.ashish.ecom.order_service.exception.*;
import com.ashish.ecom.order_service.kafka.OrderEvent;
import com.ashish.ecom.order_service.model.Order;
import com.ashish.ecom.order_service.model.OrderStatus;
import com.ashish.ecom.order_service.repository.OrderRepository;
import com.ashish.ecom.order_service.service.OrderService;
import com.ashish.ecom.order_service.service.ProductClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final String TOPIC = "order-events";

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final ProductClient productClient;

    /**
     * Saga orchestration:
     *   1. Idempotency check
     *   2. Validate product
     *   3. Reserve stock (Product Service)
     *   4. Save order
     *   5. Publish Kafka event
     *   6. ON FAILURE in steps 4-5: compensate (restore stock)
     *
     * NOTE: We do NOT wrap the entire saga in @Transactional because:
     *  - WebClient calls are not transactional (they happen anyway on rollback)
     *  - Long DB transactions hold locks → poor concurrency
     *  - We use explicit compensation instead
     */
    @Override
    public OrderResponse placeOrder(PlaceOrderRequest request, Long userId, String userEmail, String jwtToken) {
        log.info("Placing order: user={} product={} qty={}",
                userId, request.getProductId(), request.getQuantity());

        // ─── 1. Idempotency check ───
        if (request.getIdempotencyKey() != null) {
            Optional<Order> existing = orderRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Idempotent replay for key {}, returning existing order {}",
                        request.getIdempotencyKey(), existing.get().getId());
                return OrderResponse.from(existing.get());
            }
        }

        // ─── 2. Validate product ───
        ProductResponse product = productClient.getProduct(request.getProductId(), jwtToken);

        if (Boolean.FALSE.equals(product.getActive())) {
            throw new ProductNotFoundException(request.getProductId());
        }
        if (product.getStock() < request.getQuantity()) {
            throw new InsufficientStockException(request.getProductId(),
                    request.getQuantity(), product.getStock());
        }

        // ─── 3. Reserve stock (Product Service) ───
        boolean stockReduced = productClient.reduceStock(
                request.getProductId(), request.getQuantity(), jwtToken);
        if (!stockReduced) {
            throw new InsufficientStockException(
                    "Stock could not be reserved (race condition or insufficient stock)");
        }
        log.info("Stock reserved: product={} qty={}", request.getProductId(), request.getQuantity());

        // ─── 4-5. Save order + publish event (with compensation on failure) ───
        Order order;
        try {
            order = saveOrderAndPublishEvent(request, userId, userEmail, product);
        } catch (Exception ex) {
            // ─── COMPENSATION: restore stock ───
            log.error("Order save/publish failed, compensating by restoring stock", ex);
            productClient.restoreStock(request.getProductId(), request.getQuantity(), jwtToken);
            throw new ProductServiceException("Order placement failed: " + ex.getMessage(), ex);
        }

        log.info("Order placed successfully: id={}", order.getId());
        return OrderResponse.from(order);
    }

    @Transactional
    protected Order saveOrderAndPublishEvent(PlaceOrderRequest request, Long userId,
                                             String userEmail, ProductResponse product) {
        BigDecimal totalAmount = product.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .userId(userId)
                .userEmail(userEmail)
                .productId(product.getId())
                .productName(product.getName())
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .totalAmount(totalAmount)
                .status(OrderStatus.CONFIRMED)         // stock reserved → confirmed
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        Order saved = orderRepository.save(order);

        publishEvent(saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, Long requestingUserId, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        // ⭐ Authorization: user can only see own orders, admin sees all
        if (!isAdmin && !order.getUserId().equals(requestingUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only view your own orders");
        }
        return OrderResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        return PagedResponse.from(
                orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable),
                OrderResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getAllOrders(Pageable pageable) {
        return PagedResponse.from(
                orderRepository.findAllByOrderByCreatedAtDesc(pageable),
                OrderResponse::from);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        publishEvent(updated);

        log.info("Order {} status updated: {} → {}", orderId, order.getStatus(), newStatus);
        return OrderResponse.from(updated);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, Long requestingUserId, boolean isAdmin, String jwtToken) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!isAdmin && !order.getUserId().equals(requestingUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only cancel your own orders");
        }
        if (!order.canBeCancelled()) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order in status " + order.getStatus());
        }

        // Restore stock
        productClient.restoreStock(order.getProductId(), order.getQuantity(), jwtToken);

        // Update status
        Order cancelled = updateOrderStatus(order, OrderStatus.CANCELLED);
        log.info("Order {} cancelled, stock restored", orderId);
        return OrderResponse.from(cancelled);
    }

    @Transactional
    protected Order updateOrderStatus(Order order, OrderStatus newStatus) {
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        publishEvent(updated);
        return updated;
    }

    // ─── Helpers ───
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        // simple state machine — extend as needed
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    "Cannot change status from terminal state " + current);
        }
    }

    private void publishEvent(Order o) {
        try {
            OrderEvent event = OrderEvent.builder()
                    .orderId(o.getId())
                    .userId(o.getUserId())
                    .productId(o.getProductId())
                    .quantity(o.getQuantity())
                    .totalAmount(o.getTotalAmount())
                    .status(o.getStatus().name())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(TOPIC, String.valueOf(o.getId()), event)
                    .whenComplete((res, ex) -> {
                        if (ex != null) {
                            log.error("Kafka publish failed for order {}", o.getId(), ex);
                        } else {
                            log.info("Kafka event published: order={} status={}", o.getId(), o.getStatus());
                        }
                    });
        } catch (Exception e) {
            // Don't fail the order if Kafka is down — outbox pattern would be better
            log.error("Failed to publish Kafka event for order {}", o.getId(), e);
        }
    }
}