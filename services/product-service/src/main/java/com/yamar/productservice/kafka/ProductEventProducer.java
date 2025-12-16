package com.yamar.productservice.kafka;

import com.yamar.events.product.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
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

    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;
    private final NewTopic productEventsTopic;

    /**
     * Sends ANY Avro event (Created, Updated, Deleted) to the product topic.
     * Uses synchronous blocking to ensure data integrity logic in the service.
     */
    public void sendEvent(String key, SpecificRecord event) {
        String topic = productEventsTopic.name();
        String eventType = event.getClass().getSimpleName();

        try {
            log.debug("Sending {} for Key: {}", eventType, key);

            // 1. Send Async
            CompletableFuture<SendResult<String, SpecificRecord>> future =
                    kafkaTemplate.send(topic, key, event);

            // 2. Make it Sync (Wait max 3 seconds)
            SendResult<String, SpecificRecord> result = future.get(3, TimeUnit.SECONDS);

            log.info("{} published successfully. Partition: {}, Offset: {}",
                    eventType,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Kafka Publish Failed for " + eventType, e);
        }
    }
}
