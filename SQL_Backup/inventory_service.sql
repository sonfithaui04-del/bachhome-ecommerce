-- Database: food_ordering_inventory

CREATE TABLE "ingredients" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL UNIQUE,
    "unit" VARCHAR(20) NOT NULL, -- kg, g, lit, qua
    "quantity" INTEGER NOT NULL DEFAULT 0,
    "min_threshold" INTEGER DEFAULT 10
);

CREATE TABLE "product_recipes" (
    "id" BIGSERIAL PRIMARY KEY,
    "product_id" BIGINT NOT NULL,
    "ingredient_id" BIGINT NOT NULL,
    "quantity_required" INTEGER NOT NULL,
    CONSTRAINT fk_ingredient FOREIGN KEY ("ingredient_id") REFERENCES "ingredients" ("id")
);

-- Initial Data
INSERT INTO "ingredients" ("name", "unit", "quantity") VALUES 
('Thịt bò', 'kg', 50),
('Bánh phở', 'kg', 30),
('Hành tây', 'kg', 10);
