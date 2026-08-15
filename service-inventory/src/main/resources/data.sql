-- =====================================================================
-- BachHome · service-inventory · seed data (auto-loaded by Spring Boot)
-- Kho gồm 2 nhóm:
--   (1) 6 mặt hàng đồ gia dụng đang bán -> có product_id, được trừ/hoàn tự động theo đơn hàng
--   (2) 6 vật tư đóng gói               -> product_id NULL, chỉ theo dõi vật tư trong kho
-- PLAIN SQL only (no procedural blocks / dollar-quoting) so Spring Boot ScriptUtils
-- splits it correctly on ";". Idempotent: mỗi khối có WHERE NOT EXISTS riêng.
-- =====================================================================

-- (1) Tồn kho của các sản phẩm đang bán.
-- product_id trỏ tới sản phẩm bên service-product: khi đơn hàng được xác nhận,
-- service-order gọi /inventory/deduct để trừ kho; khi hủy đơn thì gọi /inventory/restore.
INSERT INTO inventory_items (name, product_id, unit, quantity, min_quantity, cost_per_unit, description, active, created_at, updated_at)
SELECT v.name, v.product_id, v.unit, v.quantity, v.min_quantity, v.cost_per_unit, v.description, v.active, v.created_at, v.updated_at
FROM (VALUES
    ('Nồi cơm điện Cuckoo 1.8L',   1::bigint, 'cái',  3.00,  5.00, 1500000.00, 'Tồn kho nồi cơm điện Cuckoo 1.8L.',   true, NOW(), NOW()),
    ('Bộ chảo chống dính 3 món',   2,         'bộ',  12.00,  5.00,  600000.00, 'Tồn kho bộ chảo chống dính 3 món.',   true, NOW(), NOW()),
    ('Bộ dao nhà bếp 6 món',       3,         'bộ',  20.00, 10.00,  400000.00, 'Tồn kho bộ dao nhà bếp 6 món.',       true, NOW(), NOW()),
    ('Ấm siêu tốc Sunhouse 1.8L',  5,         'cái',  2.00,  8.00,  300000.00, 'Tồn kho ấm siêu tốc Sunhouse 1.8L.',  true, NOW(), NOW()),
    ('Quạt điện đứng Panasonic',   6,         'cái',  4.00,  6.00,  700000.00, 'Tồn kho quạt điện đứng Panasonic.',   true, NOW(), NOW()),
    ('Máy hút bụi cầm tay',        7,         'cái',  6.00,  4.00, 2000000.00, 'Tồn kho máy hút bụi cầm tay.',        true, NOW(), NOW())
) AS v(name, product_id, unit, quantity, min_quantity, cost_per_unit, description, active, created_at, updated_at)
WHERE NOT EXISTS (SELECT 1 FROM inventory_items WHERE product_id IS NOT NULL);

-- (2) Vật tư đóng gói phục vụ giao hàng (không gắn sản phẩm bán ra)
INSERT INTO inventory_items (name, product_id, unit, quantity, min_quantity, cost_per_unit, description, active, created_at, updated_at)
SELECT v.name, v.product_id, v.unit, v.quantity, v.min_quantity, v.cost_per_unit, v.description, v.active, v.created_at, v.updated_at
FROM (VALUES
    ('Thùng carton đóng gói',              NULL::bigint, 'cái', 500.00,  80.00,  8000.00,  'Thùng carton 3 lớp dùng để đóng gói đơn hàng.',    true, NOW(), NOW()),
    ('Băng keo đóng gói',                  NULL,         'cuộn', 300.00, 50.00,  12000.00, 'Băng keo trong khổ lớn niêm phong thùng hàng.',    true, NOW(), NOW()),
    ('Màng bọc chống sốc (bong bóng khí)', NULL,         'mét', 1200.00, 200.00, 3000.00,  'Màng xốp hơi bảo vệ hàng dễ vỡ khi vận chuyển.',   true, NOW(), NOW()),
    ('Túi nilon gói hàng',                 NULL,         'kg',  150.00,  30.00,  25000.00, 'Túi nilon các cỡ để bọc và phân loại sản phẩm.',   true, NOW(), NOW()),
    ('Xốp chèn hàng',                      NULL,         'kg',  80.00,   20.00,  40000.00, 'Hạt xốp / mút chèn lấp khoảng trống trong thùng.', true, NOW(), NOW()),
    ('Nhãn dán vận chuyển',                NULL,         'tờ',  2000.00, 300.00, 500.00,   'Nhãn in mã đơn và địa chỉ giao hàng.',             true, NOW(), NOW())
) AS v(name, product_id, unit, quantity, min_quantity, cost_per_unit, description, active, created_at, updated_at)
WHERE NOT EXISTS (SELECT 1 FROM inventory_items WHERE product_id IS NULL);

-- Keep the identity sequence in sync with the highest inserted id.
SELECT setval(pg_get_serial_sequence('inventory_items', 'id'), COALESCE((SELECT MAX(id) FROM inventory_items), 1), true);
