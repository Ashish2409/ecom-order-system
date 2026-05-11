package com.ashish.ecom.product_service.service;

import com.ashish.ecom.product_service.dto.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {

    ProductResponse create(CreateProductRequest request, String createdBy);

    ProductResponse getById(Long id);

    PagedResponse<ProductResponse> getAll(Pageable pageable);

    PagedResponse<ProductResponse> search(String name, String category,
                                          BigDecimal minPrice, BigDecimal maxPrice,
                                          Pageable pageable);

    ProductResponse update(Long id, UpdateProductRequest request, String updatedBy);

    void softDelete(Long id, String deletedBy);

    boolean checkAndReduceStock(Long productId, int quantity);

    void restoreStock(Long productId, int quantity);
}