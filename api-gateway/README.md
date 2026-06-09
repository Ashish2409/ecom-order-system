# API Gateway

Single entry point for all microservices with routing, authentication, circuit breaker, and rate limiting.

## Features

- JWT Authentication & Authorization
- Circuit Breaker (Resilience4j)
- Rate Limiting (Redis-based)
- CORS Configuration
- Request Correlation ID
- Global Error Handling

## Routes

| Route | Service | Auth | Rate Limit |
|-------|---------|------|------------|
| `/api/auth/**` | user-service | Public | 20/min |
| `/api/users/**` | user-service | Required | 50/min |
| `/api/products/**` (GET) | product-service | Public | 100/min |
| `/api/products/**` (CUD) | product-service | Required | 50/min |
| `/api/orders/**` | order-service | Required | 50/min |
| `/api/notifications/**` | notification-service | Required | 50/min |

## Configuration

### Environment Variables
```properties
SERVER_PORT=8080
JWT_SECRET=your-secret-key
USER_SERVICE_URL=http://localhost:8081
PRODUCT_SERVICE_URL=http://localhost:8082
ORDER_SERVICE_URL=http://localhost:8083
NOTIFICATION_SERVICE_URL=http://localhost:8084
REDIS_HOST=localhost
REDIS_PORT=6379
```

### Circuit Breaker
- Failure Rate Threshold: 50%
- Wait Duration: 10s
- Sliding Window: 10 calls

### Rate Limiting
- Token Bucket Algorithm
- Redis-based distributed limiting
- Per-user (JWT) rate limits

## Monitoring

- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Routes: `http://localhost:8080/actuator/gateway/routes`
- Swagger: `http://localhost:8080/swagger-ui.html`
