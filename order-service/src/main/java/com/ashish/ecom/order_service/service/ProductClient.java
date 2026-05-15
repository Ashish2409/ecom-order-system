package com.ashish.ecom.order_service.service;

import com.ashish.ecom.order_service.dto.ProductResponse;

public interface ProductClient {

    ProductResponse getProduct(Long productId, String jwtToken);

    boolean reduceStock(Long productId, int quantity, String jwtToken);

    void restoreStock(Long productId, int quantity, String jwtToken);
}