package com.ashish.ecom.product_service.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException product(Long id) {
        return new ResourceNotFoundException("Product not found with id: " + id);
    }
}
