# Architecture Documentation

## System Overview

This document explains the architecture and data flow of the Debezium CDC system.

## High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                         APPLICATION LAYER                         │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │           Spring Boot Application (Port 8080)           │    │
│  │                                                         │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │    │
│  │  │ REST API     │  │ Kafka        │  │ Business     │ │    │
│  │  │ Controllers  │  │ Consumer     │  │ Logic        │ │    │
│  │  └──────────────┘  └──────────────┘  └──────────────┘ │    │
│  └─────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
                                ▲
                                │ Kafka Consumer
                                │ (Subscribe)
┌───────────────────────────────┴───────────────────────────────────┐
│                      MESSAGING LAYER                              │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │           Apache Kafka (KRaft Mode, Port 9092)          │    │
│  │                                                         │    │
│  │  Topic: dbserver1.public.users                         │    │
│  │  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐       │    │
│  │  │ Event1 │  │ Event2 │  │ Event3 │  │ Event4 │  ...  │    │
│  │  └────────┘  └────────┘  └────────┘  └────────┘       │    │
│  └─────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
                                ▲
                                │ Kafka Producer
                                │ (Publish CDC Events)
┌───────────────────────────────┴───────────────────────────────────┐
│                       CDC CAPTURE LAYER                           │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │         Debezium Connect (Port 8083)                    │    │
│  │                                                         │    │
│  │  ┌────────────────────────────────────────────────┐    │    │
│  │  │  PostgreSQL Connector                          │    │    │
│  │  │  - Reads WAL (Write-Ahead Log)                 │    │    │
│  │  │  - Converts to CDC events                      │    │    │
│  │  │  - Publishes to Kafka topics                   │    │    │
│  │  └────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
                                ▲
                                │ Logical Replication
                                │ (Stream WAL)
┌───────────────────────────────┴───────────────────────────────────┐
│                        DATA LAYER                                 │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │         PostgreSQL Database (Port 5432)                 │    │
│  │                                                         │    │
│  │  Database: testdb                                      │    │
│  │  ┌─────────────────────────────────────────┐           │    │
│  │  │  users table                            │           │    │
│  │  │  ┌────┬───────┬────────────┬──────────┐ │           │    │
│  │  │  │ id │ email │ first_name │ ...      │ │           │    │
│  │  │  └────┴───────┴────────────┴──────────┘ │           │    │
│  │  └─────────────────────────────────────────┘           │    │
│  │                                                         │    │
│  │  WAL (Write-Ahead Log) - wal_level=logical             │    │
│  └─────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
```

## Data Flow

### 1. Database Operation Flow

```
User/Application
      │
      ▼
┌─────────────┐
│   SQL DML   │  INSERT / UPDATE / DELETE
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│  PostgreSQL Engine  │
│  - Execute query    │
│  - Update table     │
│  - Write to WAL     │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Write-Ahead Log    │
│  (wal_level=logical)│
└─────────────────────┘
```

### 2. CDC Capture Flow

```
┌─────────────────────┐
│  Write-Ahead Log    │
└──────┬──────────────┘
       │ Stream changes
       ▼
┌─────────────────────┐
│ Debezium Connector  │
│ - Read WAL          │
│ - Parse changes     │
│ - Create events     │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  CDC Event Format   │
│  {                  │
│    before: {...},   │
│    after: {...},    │
│    op: "c/u/d",     │
│    source: {...}    │
│  }                  │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│   Kafka Topic       │
│ dbserver1.public.   │
│      users          │
└─────────────────────┘
```

### 3. Event Processing Flow

```
┌─────────────────────┐
│   Kafka Topic       │
└──────┬──────────────┘
       │ Subscribe
       ▼
┌─────────────────────┐
│  Spring Kafka       │
│  @KafkaListener     │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  UserCdcConsumer    │
│  - Parse event      │
│  - Log operation    │
│  - Route to handler │
└──────┬──────────────┘
       │
       ▼
┌──────────────────────────────────┐
│  Operation Handlers              │
│  ┌────────────┬────────────────┐ │
│  │ handleInsert│ handleUpdate  │ │
│  └────────────┴────────────────┘ │
│  ┌────────────┐                  │
│  │handleDelete│                  │
│  └────────────┘                  │
└──────────────────────────────────┘
       │
       ▼
