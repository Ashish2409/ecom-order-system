package com.ashish.ecom.product_service.service;

import com.ashish.ecom.product_service.model.Product;
import com.ashish.ecom.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public List<Product> getAll() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> search(String name) {
        return productRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(name);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    @Transactional
    public boolean checkAndReduceStock(Long productId, int quantity) {
        Product p = getById(productId);
        if (p.getStock() < quantity) return false;
        p.setStock(p.getStock() - quantity);
        productRepository.save(p);
        return true;
    }

    @Transactional
    public void restoreStock(Long productId, int quantity) {
        Product p = getById(productId);
        p.setStock(p.getStock() + quantity);
        productRepository.save(p);
    }
}
