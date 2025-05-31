package com.yamar.productservice.mapper;

import com.yamar.productservice.dto.ProductRequest;
import com.yamar.productservice.dto.ProductResponse;
import com.yamar.productservice.dto.ProductUpdateRequest;
import com.yamar.productservice.model.Product;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {

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
        if (dto.getName() != null) {
            product.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            product.setCategory(dto.getCategory());
        }
        if (dto.getImages() != null) {
            product.setImages(dto.getImages());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getOriginCountry() != null) {
            product.setOriginCountry(dto.getOriginCountry());
        }
    }
}
