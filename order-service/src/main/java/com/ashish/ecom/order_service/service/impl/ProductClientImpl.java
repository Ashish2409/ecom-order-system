package com.ashish.ecom.order_service.service.impl;

import com.ashish.ecom.order_service.dto.ProductResponse;
import com.ashish.ecom.order_service.exception.ProductNotFoundException;
import com.ashish.ecom.order_service.exception.ProductServiceException;
import com.ashish.ecom.order_service.service.ProductClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class ProductClientImpl implements ProductClient {

    private final WebClient productWebClient;
    private final ObjectMapper objectMapper;

    public ProductClientImpl(@Qualifier("productWebClient") WebClient productWebClient,
                             ObjectMapper objectMapper) {
        this.productWebClient = productWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProductResponse getProduct(Long productId, String jwtToken) {
        try {
            // product-service wraps responses in ApiResponse<T>
            JsonNode body = productWebClient
                    .get()
                    .uri("/api/products/{id}", productId)
                    .headers(h -> addAuth(h, jwtToken))
                    .retrieve()
                    .onStatus(s -> s.value() == 404,
                            r -> reactor.core.publisher.Mono.error(new ProductNotFoundException(productId)))
                    .bodyToMono(JsonNode.class)
                    .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(300))
                            .filter(this::isRetryable))
                    .block();

            if (body == null || !body.has("data")) {
                throw new ProductServiceException("Empty response from product-service");
            }
            return objectMapper.treeToValue(body.get("data"), ProductResponse.class);

        } catch (ProductNotFoundException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("Product service HTTP error: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ProductServiceException("Product service error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Product service call failed", e);
            throw new ProductServiceException("Failed to fetch product: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean reduceStock(Long productId, int quantity, String jwtToken) {
        try {
            JsonNode body = productWebClient
                    .put()
                    .uri(uri -> uri.path("/api/products/{id}/stock")
                            .queryParam("quantity", quantity)
                            .build(productId))
                    .headers(h -> addAuth(h, jwtToken))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(300))
                            .filter(this::isRetryable))
                    .block();

            if (body == null || !body.has("data")) return false;
            return body.get("data").asBoolean(false);

        } catch (WebClientResponseException e) {
            log.warn("Stock reduction failed: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Stock reduction error", e);
            throw new ProductServiceException("Failed to reduce stock", e);
        }
    }

    @Override
    public void restoreStock(Long productId, int quantity, String jwtToken) {
        try {
            productWebClient
                    .put()
                    .uri(uri -> uri.path("/api/products/{id}/restore-stock")
                            .queryParam("quantity", quantity)
                            .build(productId))
                    .headers(h -> addAuth(h, jwtToken))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(500)))   // try harder for compensation
                    .block();
            log.info("Stock restored for product {} by {}", productId, quantity);
        } catch (Exception e) {
            // ⚠️ Compensation failure → critical, alert ops
            log.error("CRITICAL: Failed to restore stock for product {} qty {}. Manual intervention needed.",
                    productId, quantity, e);
        }
    }

    private void addAuth(HttpHeaders headers, String jwtToken) {
        if (jwtToken != null && !jwtToken.isBlank()) {
            headers.setBearerAuth(jwtToken);
        }
    }

    private boolean isRetryable(Throwable t) {
        if (t instanceof WebClientResponseException ex) {
            // Retry on 5xx but not 4xx
            return ex.getStatusCode().is5xxServerError();
        }
        return true;   // network errors retryable
    }
}