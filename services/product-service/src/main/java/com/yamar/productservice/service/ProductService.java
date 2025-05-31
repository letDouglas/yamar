package com.yamar.productservice.service;

import com.yamar.productservice.dto.ProductRequest;
import com.yamar.productservice.dto.ProductResponse;
import com.yamar.productservice.dto.ProductUpdateRequest;
import com.yamar.productservice.exception.InvalidProductDataException;
import com.yamar.productservice.exception.ProductNotFoundException;
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

    public ProductResponse createProduct(ProductRequest productRequest) {
        var product = mapper.toProduct(productRequest);
        var savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());
        return mapper.toDto(savedProduct);
    }

    public List<ProductResponse> getAllProducts() {
        var products = productRepository.findAll();
        log.info("Retrieved {} products", products.size());
        return products.stream()
                .map(mapper::toDto)
                .toList();
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product with ID {} not found", id);
                    return new ProductNotFoundException("Product not found with ID: " + id);
                });
        log.info("Product retrieved with ID: {}", id);
        return mapper.toDto(product);
    }

    public ProductResponse updateProduct(ProductUpdateRequest updateRequest) {
        if (updateRequest.getId() == null || updateRequest.getId().isBlank()) {
            log.warn("Invalid update request: missing product ID");
            throw new InvalidProductDataException("Product ID must not be null or blank");
        }

        Product existingProduct = productRepository.findById(updateRequest.getId())
                .orElseThrow(() -> {
                    log.warn("Product with ID {} not found for update", updateRequest.getId());
                    return new ProductNotFoundException("Product not found with ID: " + updateRequest.getId());
                });

        mapper.updateFields(existingProduct, updateRequest);
        productRepository.save(existingProduct);
        log.info("Product updated with ID: {}", updateRequest.getId());
        return mapper.toDto(existingProduct);
    }

    public boolean existsById(String id) {
        boolean exists = productRepository.existsById(id);
        log.debug("Check existence for ID {}: {}", id, exists);
        return exists;
    }

    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            log.warn("Product with ID {} not found for delete", id);
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted with ID: {}", id);
    }
}
