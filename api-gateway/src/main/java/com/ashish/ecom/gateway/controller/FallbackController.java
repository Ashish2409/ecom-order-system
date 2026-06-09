package com.ashish.ecom.gateway.controller;

import com.ashish.ecom.gateway.dto.FallbackResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

/**
 * Fallback endpoints for Circuit Breaker
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/user-service")
    public ResponseEntity<FallbackResponse> userServiceFallback(ServerWebExchange exchange) {
        String correlationId = exchange.getAttribute("X-Correlation-Id");
        log.warn("User service is unavailable - Correlation ID: {}", correlationId);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(FallbackResponse.of(
                        "user-service",
                        "User service is temporarily unavailable. Please try again later.",
                        correlationId
                ));
    }

    @GetMapping("/product-service")
    public ResponseEntity<FallbackResponse> productServiceFallback(ServerWebExchange exchange) {
        String correlationId = exchange.getAttribute("X-Correlation-Id");
        log.warn("Product service is unavailable - Correlation ID: {}", correlationId);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(FallbackResponse.of(
                        "product-service",
                        "Product service is temporarily unavailable. Please try again later.",
                        correlationId
                ));
    }

    @GetMapping("/order-service")
    public ResponseEntity<FallbackResponse> orderServiceFallback(ServerWebExchange exchange) {
        String correlationId = exchange.getAttribute("X-Correlation-Id");
        log.warn("Order service is unavailable - Correlation ID: {}", correlationId);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(FallbackResponse.of(
                        "order-service",
                        "Order service is temporarily unavailable. Please try again later.",
                        correlationId
                ));
    }

    @GetMapping("/notification-service")
    public ResponseEntity<FallbackResponse> notificationServiceFallback(ServerWebExchange exchange) {
        String correlationId = exchange.getAttribute("X-Correlation-Id");
        log.warn("Notification service is unavailable - Correlation ID: {}", correlationId);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(FallbackResponse.of(
                        "notification-service",
                        "Notification service is temporarily unavailable. Please try again later.",
                        correlationId
                ));
    }
}
