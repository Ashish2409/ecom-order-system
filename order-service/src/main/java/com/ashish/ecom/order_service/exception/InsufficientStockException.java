package com.ashish.ecom.order_service.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, int requested, int available) {
        super(String.format("Insufficient stock for product %d. Requested: %d, Available: %d",
                productId, requested, available));
    }
    public InsufficientStockException(String message) {
        super(message);
    }
}