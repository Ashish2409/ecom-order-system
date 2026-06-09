package com.ashish.ecom.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway - Single entry point for all microservices
 * Features:
 * - Routing to all services
 * - JWT Authentication
 * - Circuit Breaker (Resilience4j)
 * - Rate Limiting (Redis-based)
 * - Request/Response Logging
 * - Correlation ID tracking
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
