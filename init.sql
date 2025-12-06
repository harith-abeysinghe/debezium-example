-- Database initialization script for PostgreSQL
-- This script creates the users table and sets up the database for Debezium CDC

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create an index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Insert sample data
INSERT INTO users (email, first_name, last_name) VALUES
    ('john.doe@example.com', 'John', 'Doe'),
    ('jane.smith@example.com', 'Jane', 'Smith'),
    ('bob.johnson@example.com', 'Bob', 'Johnson')
ON CONFLICT (email) DO NOTHING;

-- Grant necessary permissions for Debezium
ALTER TABLE users REPLICA IDENTITY FULL;

-- Display success message
SELECT 'Database initialization completed successfully!' AS status;
