package com.yamar.orderservice.kafka;

import com.yamar.events.OrderPlacedEvent;
import com.yamar.events.PurchasedProductInfo;
import com.yamar.orderservice.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final NewTopic ordersTopic;

    public void sendOrderPlacedEvent(Order order) {
        final String orderNumber = order.getOrderNumber();
        log.info("Preparazione dell'evento OrderPlaced per l'ordine: {}", orderNumber);

        OrderPlacedEvent event = mapToAvroEvent(order);
        final String topicName = ordersTopic.name();

        kafkaTemplate.send(topicName, orderNumber, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error(
                                "FALLIMENTO PUBBLICAZIONE EVENTO: Impossibile inviare evento per l'ordine. " +
                                        "OrderNumber='{}', Topic='{}', Errore='{}'",
                                orderNumber,
                                topicName,
                                ex.getMessage()
                        );
                    } else {
                        log.info(
                                "SUCCESSO PUBBLICAZIONE EVENTO: Inviato evento per l'ordine. " +
                                        "OrderNumber='{}', Topic='{}', Partition='{}', Offset='{}'",
                                orderNumber,
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }

    private OrderPlacedEvent mapToAvroEvent(Order order) {
        List<PurchasedProductInfo> purchasedProducts = order.getOrderLines().stream()
                .map(line -> PurchasedProductInfo.newBuilder()
                        .setProductId(line.getProductId())
                        .setQuantity(line.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderPlacedEvent.newBuilder()
                .setOrderId(order.getId())
                .setOrderNumber(order.getOrderNumber())
                .setCustomerId(order.getCustomerId())
                .setTotalAmount(order.getTotalAmount())
                .setPaymentMethod(order.getPaymentMethod().name())
                .setProducts(purchasedProducts)
                .build();
    }
}
