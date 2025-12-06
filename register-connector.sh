#!/bin/bash

# Script to register Debezium PostgreSQL connector
# This configures Debezium to monitor the users table for changes

echo "Waiting for Debezium Connect to be ready..."
until curl -s http://localhost:8083/ > /dev/null; do
    sleep 2
done

echo "Debezium Connect is ready. Registering PostgreSQL connector..."

# Register the connector
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" \
  http://localhost:8083/connectors/ -d '{
  "name": "postgres-connector",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "postgres",
    "database.port": "5432",
    "database.user": "postgres",
    "database.password": "postgres",
    "database.dbname": "testdb",
    "database.server.name": "dbserver1",
    "table.include.list": "public.users",
    "topic.prefix": "dbserver1",
    "plugin.name": "pgoutput",
    "slot.name": "debezium_slot",
    "publication.name": "dbz_publication",
    "publication.autocreate.mode": "filtered",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false",
    "transforms": "unwrap",
    "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState",
    "transforms.unwrap.drop.tombstones": "false",
    "transforms.unwrap.delete.handling.mode": "rewrite"
  }
}'

echo ""
echo "Connector registration complete!"
echo ""
echo "To check connector status, run:"
echo "curl http://localhost:8083/connectors/postgres-connector/status"
