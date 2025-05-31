package com.yamar.productservice.service;

import com.yamar.productservice.dto.ProductRequest;
import com.yamar.productservice.dto.ProductResponse;
import com.yamar.productservice.dto.ProductUpdateRequest;
import com.yamar.productservice.mapper.ProductMapper;
import com.yamar.productservice.model.Product;
import com.yamar.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper mapper;

    public void createProduct(ProductRequest productRequest) {
        var product = mapper.toProduct(productRequest);
        productRepository.save(product);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapper.toDto(product);
    }

    public ProductResponse updateProduct(ProductUpdateRequest updateRequest) {
        if (updateRequest.getId() == null || updateRequest.getId().isBlank()) {
            throw new IllegalArgumentException("Product ID must not be null or blank");
        }

        Product existingProduct = productRepository.findById(updateRequest.getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        mapper.updateFields(existingProduct, updateRequest);

        productRepository.save(existingProduct);

        return mapper.toDto(existingProduct);
    }

    public boolean existsById(String id) {
        return productRepository.existsById(id);
    }
}
