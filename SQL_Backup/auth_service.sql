-- Database: food_ordering_auth

CREATE TABLE "users" (
    "id" BIGSERIAL PRIMARY KEY,
    "username" VARCHAR(50) NOT NULL UNIQUE,
    "password" VARCHAR(255) NOT NULL,
    "email" VARCHAR(100) NOT NULL UNIQUE,
    "full_name" VARCHAR(100),
    "phone_number" VARCHAR(20),
    "role" VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER', -- ROLE_USER, ROLE_ADMIN
    "status" VARCHAR(20) DEFAULT 'ACTIVE',
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Initial Data
INSERT INTO "users" ("username", "password", "email", "full_name", "role") VALUES
('admin', '$2a$10$NxExampleHash...', 'admin@example.com', 'Administrator', 'ADMIN'),
('nguyenvanan', '$2a$10$NxExampleHash...', 'an.nguyen@example.com', 'Nguyen Van An', 'CUSTOMER');
