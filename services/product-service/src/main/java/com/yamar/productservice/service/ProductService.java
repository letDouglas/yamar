package com.yamar.productservice.service;

import com.yamar.productservice.dto.ProductBatchRequest;
import com.yamar.productservice.dto.ProductRequest;
import com.yamar.productservice.dto.ProductResponse;
import com.yamar.productservice.dto.ProductUpdateRequest;
import com.yamar.productservice.exception.InvalidProductDataException;
import com.yamar.productservice.exception.InvalidRequestException;
import com.yamar.productservice.exception.ProductNotFoundException;
import com.yamar.productservice.exception.ProductsNotFoundException;
import com.yamar.productservice.kafka.ProductEventProducer;
import com.yamar.productservice.mapper.ProductMapper;
import com.yamar.productservice.model.Product;
import com.yamar.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private static final int MAX_BATCH_SIZE = 100;
    private final ProductRepository productRepository;
    private final ProductMapper mapper;
    private final ProductEventProducer eventProducer;

    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        var product = mapper.toProduct(productRequest);
        var savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());

        /*
          Availability over consistency.
          MongoDB is the source of truth; (For now!) no rollback if Kafka publish fails.
          (Future) Consider Transactional Outbox Pattern for stricter guarantees.
         */

        try {
            eventProducer.sendProductCreated(savedProduct);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to publish product event. Search Index will be stale.", e);
        }

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


    public List<ProductResponse> getProductsByIds(ProductBatchRequest request) {
        List<String> ids = request.getProductIds();

        log.info("Attempting to retrieve products by IDs: {}", ids);

        if (ids == null || ids.isEmpty()) {
            log.error("The list of IDs cannot be empty");
            throw new InvalidRequestException("The list of IDs cannot be empty");
        }

        if (ids.size() > MAX_BATCH_SIZE) {
            log.error("Maximum {} IDs allowed per batch request", MAX_BATCH_SIZE);
            throw new InvalidRequestException("Maximum " + MAX_BATCH_SIZE + " IDs per batch request");
        }

        List<Product> products = productRepository.findAllById(ids);

        List<String> foundIds = products.stream()
                .map(Product::getId)
                .toList();

        List<String> missingIds = ids.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            log.warn("Products not found for IDs: {}", missingIds);
            throw new ProductsNotFoundException(missingIds);
        }

        log.info("Successfully retrieved {} products", products.size());
        return products.stream()
                .map(mapper::toDto)
                .toList();
    }
}
