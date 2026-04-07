package com.ashish.ecom.product_service.controller;

import com.ashish.ecom.product_service.model.Product;
import com.ashish.ecom.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(productService.create(product));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(@RequestParam String name) {
        return ResponseEntity.ok(productService.search(name));
    }

    // Called internally by Order Service
    @PutMapping("/{id}/stock")
    public ResponseEntity<Boolean> checkStock(@PathVariable Long id,
                                              @RequestParam int quantity) {
        return ResponseEntity.ok(
                productService.checkAndReduceStock(id, quantity));
    }
}
