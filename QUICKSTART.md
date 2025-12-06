# Quick Start Guide

Follow these steps to get the Debezium CDC example running in minutes.

## Prerequisites

- Docker and Docker Compose installed
- Java 21 installed (for running Spring Boot locally)

## Step-by-Step Guide

### 1. Start Infrastructure (2-3 minutes)

```bash
# Start all services
docker-compose up -d

# Wait for services to be healthy
echo "Waiting for services to start..."
sleep 30

# Check service health
docker-compose ps
```

All services should show "healthy" status.

### 2. Register Debezium Connector (30 seconds)

```bash
# Register the PostgreSQL connector
./register-connector.sh

# Verify connector is running
curl http://localhost:8083/connectors/postgres-connector/status
```

Expected output: `"state": "RUNNING"`

### 3. Build Spring Boot Application (1 minute)

```bash
# Build the application
./gradlew build
```

### 4. Run Spring Boot Application

```bash
# Run in terminal (you'll see CDC events in logs)
./gradlew bootRun
```

### 5. Test CDC Events

Open a new terminal and run these commands:

#### Test INSERT
```bash
docker exec -it postgres psql -U postgres -d testdb -c \
  "INSERT INTO users (email, first_name, last_name) VALUES ('test@example.com', 'Test', 'User');"
```

Check Spring Boot logs - you should see:
```
INFO c.e.d.consumer.UserCdcConsumer : INSERT operation detected
INFO c.e.d.consumer.UserCdcConsumer : New record: {id=4, email=test@example.com, ...}
```

#### Test UPDATE
```bash
docker exec -it postgres psql -U postgres -d testdb -c \
  "UPDATE users SET first_name = 'Updated' WHERE email = 'test@example.com';"
```

Check logs for UPDATE event.

#### Test DELETE
```bash
docker exec -it postgres psql -U postgres -d testdb -c \
  "DELETE FROM users WHERE email = 'test@example.com';"
```

Check logs for DELETE event.

### 6. Test REST Endpoints

```bash
# Health check
curl http://localhost:8080/api/health

# Application status
curl http://localhost:8080/api/status
```

## Troubleshooting

### Services won't start
```bash
# Check logs
docker-compose logs postgres
docker-compose logs kafka
docker-compose logs debezium
```

### Connector registration fails
```bash
# Wait longer for services to be ready
sleep 60

# Try registering again
./register-connector.sh
```

### No CDC events received
```bash
# Check if connector is running
curl http://localhost:8083/connectors/postgres-connector/status

# Check Kafka topic
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

# Consume Kafka messages directly
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic dbserver1.public.users \
  --from-beginning
```

## Cleanup

```bash
# Stop services (keeps data)
docker-compose down

# Stop and remove all data
docker-compose down -v
```

## What's Happening?

1. **PostgreSQL**: Your database with a `users` table
2. **Debezium**: Monitors PostgreSQL's Write-Ahead Log (WAL)
3. **Kafka**: Receives CDC events from Debezium
4. **Spring Boot**: Consumes and processes CDC events

```
INSERT/UPDATE/DELETE → PostgreSQL → Debezium → Kafka → Spring Boot → Your Business Logic
```

## Next Steps

- Modify `UserCdcConsumer.java` to add your business logic
- Add more tables to monitor
- Implement data synchronization
- Add event processing workflows
- Scale with multiple consumers

Enjoy real-time Change Data Capture! 🚀
