package com.ashish.ecom.order_service.controller;

import com.ashish.ecom.order_service.model.*;
import com.ashish.ecom.order_service.repository.OrderRepository;
import com.ashish.ecom.order_service.service.OrderService;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService    orderService;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @RequestBody OrderRequest req) {
        return ResponseEntity.ok(
                orderService.placeOrder(req.getUserId(),
                        req.getProductId(),
                        req.getQuantity()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> userOrders(
            @PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, status));
    }

    // DTO
    @Data static class OrderRequest {
        private Long    userId;
        private Long    productId;
        private Integer quantity;
    }
}
