# Debezium CDC Example with Spring Boot 3.5 + Java 21

A complete example demonstrating Change Data Capture (CDC) using Debezium, Kafka, PostgreSQL, and Spring Boot.

## 📋 Overview

This project demonstrates a real-time data streaming architecture using:
- **Spring Boot 3.5** with Java 21
- **Debezium** for Change Data Capture (CDC)
- **Apache Kafka** (KRaft mode - no Zookeeper)
- **PostgreSQL** as the source database
- **Lombok** for clean, concise code
- **Docker Compose** for easy deployment

## 🏗️ Architecture

```
┌─────────────────┐
│   PostgreSQL    │
│   (Source DB)   │
│                 │
│  - users table  │
└────────┬────────┘
         │ WAL (Write-Ahead Log)
         │ wal_level=logical
         ↓
┌─────────────────┐
│    Debezium     │
│   Connector     │
│                 │
│  - Captures CDC │
│  - Parses WAL   │
└────────┬────────┘
         │ CDC Events
         │ (JSON)
         ↓
┌─────────────────┐
│      Kafka      │
│   (KRaft Mode)  │
│                 │
│  Topic:         │
│  dbserver1.     │
│  public.users   │
└────────┬────────┘
         │ Consumer
         │ subscribes
         ↓
┌─────────────────┐
│  Spring Boot    │
│   Application   │
│                 │
│  - Consumes     │
│  - Processes    │
│  - Logs events  │
└─────────────────┘
```

## 🔄 CDC Event Flow

### 1. Database Changes
When data changes occur in PostgreSQL:
```sql
INSERT INTO users (email, first_name, last_name) 
VALUES ('test@example.com', 'Test', 'User');

UPDATE users SET first_name = 'Updated' WHERE id = 1;

DELETE FROM users WHERE id = 1;
```

### 2. Debezium Captures Changes
Debezium monitors the PostgreSQL Write-Ahead Log (WAL) and captures:
- **INSERT** operations (`op: 'c'` - create)
- **UPDATE** operations (`op: 'u'` - update)
- **DELETE** operations (`op: 'd'` - delete)

### 3. Kafka Event Format
Events published to Kafka topic `dbserver1.public.users`:
```json
{
  "before": null,
  "after": {
    "id": 1,
    "email": "test@example.com",
    "first_name": "Test",
    "last_name": "User",
    "created_at": 1234567890000,
    "updated_at": 1234567890000
  },
  "source": {
    "db": "testdb",
    "table": "users",
    "ts_ms": 1234567890000
  },
  "op": "c",
  "ts_ms": 1234567890000
}
```

### 4. Spring Boot Processing
The Kafka consumer processes events:
```java
@KafkaListener(topics = "${kafka.topic.users}")
public void consumeUserEvent(String message) {
    // Parse and handle INSERT/UPDATE/DELETE
}
```

## 🚀 Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 21 (for local development)
- Gradle (or use included wrapper)

### Step 1: Start Infrastructure
```bash
# Start PostgreSQL, Kafka, and Debezium
docker-compose up -d

# Wait for all services to be healthy (30-60 seconds)
docker-compose ps
```

### Step 2: Register Debezium Connector
```bash
# Register the PostgreSQL connector
./register-connector.sh

# Verify connector status
curl http://localhost:8083/connectors/postgres-connector/status
```

### Step 3: Build and Run Spring Boot Application
```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun
```

### Step 4: Test the Setup

#### Check Application Health
```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/status
```

#### Insert Data (triggers CDC event)
```bash
docker exec -it postgres psql -U postgres -d testdb -c \
  "INSERT INTO users (email, first_name, last_name) VALUES ('new.user@example.com', 'New', 'User');"
```

#### Update Data (triggers CDC event)
```bash
docker exec -it postgres psql -U postgres -d testdb -c \
  "UPDATE users SET first_name = 'Updated' WHERE email = 'new.user@example.com';"
```

