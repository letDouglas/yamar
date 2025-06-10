package com.yamar.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${application.config.kafka.topics.orders}")
    private String ordersTopicName;

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(ordersTopicName)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }
}