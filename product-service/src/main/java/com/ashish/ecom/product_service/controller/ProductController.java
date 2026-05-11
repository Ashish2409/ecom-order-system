package com.ashish.ecom.product_service.controller;

import com.ashish.ecom.product_service.common.ApiResponse;
import com.ashish.ecom.product_service.dto.*;
import com.ashish.ecom.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product catalog operations")
public class ProductController {

    private final ProductService productService;

    // ─── PUBLIC: List products (paginated) ───
    @GetMapping
    @Operation(summary = "List active products (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Products fetched", productService.getAll(pageable)));
    }

    // ─── PUBLIC: Search ───
    @GetMapping("/search")
    @Operation(summary = "Search products with filters")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Search results",
                productService.search(name, category, minPrice, maxPrice, pageable)));
    }

    // ─── PUBLIC: Get by ID ───
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Product fetched", productService.getById(id)));
    }

    // ─── ADMIN: Create ───
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new product (admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody CreateProductRequest request,
            Authentication auth) {
        ProductResponse created = productService.create(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", created));
    }

    // ─── ADMIN: Update ───
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product (admin only)")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product updated",
                productService.update(id, request, auth.getName())));
    }

    // ─── ADMIN: Soft delete ───
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete product (admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication auth) {
        productService.softDelete(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }

    // ─── INTERNAL: Stock decrement (called by order-service) ───
    @PutMapping("/{id}/stock")
    @Operation(summary = "Reduce stock (called by order-service)")
    public ResponseEntity<ApiResponse<Boolean>> reduceStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        boolean success = productService.checkAndReduceStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stock updated", success));
    }

    // ─── INTERNAL: Stock restore (compensation on order failure) ───
    @PutMapping("/{id}/restore-stock")
    @Operation(summary = "Restore stock (compensation)")
    public ResponseEntity<ApiResponse<Void>> restoreStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        productService.restoreStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stock restored", null));
    }
}