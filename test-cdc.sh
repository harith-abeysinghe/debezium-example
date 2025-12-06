#!/bin/bash

# Script to test CDC events by performing database operations

echo "================================"
echo "Testing Debezium CDC Events"
echo "================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to execute SQL
execute_sql() {
    local sql=$1
    local description=$2
    
    echo -e "${BLUE}[TEST]${NC} $description"
    echo -e "${YELLOW}SQL:${NC} $sql"
    docker exec -it postgres psql -U postgres -d testdb -c "$sql"
    echo ""
    echo "Check Spring Boot logs for CDC event processing..."
    echo ""
    sleep 2
}

echo "Current users in database:"
docker exec -it postgres psql -U postgres -d testdb -c "SELECT * FROM users;"
echo ""
echo "Press Enter to continue..."
read

# Test 1: INSERT
echo -e "${GREEN}=== Test 1: INSERT Operation ===${NC}"
execute_sql "INSERT INTO users (email, first_name, last_name) VALUES ('alice.wonder@example.com', 'Alice', 'Wonder');" \
    "Inserting new user"

echo "Press Enter to continue..."
read

# Test 2: UPDATE
echo -e "${GREEN}=== Test 2: UPDATE Operation ===${NC}"
execute_sql "UPDATE users SET first_name = 'Alicia' WHERE email = 'alice.wonder@example.com';" \
    "Updating user's first name"

echo "Press Enter to continue..."
read

# Test 3: Another UPDATE
echo -e "${GREEN}=== Test 3: Multiple Fields UPDATE ===${NC}"
execute_sql "UPDATE users SET first_name = 'Alice', last_name = 'Wonderland' WHERE email = 'alice.wonder@example.com';" \
    "Updating multiple fields"

echo "Press Enter to continue..."
read

# Test 4: DELETE
echo -e "${GREEN}=== Test 4: DELETE Operation ===${NC}"
execute_sql "DELETE FROM users WHERE email = 'alice.wonder@example.com';" \
    "Deleting user"

echo "Press Enter to continue..."
read

# Test 5: Batch INSERT
echo -e "${GREEN}=== Test 5: Batch INSERT Operations ===${NC}"
BATCH_INSERT_SQL="INSERT INTO users (email, first_name, last_name) VALUES \
('bob.builder@example.com', 'Bob', 'Builder'), \
('charlie.chocolate@example.com', 'Charlie', 'Chocolate'), \
('diana.prince@example.com', 'Diana', 'Prince');"
execute_sql "$BATCH_INSERT_SQL" "Inserting multiple users"

echo "Press Enter to continue..."
read

# Test 6: Batch UPDATE
echo -e "${GREEN}=== Test 6: Batch UPDATE Operations ===${NC}"
execute_sql "UPDATE users SET last_name = 'Updated' WHERE email LIKE '%@example.com' AND id > 3;" \
    "Updating multiple users"

echo "Press Enter to continue..."
read

# Test 7: Batch DELETE
echo -e "${GREEN}=== Test 7: Batch DELETE Operations ===${NC}"
execute_sql "DELETE FROM users WHERE last_name = 'Updated';" \
    "Deleting multiple users"

echo ""
echo -e "${GREEN}=== Testing Complete! ===${NC}"
echo ""
echo "Final state of users table:"
docker exec -it postgres psql -U postgres -d testdb -c "SELECT * FROM users;"
echo ""
echo "Check your Spring Boot application logs to see all CDC events!"
echo ""
