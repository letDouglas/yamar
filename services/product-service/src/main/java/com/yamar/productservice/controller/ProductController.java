package com.yamar.productservice.controller;

import com.yamar.productservice.dto.ProductRequest;
import com.yamar.productservice.dto.ProductResponse;
import com.yamar.productservice.dto.ProductUpdateRequest;
import com.yamar.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productRequest));
    }


    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PutMapping
    public ResponseEntity<ProductResponse> updateProduct(@Valid @RequestBody ProductUpdateRequest updateRequest) {
        return ResponseEntity.ok(productService.updateProduct(updateRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }


    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> existsProduct(@PathVariable String id) {
        return ResponseEntity.ok(productService.existsById(id));
    }
}
