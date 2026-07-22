-- Database: food_ordering_menu

CREATE TABLE "categories" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "description" TEXT,
    "image_url" VARCHAR(255)
);

CREATE TABLE "products" (
    "id" BIGSERIAL PRIMARY KEY,
    "name" VARCHAR(100) NOT NULL,
    "price" DECIMAL(10, 2) NOT NULL,
    "description" TEXT,
    "image_url" VARCHAR(255),
    "category_id" BIGINT,
    "available" BOOLEAN DEFAULT TRUE,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category FOREIGN KEY ("category_id") REFERENCES "categories" ("id")
);

-- Initial Data
INSERT INTO "categories" ("name", "description") VALUES 
('Món chính', 'Các món ăn chính trong bữa'),
('Đồ uống', 'Các loại nước giải khát');

INSERT INTO "products" ("name", "price", "description", "category_id") VALUES 
('Phở Bò', 50000, 'Phở bò tái chín truyền thống', 1),
('Bún Chả', 60000, 'Bún chả Hà Nội', 1),
('Trà Đá', 5000, 'Trà đá mát lạnh', 2);
