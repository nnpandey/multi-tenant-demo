package com.example.multitenant.controller;

import com.example.multitenant.entity.Product;
import com.example.multitenant.service.ProductService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Transactional
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return productService.getAllProducts();
    }
}