#### Delete Data (triggers CDC event)
```bash
docker exec -it postgres psql -U postgres -d testdb -c \
  "DELETE FROM users WHERE email = 'new.user@example.com';"
```

Check the Spring Boot application logs to see CDC events being processed!

## 📁 Project Structure

```
debezium-example/
├── src/
│   └── main/
│       ├── java/com/example/debezium/
│       │   ├── DebeziumExampleApplication.java   # Main application
│       │   ├── config/
│       │   │   └── KafkaConfig.java              # Kafka configuration
│       │   ├── model/
│       │   │   ├── User.java                     # User entity (Lombok)
│       │   │   └── CdcEvent.java                 # CDC event model
│       │   ├── consumer/
│       │   │   └── UserCdcConsumer.java          # Kafka consumer
│       │   └── controller/
│       │       └── HealthController.java         # REST endpoints
│       └── resources/
│           └── application.yml                   # Application config
├── docker-compose.yml                            # Docker services
├── init.sql                                      # Database schema
├── register-connector.sh                         # Debezium setup
├── build.gradle                                  # Gradle build
└── README.md                                     # This file
```

## 🔧 Configuration

### application.yml
Key configurations:
- **Database**: PostgreSQL connection settings
- **Kafka**: Bootstrap servers and consumer settings
- **Topic**: `dbserver1.public.users` (format: `{server.name}.{schema}.{table}`)

### Docker Compose Services
1. **PostgreSQL** (port 5432)
   - WAL level set to `logical` for CDC
   - Initialized with `init.sql`

2. **Kafka** (port 9092)
   - KRaft mode (no Zookeeper)
   - Single node setup

3. **Debezium Connect** (port 8083)
   - PostgreSQL connector
   - REST API for management

## 📝 Lombok Usage

This project extensively uses Lombok to reduce boilerplate:

```java
@Data                    // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor       // Generates no-args constructor
@AllArgsConstructor      // Generates all-args constructor
@Builder                 // Generates builder pattern
@Slf4j                   // Generates logger field
@RequiredArgsConstructor // Generates constructor for final fields
```

## 🧪 Monitoring and Debugging

### View Kafka Topics
```bash
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### View Kafka Messages
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic dbserver1.public.users \
  --from-beginning
```

### Check Debezium Connector Status
```bash
curl http://localhost:8083/connectors/postgres-connector/status | jq
```

### View PostgreSQL Logs
```bash
docker logs postgres -f
```

### View Spring Boot Logs
The application logs CDC events with operation details:
```
INFO  c.e.d.consumer.UserCdcConsumer : Received CDC event: {...}
INFO  c.e.d.consumer.UserCdcConsumer : INSERT operation detected
INFO  c.e.d.consumer.UserCdcConsumer : New record: {id=1, email=test@example.com, ...}
```

## 🛠️ Troubleshooting

### Connector Not Registering
```bash
# Check Debezium Connect logs
docker logs debezium -f

# Ensure PostgreSQL and Kafka are healthy
docker-compose ps
```

### No CDC Events Received
1. Verify connector is running:
   ```bash
   curl http://localhost:8083/connectors/postgres-connector/status
   ```

2. Check Kafka topic exists:
   ```bash
   docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
   ```

3. Verify Spring Boot is listening to correct topic in `application.yml`

### PostgreSQL Connection Issues
- Ensure `wal_level=logical` is set
- Check PostgreSQL logs: `docker logs postgres`
- Verify network connectivity: `docker network inspect debezium-example_debezium-network`

## 🧹 Cleanup

```bash
# Stop all services
docker-compose down

# Remove volumes (deletes data)
docker-compose down -v
```

## 🎯 Use Cases

This architecture is ideal for:
- **Real-time Data Synchronization**: Sync data across systems
- **Event-Driven Architecture**: Trigger actions on data changes
- **Audit Logging**: Track all database changes
- **Data Replication**: Replicate data to analytics systems
- **Cache Invalidation**: Update caches when data changes
- **Microservices Communication**: Notify services of changes
