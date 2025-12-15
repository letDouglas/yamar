package com.yamar.productservice.service;

import com.yamar.productservice.dto.ProductResponse;
import com.yamar.productservice.mapper.ProductMapper;
import com.yamar.productservice.model.search.ProductDocument;
import com.yamar.productservice.repository.search.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final ProductSearchRepository searchRepository;
    private final ProductMapper mapper;

    public List<ProductResponse> searchProducts(String query) {
        log.info("Searching products with query: {}", query);
        List<ProductDocument> results = searchRepository.findByNameOrDescription(query, query);

        return results.stream()
                .map(mapper::toDto)
                .toList();
    }
}