package com.yamar.productservice.kafka;

import com.yamar.events.product.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final NewTopic productEventsTopic;

    /**
     * Sends the event to Kafka and WAITS for acknowledgment.
     * This ensures strict observability: if it fails, throw Exception.
     */
    public void sendProductCreated(ProductCreatedEvent event) {
        String topic = productEventsTopic.name();
        String key = event.getId();

        try {
            log.debug("Sending ProductCreatedEvent for ID: {}", key);

            // 1. Send Async
            CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
                    kafkaTemplate.send(topic, key, event);

            // 2. Make it Sync (Wait max 3 seconds)
            SendResult<String, ProductCreatedEvent> result = future.get(3, TimeUnit.SECONDS);

            log.info("Event published successfully. Topic: {}, Partition: {}, Offset: {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Kafka Publish Failed", e);
        }
    }
}
