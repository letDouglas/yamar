package com.yamar.productservice.repository.search;

import com.yamar.productservice.model.search.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    List<ProductDocument> findByNameOrDescription(String name, String description);
}