-- Database: food_ordering_payment

CREATE TABLE "payments" (
    "id" BIGSERIAL PRIMARY KEY,
    "order_id" BIGINT NOT NULL UNIQUE,
    "amount" DECIMAL(10, 2) NOT NULL,
    "payment_method" VARCHAR(50) NOT NULL, -- VN_PAY, SE_PAY, COD
    "status" VARCHAR(50) NOT NULL, -- PENDING, SUCCESS, FAILED
    "transaction_id" VARCHAR(100),
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Initial Data
INSERT INTO "payments" ("order_id", "amount", "payment_method", "status", "transaction_id") VALUES
(1, 110000, 'COD', 'SUCCESS', NULL);
