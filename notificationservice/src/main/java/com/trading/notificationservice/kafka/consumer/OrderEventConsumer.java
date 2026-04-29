package com.trading.notificationservice.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trading.notificationservice.dto.OrderEvent;
import com.trading.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final NotificationService notificationService;

    // ObjectMapper handles JSON → Java object conversion
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // ---- Listen to ORDER CREATED topic ----
    @KafkaListener(
            topics = "${kafka.topics.order-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrderCreated(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment) {

        log.debug("Received ORDER_CREATED event | key={} | partition={} | offset={}",
                record.key(), record.partition(), record.offset());

        try {
            OrderEvent event = objectMapper.readValue(record.value(), OrderEvent.class);
            notificationService.handleOrderCreated(event);

            // Acknowledge ONLY after successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process ORDER_CREATED event | key={} | error={}",
                    record.key(), e.getMessage(), e);
            // Don't acknowledge → Kafka will redeliver this message
        }
    }

    // ---- Listen to ORDER UPDATED topic ----
    @KafkaListener(
            topics = "${kafka.topics.order-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrderUpdated(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment) {

        log.debug("Received ORDER_UPDATED event | key={} | partition={} | offset={}",
                record.key(), record.partition(), record.offset());

        try {
            OrderEvent event = objectMapper.readValue(record.value(), OrderEvent.class);
            notificationService.handleOrderUpdated(event);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process ORDER_UPDATED event | key={} | error={}",
                    record.key(), e.getMessage(), e);
        }
    }

    // ---- Listen to ORDER DELETED topic ----
    @KafkaListener(
            topics = "${kafka.topics.order-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrderDeleted(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment) {

        log.debug("Received ORDER_DELETED event | key={} | partition={} | offset={}",
                record.key(), record.partition(), record.offset());

        try {
            // Deleted event only contains orderId, parse it simply
            String payload = record.value();
            String orderId = objectMapper.readTree(payload)
                    .get("orderId")
                    .asText();

            notificationService.handleOrderDeleted(orderId);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process ORDER_DELETED event | key={} | error={}",
                    record.key(), e.getMessage(), e);
        }
    }
}