package com.yamar.productservice.kafka;

import com.yamar.events.product.ProductCreatedEvent;
import com.yamar.productservice.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final NewTopic productEventsTopic;

    public void sendProductCreated(Product product) {
        log.info("Preparing to send ProductCreatedEvent for product ID: {}", product.getId());

        ProductCreatedEvent event = ProductCreatedEvent.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setDescription(product.getDescription())
                .setCategory(product.getCategory().name())
                .setPrice(product.getPrice().toString()) // Convert BigDecimal to String
                .setOriginCountry(product.getOriginCountry())
                .setImages(product.getImages())
                .build();

        // We use the product ID as the key to guarantee ordering within the partition
        String key = product.getId();

        kafkaTemplate.send(productEventsTopic.name(), key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send ProductCreatedEvent for ID: {}", key, ex);
                        // TODO: Implement Outbox Pattern logic here for reliability
                    } else {
                        log.info("ProductCreatedEvent sent successfully. Offset: {}", result.getRecordMetadata().offset());
                    }
                });
    }
}
