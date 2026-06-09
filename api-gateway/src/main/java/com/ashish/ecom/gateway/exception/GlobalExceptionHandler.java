package com.ashish.ecom.gateway.exception;

import com.ashish.ecom.gateway.dto.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for API Gateway
 */
@Component
@Order(-2)
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        String correlationId = exchange.getAttribute("X-Correlation-Id");
        String path = exchange.getRequest().getURI().getPath();

        HttpStatus status;
        String message;

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred";
            log.error("Unexpected error: ", ex);
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                message != null ? message : status.getReasonPhrase(),
                path,
                correlationId
        );

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            return exchange.getResponse().writeWith(Mono.just(bufferFactory.wrap(bytes)));
        } catch (JsonProcessingException e) {
            log.error("Error writing error response", e);
            return exchange.getResponse().setComplete();
        }
    }
}
