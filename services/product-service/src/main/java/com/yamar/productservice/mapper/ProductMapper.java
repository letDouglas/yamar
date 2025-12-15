package com.yamar.productservice.mapper;

import com.yamar.events.product.ProductCreatedEvent;
import com.yamar.productservice.dto.ProductRequest;
import com.yamar.productservice.dto.ProductResponse;
import com.yamar.productservice.dto.ProductUpdateRequest;
import com.yamar.productservice.model.Category;
import com.yamar.productservice.model.Product;
import com.yamar.productservice.model.search.ProductDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductMapper {

    // --- MAPPING MONGO DB (Source of Truth) ---

    public ProductResponse toDto(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getImages(),
                product.getPrice(),
                product.getOriginCountry()
        );
    }

    public Product toProduct(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .images(request.getImages())
                .price(request.getPrice())
                .originCountry(request.getOriginCountry())
                .build();
    }

    public void updateFields(Product product, ProductUpdateRequest dto) {
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());
        if (dto.getImages() != null) product.setImages(dto.getImages());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getOriginCountry() != null) product.setOriginCountry(dto.getOriginCountry());
    }

    // --- MAPPING ELASTICSEARCH (Projection) ---

    public ProductResponse toDto(ProductDocument doc) {
        Category category = null;

        if (doc.getCategory() != null) {
            try {
                category = Category.valueOf(doc.getCategory());
            } catch (IllegalArgumentException e) {
                // Log error immediately so observability tools (ELK/Splunk) pick it up
                log.error("DATA CORRUPTION DETECTED: Invalid category value '{}' in Elasticsearch for Product ID: {}",
                        doc.getCategory(), doc.getId());
                // We leave category as null. The Frontend must handle the missing badge/label.
            }
        }

        return new ProductResponse(
                doc.getId(),
                doc.getName(),
                doc.getDescription(),
                category,
                doc.getImages(),
                doc.getPrice(),
                doc.getOriginCountry()
        );
    }

    public ProductCreatedEvent toCreatedEvent(Product product) {
        return ProductCreatedEvent.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setDescription(product.getDescription())
                .setCategory(product.getCategory().name())
                .setPrice(product.getPrice().toPlainString())
                .setOriginCountry(product.getOriginCountry())
                .setImages(product.getImages())
                .build();
    }
}