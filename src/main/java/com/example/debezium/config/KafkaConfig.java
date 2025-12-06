package com.example.debezium.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Kafka configuration for CDC event processing
 * 
 * Spring Boot auto-configures Kafka based on application.yml properties
 * This class provides additional beans needed for CDC event processing
 */
@Configuration
public class KafkaConfig {

    /**
     * ObjectMapper bean for JSON serialization/deserialization
     * Configured with JavaTimeModule for Java 8+ date/time support
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