┌─────────────────────┐
│  Your Business      │
│  Logic              │
│  - Notifications    │
│  - Cache updates    │
│  - Data sync        │
│  - Workflows        │
└─────────────────────┘
```

## CDC Event Structure

### INSERT Operation (op: "c")
```json
{
  "before": null,
  "after": {
    "id": 1,
    "email": "user@example.com",
    "first_name": "John",
    "last_name": "Doe",
    "created_at": 1702123456000,
    "updated_at": 1702123456000
  },
  "source": {
    "db": "testdb",
    "table": "users",
    "ts_ms": 1702123456000
  },
  "op": "c",
  "ts_ms": 1702123456789
}
```

### UPDATE Operation (op: "u")
```json
{
  "before": {
    "id": 1,
    "email": "user@example.com",
    "first_name": "John",
    "last_name": "Doe",
    "created_at": 1702123456000,
    "updated_at": 1702123456000
  },
  "after": {
    "id": 1,
    "email": "user@example.com",
    "first_name": "Jane",
    "last_name": "Doe",
    "created_at": 1702123456000,
    "updated_at": 1702123999000
  },
  "source": {
    "db": "testdb",
    "table": "users",
    "ts_ms": 1702123999000
  },
  "op": "u",
  "ts_ms": 1702123999789
}
```

### DELETE Operation (op: "d")
```json
{
  "before": {
    "id": 1,
    "email": "user@example.com",
    "first_name": "Jane",
    "last_name": "Doe",
    "created_at": 1702123456000,
    "updated_at": 1702123999000
  },
  "after": null,
  "source": {
    "db": "testdb",
    "table": "users",
    "ts_ms": 1702124000000
  },
  "op": "d",
  "ts_ms": 1702124000789
}
```

## Component Details

### PostgreSQL Configuration
- **wal_level=logical**: Enables logical replication for CDC
- **Replication slot**: `debezium_slot` - tracks CDC position
- **Publication**: `dbz_publication` - defines replicated tables

### Debezium Configuration
- **Connector**: PostgreSQL connector
- **Plugin**: `pgoutput` (native PostgreSQL logical decoding)
- **Transform**: `ExtractNewRecordState` - unwraps Debezium envelope
- **Topic naming**: `{server.name}.{schema}.{table}`

### Kafka Configuration
- **Mode**: KRaft
- **Replication**: Single node
- **Consumer group**: `debezium-consumer-group`

### Spring Boot Configuration
- **Framework**: Spring Boot 3.5.8
- **Java**: 21
- **Kafka**: Spring Kafka with @KafkaListener
- **Lombok**: Reduces boilerplate code

## Scaling Considerations

### Horizontal Scaling
```
┌─────────────┐
│ Kafka Topic │
└──────┬──────┘
       │
       ├────────────────────────────────┐
       │                                │
       ▼                                ▼
┌──────────────┐               ┌──────────────┐
│ Consumer 1   │               │ Consumer 2   │
│ Partition 0,2│               │ Partition 1,3│
└──────────────┘               └──────────────┘
```

### High Availability
```
┌──────────────┐     ┌──────────────┐
│ PostgreSQL   │────▶│ PostgreSQL   │
│ Primary      │     │ Replica      │
└──────┬───────┘     └──────────────┘
       │
       ▼
┌──────────────────────────────────┐
│ Debezium Connect Cluster         │
│  ┌──────┐  ┌──────┐  ┌──────┐   │
│  │Node 1│  │Node 2│  │Node 3│   │
│  └──────┘  └──────┘  └──────┘   │
└──────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────┐
│ Kafka Cluster                    │
│  ┌──────┐  ┌──────┐  ┌──────┐   │
│  │Broker│  │Broker│  │Broker│   │
│  │  1   │  │  2   │  │  3   │   │
│  └──────┘  └──────┘  └──────┘   │
└──────────────────────────────────┘
```

## Monitoring Points

1. **Database Level**
   - WAL size and growth
   - Replication slot lag
   - Connection count

2. **Debezium Level**
   - Connector status
   - Lag behind database
   - Error counts

3. **Kafka Level**
   - Topic partition count
   - Consumer lag
   - Message throughput

4. **Application Level**
   - Processing time
   - Error rates
   - Business metrics

## Troubleshooting

### Common Issues

1. **No events received**
   - Check WAL level is logical
   - Verify connector is running
   - Check Kafka topic exists

2. **High lag**
   - Scale consumers
   - Optimize processing logic
   - Check network bandwidth

3. **Missing events**
   - Check replication slot status
   - Verify no transaction rollbacks
   - Check Kafka retention settings

4. **Duplicate events**
   - Ensure idempotent processing
   - Check consumer offset management
   - Verify exactly-once semantics
