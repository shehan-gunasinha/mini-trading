package com.trading.orderservice.kafka.config;

import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;


@Configuration
public class KafkaTopicConfig {
    @Value("${kafka.topics.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topics.order-updated}")
    private String orderUpdatedTopic;

    @Value("${kafka.topics.order-deleted}")
    private String orderDeletedTopic;

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(orderCreatedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic orderUpdatedTopic() {
        return TopicBuilder.name(orderUpdatedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic orderDeletedTopic() {
        return TopicBuilder.name(orderDeletedTopic).partitions(3).replicas(1).build();
    }
}

