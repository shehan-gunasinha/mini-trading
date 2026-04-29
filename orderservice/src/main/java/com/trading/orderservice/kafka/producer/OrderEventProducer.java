package com.trading.orderservice.kafka.producer;

import com.trading.orderservice.entity.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor

public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topics.order-updated}")
    private String orderUpdatedTopic;

    @Value("${kafka.topics.order-deleted}")
    private String orderDeletedTopic;

    public void sendOrderCreatedEvent(Order order) {
        sendEvent(orderCreatedTopic, order.getId().toString(), order, "ORDER_CREATED");
    }

    public void sendOrderUpdatedEvent(Order order) {
        sendEvent(orderUpdatedTopic, order.getId().toString(), order, "ORDER_UPDATED");
    }

    public void sendOrderDeletedEvent(String orderId) {
        String payload = String.format(
                "{\"eventType\":\"ORDER_DELETED\",\"orderId\":\"%s\"}", orderId
        );
        publish(orderDeletedTopic, orderId, payload);
    }

    private void sendEvent(String topic, String key, Order order, String eventType) {
        try {
            // Wrap order + event type into a simple JSON envelope
            String payload = objectMapper.writeValueAsString(
                    new OrderEvent(eventType, order)
            );
            publish(topic, key, payload);
        } catch (Exception e) {
            log.error("Failed to serialize order event for orderId={}", key, e);
        }
    }

    private void publish(String topic, String key, String payload) {
        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, key, payload);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send Kafka message to topic={}, key={}: {}",
                        topic, key, ex.getMessage());
            } else {
                log.debug("Sent Kafka message → topic={}, partition={}, offset={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    // ---- Inner envelope record ----
    public record OrderEvent(String eventType, Order order) {}
}
