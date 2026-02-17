-- Create database
CREATE DATABASE IF NOT EXISTS digital_approval_db;
USE digital_approval_db;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'APPROVER', 'ADMIN') NOT NULL
);

-- Requests table
CREATE TABLE requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    category VARCHAR(50) NOT NULL,
    requested_date DATE NOT NULL DEFAULT CURRENT_DATE,
    attachment VARCHAR(255),
    attachment_original_name VARCHAR(255),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    user_id BIGINT NOT NULL,
    approver_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (approver_id) REFERENCES users(id)
);

-- Approvals table (for audit log)
CREATE TABLE approvals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL,
    remarks VARCHAR(500),
    approved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES requests(id),
    FOREIGN KEY (approver_id) REFERENCES users(id)
);

-- Audit log table
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT,
    action VARCHAR(100) NOT NULL,
    details VARCHAR(500),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Insert sample data
INSERT INTO users (username, email, password, role) VALUES
('admin', 'admin@example.com', '$2a$10$exampleHashedPassword', 'ADMIN'),
('approver1', 'approver1@example.com', '$2a$10$exampleHashedPassword', 'APPROVER'),
('user1', 'user1@example.com', '$2a$10$exampleHashedPassword', 'USER');