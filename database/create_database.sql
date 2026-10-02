-- ============================================================
-- FARMORA DATABASE SETUP SCRIPT
-- ============================================================
-- Execute this script manually inside MySQL Workbench.
-- Spring Boot will NOT create the database.
-- Spring Data JPA / Hibernate will automatically create and update
-- application tables inside farmora_db when Spring Boot connects
-- using spring.jpa.hibernate.ddl-auto=update.
-- ============================================================

CREATE DATABASE IF NOT EXISTS farmora_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE farmora_db;

-- ============================================================
-- DEFAULT TEST USERS (Pass through MySQL Workbench)
-- BCrypt hashed passwords:
-- Admin@123   -> $2a$10$NLoO12MGaF0C/zMl0Q3oReG8.HkeVs7chNZ7Kzdsb0BmhBDFuB1Ia
-- Farmer@123  -> $2a$10$XnuzDCrPAuDMTdqet3HkDuter4HJOIj5xvvGn6.tB.VXP5u9W5xDG
-- Buyer@123   -> $2a$10$r0TR1EYOeZckeQD793/sXeMpu5KSafPgcMIM.OQjsUuej9pWDHy6u
-- Delivery@123-> $2a$10$xn0xisFIXFulXCuChpOBveIKyAdVRrTYRMSxPBKCRKKCm7QHkqLOu
-- ============================================================

INSERT INTO users (full_name, email, phone, password, role, address, active, created_at, updated_at)
VALUES 
('System Admin', 'admin@farmora.com', '+94770000001', '$2a$10$NLoO12MGaF0C/zMl0Q3oReG8.HkeVs7chNZ7Kzdsb0BmhBDFuB1Ia', 'ADMIN', 'Colombo, Western Province', 1, NOW(), NOW()),
('Kamal Perera (Farmer)', 'farmer@farmora.com', '+94770000002', '$2a$10$XnuzDCrPAuDMTdqet3HkDuter4HJOIj5xvvGn6.tB.VXP5u9W5xDG', 'FARMER', 'Nuwara Eliya, Central Province', 1, NOW(), NOW()),
('Nimal Silva (Buyer)', 'buyer@farmora.com', '+94770000003', '$2a$10$r0TR1EYOeZckeQD793/sXeMpu5KSafPgcMIM.OQjsUuej9pWDHy6u', 'BUYER', 'Colombo 03, Western Province', 1, NOW(), NOW()),
('Sunil Bandara (Delivery)', 'delivery@farmora.com', '+94770000004', '$2a$10$xn0xisFIXFulXCuChpOBveIKyAdVRrTYRMSxPBKCRKKCm7QHkqLOu', 'DELIVERY_PARTNER', 'Kandy, Central Province', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE password = VALUES(password), active = VALUES(active), updated_at = NOW();


