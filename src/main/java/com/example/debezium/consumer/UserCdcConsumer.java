package com.example.debezium.consumer;

import com.example.debezium.model.CdcEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCdcConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "${kafka.topic.users}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeUserEvent(String message) {
        try {
            log.info("Received CDC event: {}", message);

            CdcEvent event = objectMapper.readValue(message, CdcEvent.class);

            switch (event.getOperation()) {
                case "c", "r" -> handleInsert(event);
                case "u" -> handleUpdate(event);
                case "d" -> handleDelete(event);
                default -> log.warn("Unknown operation type: {}", event.getOperation());
            }

        } catch (JsonProcessingException e) {
            log.error("Error parsing CDC event: {}", message, e);
        } catch (Exception e) {
            log.error("Error processing CDC event: {}", message, e);
        }
    }

    private void handleInsert(CdcEvent event) {
        log.info("INSERT operation detected");
        log.info("New record: {}", event.getAfter());
        
        if (event.getAfter() != null) {
            Object id = event.getAfter().get("id");
            Object email = event.getAfter().get("email");
            Object firstName = event.getAfter().get("first_name");
            Object lastName = event.getAfter().get("last_name");
            
            log.info("User ID: {}, Email: {}, Name: {} {}", 
                id != null ? id : "N/A",
                email != null ? email : "N/A",
                firstName != null ? firstName : "N/A",
                lastName != null ? lastName : "N/A"
            );
        }
    }

    private void handleUpdate(CdcEvent event) {
        log.info("UPDATE operation detected");
        log.info("Before: {}", event.getBefore());
        log.info("After: {}", event.getAfter());
        
        if (event.getBefore() != null && event.getAfter() != null) {
            Object id = event.getAfter().get("id");
            Object beforeEmail = event.getBefore().get("email");
            Object afterEmail = event.getAfter().get("email");
            
            log.info("User ID: {}, Email: {} -> {}", 
                id != null ? id : "N/A",
                beforeEmail != null ? beforeEmail : "N/A",
                afterEmail != null ? afterEmail : "N/A"
            );
        }
    }

    private void handleDelete(CdcEvent event) {
        log.info("DELETE operation detected");
        log.info("Deleted record: {}", event.getBefore());
        
        if (event.getBefore() != null) {
            Object id = event.getBefore().get("id");
            Object email = event.getBefore().get("email");
            
            log.info("Deleted User ID: {}, Email: {}", 
                id != null ? id : "N/A",
                email != null ? email : "N/A"
            );
        }
    }
}
