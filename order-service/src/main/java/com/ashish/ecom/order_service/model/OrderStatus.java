package com.ashish.ecom.order_service.model;

public enum OrderStatus {
    PENDING,        // order created, awaiting confirmation
    CONFIRMED,      // payment/stock confirmed
    PROCESSING,     // being prepared
    SHIPPED,        // in transit
    DELIVERED,      // completed
    CANCELLED,      // user/admin cancelled
    FAILED          // saga failed
}