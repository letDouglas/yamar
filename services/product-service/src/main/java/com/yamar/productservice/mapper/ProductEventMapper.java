package com.yamar.productservice.mapper;

import com.yamar.events.product.ProductCreatedEvent;
import com.yamar.events.product.ProductDeletedEvent;
import com.yamar.events.product.ProductUpdatedEvent;
import com.yamar.productservice.model.Product;
import com.yamar.productservice.model.search.ProductDocument;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductEventMapper {

    // --- FROM ENTITY TO AVRO (Producer Side) ---

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

    public ProductUpdatedEvent toUpdatedEvent(Product product) {
        return ProductUpdatedEvent.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setDescription(product.getDescription())
                .setCategory(product.getCategory().name())
                .setPrice(product.getPrice().toPlainString())
                .setOriginCountry(product.getOriginCountry())
                .setImages(product.getImages())
                .build();
    }

    public ProductDeletedEvent toDeletedEvent(String id) {
        return ProductDeletedEvent.newBuilder()
                .setId(id)
                .build();
    }

    // --- FROM AVRO TO ELASTIC DOCUMENT (Consumer Side) ---

    public ProductDocument toDocument(ProductCreatedEvent event) {
        return buildDocument(
                event.getId(), event.getName(), event.getDescription(),
                event.getCategory(), event.getPrice(), event.getOriginCountry(), event.getImages()
        );
    }

    public ProductDocument toDocument(ProductUpdatedEvent event) {
        return buildDocument(
                event.getId(), event.getName(), event.getDescription(),
                event.getCategory(), event.getPrice(), event.getOriginCountry(), event.getImages()
        );
    }

    private ProductDocument buildDocument(String id, String name, String description, String category, String price, String origin, List<String> images) {
        return ProductDocument.builder()
                .id(id)
                .name(name)
                .description(description)
                .category(category)
                .price(new BigDecimal(price))
                .originCountry(origin)
                .images(images)
                .build();
    }
}