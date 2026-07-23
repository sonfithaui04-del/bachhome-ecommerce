-- BachHome sample data (inventory service): 6 warehouse consumables / packaging items.
-- PLAIN SQL only (no procedural blocks / dollar-quoting) so Spring Boot ScriptUtils splits it correctly on ";".
-- Idempotent: the INSERT carries a WHERE NOT EXISTS guard so it only fills an empty table.

INSERT INTO ingredients (name, unit, quantity, min_quantity, cost_per_unit, description, active, created_at, updated_at)
SELECT v.name, v.unit, v.quantity, v.min_quantity, v.cost_per_unit, v.description, v.active, v.created_at, v.updated_at
FROM (VALUES
    ('Thùng carton đóng gói',              'cái', 500.00,  80.00,  8000.00,  'Thùng carton 3 lớp dùng để đóng gói đơn hàng.',    true, NOW(), NOW()),
    ('Băng keo đóng gói',                  'cuộn', 300.00, 50.00,  12000.00, 'Băng keo trong khổ lớn niêm phong thùng hàng.',    true, NOW(), NOW()),
    ('Màng bọc chống sốc (bong bóng khí)', 'mét', 1200.00, 200.00, 3000.00,  'Màng xốp hơi bảo vệ hàng dễ vỡ khi vận chuyển.',   true, NOW(), NOW()),
    ('Túi nilon gói hàng',                 'kg',  150.00,  30.00,  25000.00, 'Túi nilon các cỡ để bọc và phân loại sản phẩm.',   true, NOW(), NOW()),
    ('Xốp chèn hàng',                      'kg',  80.00,   20.00,  40000.00, 'Hạt xốp / mút chèn lấp khoảng trống trong thùng.', true, NOW(), NOW()),
    ('Nhãn dán vận chuyển',                'tờ',  2000.00, 300.00, 500.00,   'Nhãn in mã đơn và địa chỉ giao hàng.',             true, NOW(), NOW())
) AS v(name, unit, quantity, min_quantity, cost_per_unit, description, active, created_at, updated_at)
WHERE NOT EXISTS (SELECT 1 FROM ingredients);

-- Keep the identity sequence in sync with the highest inserted id.
SELECT setval(pg_get_serial_sequence('ingredients', 'id'), COALESCE((SELECT MAX(id) FROM ingredients), 1), true);
