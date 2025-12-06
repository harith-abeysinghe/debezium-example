package com.example.debezium;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Spring Boot Application for Debezium CDC Example
 * 
 * Flow Architecture:
 * PostgreSQL → Debezium Connector → Kafka Topics → Spring Boot Consumer → Application Logic
 * 
 * Components:
 * 1. PostgreSQL: Source database with users table
 * 2. Debezium: CDC connector that captures database changes
 * 3. Kafka: Message broker for streaming CDC events
 * 4. Spring Boot: Consumer application that processes CDC events
 */
@SpringBootApplication
@EnableJpaAuditing
public class DebeziumExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(DebeziumExampleApplication.class, args);
    }

}
