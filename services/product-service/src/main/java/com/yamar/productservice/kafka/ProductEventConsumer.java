package com.yamar.productservice.kafka;

import com.yamar.events.product.ProductCreatedEvent;
import com.yamar.events.product.ProductDeletedEvent;
import com.yamar.events.product.ProductUpdatedEvent;
import com.yamar.productservice.mapper.ProductEventMapper;
import com.yamar.productservice.mapper.ProductMapper;
import com.yamar.productservice.repository.search.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@KafkaListener(
        topics = "${application.config.kafka.topics.product-events}",
        groupId = "${application.config.kafka.consumer-groups.product-search}"
)
public class ProductEventConsumer {

    private final ProductSearchRepository searchRepository;
    private final ProductEventMapper eventMapper;

    @KafkaHandler
    public void handleCreated(ProductCreatedEvent event) {
        log.info("Processing CreatedEvent for ID: {}", event.getId());
        var doc = eventMapper.toDocument(event);
        searchRepository.save(doc);
    }

    @KafkaHandler
    public void handleUpdated(ProductUpdatedEvent event) {
        log.info("Processing UpdatedEvent for ID: {}", event.getId());
        var doc = eventMapper.toDocument(event);
        searchRepository.save(doc);
    }

    @KafkaHandler
    public void handleDeleted(ProductDeletedEvent event) {
        log.info("Processing DeletedEvent for ID: {}", event.getId());
        searchRepository.deleteById(event.getId());
    }

    /**
     * Fallback handler for unknown events (Forward Compatibility).
     * Prevents the consumer from crashing if a new event version is introduced.
     */
    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object object) {
        log.warn("Received unknown event type: {}", object.getClass().getName());
    }
}