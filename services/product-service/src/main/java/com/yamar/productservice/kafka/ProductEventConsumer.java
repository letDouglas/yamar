package com.yamar.productservice.kafka;

import com.yamar.events.product.ProductCreatedEvent;
import com.yamar.productservice.model.search.ProductDocument;
import com.yamar.productservice.repository.search.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final ProductSearchRepository searchRepository;

    @KafkaListener(
            topics = "${application.config.kafka.topics.product-events}",
            groupId = "${application.config.kafka.consumer-groups.product-search}"
    )
    public void consumeProductCreated(ConsumerRecord<String, ProductCreatedEvent> record) {
        ProductCreatedEvent event = record.value();
        log.info("Indexing Product ID: {}", event.getId());

        ProductDocument document = ProductDocument.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .category(event.getCategory())
                .price(new BigDecimal(event.getPrice()))
                .originCountry(event.getOriginCountry())
                .images(event.getImages())
                .build();

        searchRepository.save(document);
        log.info("Product ID: {} successfully indexed in Elasticsearch.", event.getId());
    }
}