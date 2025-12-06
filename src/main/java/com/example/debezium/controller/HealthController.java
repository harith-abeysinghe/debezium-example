package com.example.debezium.controller;

import com.example.debezium.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller providing health check and status endpoints
 */
@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class HealthController {

    private final UserRepository userRepository;

    @Value("${spring.application.name:debezium-example}")
    private String applicationName;

    @Value("${kafka.topic.users}")
    private String kafkaTopic;

    /**
     * Health check endpoint
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        log.info("Health check requested");
        
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setApplication(applicationName);
        response.setTimestamp(Instant.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Status endpoint providing application configuration
     * 
     * @return Application status and configuration
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        log.info("Status check requested");
        
        Map<String, Object> status = new HashMap<>();
        status.put("application", applicationName);
        status.put("timestamp", Instant.now());
        status.put("kafkaTopic", kafkaTopic);
        status.put("description", "Debezium CDC Consumer - Listening for PostgreSQL user table changes");
        status.put("userCount", userRepository.count());
        
        return ResponseEntity.ok(status);
    }

    @Data
    static class HealthResponse {
        private String status;
        private String application;
        private Instant timestamp;
    }
}
