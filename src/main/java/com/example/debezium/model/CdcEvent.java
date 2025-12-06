package com.example.debezium.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class CdcEvent {

    @JsonProperty("before")
    private Map<String, Object> before;

    @JsonProperty("after")
    private Map<String, Object> after;

    @JsonProperty("source")
    private Source source;

    @JsonProperty("op")
    private String operation;

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
