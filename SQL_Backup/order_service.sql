-- Database: food_ordering_order

CREATE TABLE "orders" (
    "id" BIGSERIAL PRIMARY KEY,
    "user_id" BIGINT NOT NULL,
    "total_amount" DECIMAL(10, 2) NOT NULL,
    "status" VARCHAR(50) NOT NULL, -- PENDING, CONFIRMED, PREPARING, SHIPPING, COMPLETED, CANCELLED
    "payment_method" VARCHAR(50),
    "delivery_address" TEXT,
    "note" TEXT,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "order_items" (
    "id" BIGSERIAL PRIMARY KEY,
    "order_id" BIGINT NOT NULL,
    "product_id" BIGINT NOT NULL,
    "product_name" VARCHAR(100), -- Snapshot of name
    "quantity" INTEGER NOT NULL,
    "price" DECIMAL(10, 2) NOT NULL, -- Snapshot of price
    CONSTRAINT fk_order FOREIGN KEY ("order_id") REFERENCES "orders" ("id")
);

-- Initial Data
INSERT INTO "orders" ("user_id", "total_amount", "status", "payment_method", "delivery_address") VALUES
(2, 110000, 'COMPLETED', 'COD', '123 Le Loi');

INSERT INTO "order_items" ("order_id", "product_id", "quantity", "price") VALUES
(1, 1, 1, 50000),
(1, 2, 1, 60000);
