package com.example.debezium.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Model representing Debezium CDC event structure
 * Debezium sends events in a specific format with before/after snapshots
 */
@Data
@NoArgsConstructor
public class CdcEvent {

    /**
     * The before state of the record (null for INSERT operations)
     */
    @JsonProperty("before")
    private Map<String, Object> before;

    /**
     * The after state of the record (null for DELETE operations)
     */
    @JsonProperty("after")
    private Map<String, Object> after;

    /**
     * The source metadata including database, table, timestamp, etc.
     */
    @JsonProperty("source")
    private Source source;

    /**
     * The operation type: 'c' (create), 'u' (update), 'd' (delete), 'r' (read/snapshot)
     */
    @JsonProperty("op")
    private String operation;

    /**
     * Timestamp when the event was created
     */
    @JsonProperty("ts_ms")
    private Long timestamp;

    @Data
    @NoArgsConstructor
    public static class Source {
        @JsonProperty("db")
        private String database;

        @JsonProperty("table")
        private String table;

        @JsonProperty("ts_ms")
        private Long timestamp;
    }
}
