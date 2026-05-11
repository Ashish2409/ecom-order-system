package com.ashish.ecom.product_service.service.impl;

import com.ashish.ecom.product_service.dto.*;
import com.ashish.ecom.product_service.exception.InsufficientStockException;
import com.ashish.ecom.product_service.exception.ResourceNotFoundException;
import com.ashish.ecom.product_service.model.Product;
import com.ashish.ecom.product_service.repository.ProductRepository;
import com.ashish.ecom.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    // ─── CREATE ───
    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request, String createdBy) {
        log.info("Creating product '{}' by {}", request.getName(), createdBy);

        Product product = Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .category(request.getCategory().trim())
                .price(request.getPrice())
                .stock(request.getStock())
                .active(true)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created id={}", saved.getId());
        return ProductResponse.from(saved);
    }

    // ─── READ ───
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::getActive)
                .orElseThrow(() -> ResourceNotFoundException.product(id));
        return ProductResponse.from(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAll(Pageable pageable) {
        Page<Product> page = productRepository.findByActiveTrue(pageable);
        return PagedResponse.from(page, ProductResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> search(String name, String category,
                                                 BigDecimal minPrice, BigDecimal maxPrice,
                                                 Pageable pageable) {
        // Treat blank strings as null for the query
        String n = (name != null && !name.isBlank()) ? name.trim() : null;
        String c = (category != null && !category.isBlank()) ? category.trim() : null;

        Page<Product> page = productRepository.searchProducts(n, c, minPrice, maxPrice, pageable);
        return PagedResponse.from(page, ProductResponse::from);
    }

    // ─── UPDATE ───
    @Override
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request, String updatedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.product(id));

        if (request.getName() != null) product.setName(request.getName().trim());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getCategory() != null) product.setCategory(request.getCategory().trim());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getActive() != null) product.setActive(request.getActive());
        product.setUpdatedBy(updatedBy);

        Product saved = productRepository.save(product);
        log.info("Product {} updated by {}", id, updatedBy);
        return ProductResponse.from(saved);
    }

    // ─── DELETE (soft) ───
    @Override
    @Transactional
    public void softDelete(Long id, String deletedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.product(id));
        product.setActive(false);
        product.setUpdatedBy(deletedBy);
        productRepository.save(product);
        log.info("Product {} soft-deleted by {}", id, deletedBy);
    }

    // ─── STOCK MANAGEMENT (called by order-service) ───
    @Override
    @Transactional
    public boolean checkAndReduceStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // ⭐ Pessimistic lock: blocks other transactions until commit
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));

        if (product.getStock() < quantity) {
            log.warn("Insufficient stock for product {}: requested={}, available={}",
                    productId, quantity, product.getStock());
            throw new InsufficientStockException(productId, quantity, product.getStock());
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        log.info("Stock reduced for product {} by {}, remaining={}",
                productId, quantity, product.getStock());
        return true;
    }

    @Override
    @Transactional
    public void restoreStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> ResourceNotFoundException.product(productId));

        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        log.info("Stock restored for product {} by {}, total={}",
                productId, quantity, product.getStock());
    }
}