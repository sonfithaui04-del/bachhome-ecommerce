-- BachHome sample data (menu service): 5 categories + 12 hero products + 100 generated products = 112 menu_items.
-- PLAIN SQL only (no procedural blocks / dollar-quoting) so Spring Boot ScriptUtils splits it correctly on ";".
-- Idempotent: every INSERT carries a WHERE NOT EXISTS guard so it only fills an empty table.

-- 1) Categories (ids auto-generated 1..5 on a fresh table). Insert only when the table is empty.
INSERT INTO categories (name, description, active, display_order, created_at, updated_at)
SELECT v.name, v.description, v.active, v.display_order, v.created_at, v.updated_at
FROM (VALUES
    ('Đồ dùng nhà bếp', 'Nồi, chảo, dao kéo và dụng cụ nấu ăn cho gia đình.', true, 1, NOW(), NOW()),
    ('Điện gia dụng',   'Thiết bị điện tiện nghi, tiết kiệm điện cho ngôi nhà.', true, 2, NOW(), NOW()),
    ('Dụng cụ vệ sinh', 'Dụng cụ lau dọn, làm sạch nhà cửa gọn gàng.', true, 3, NOW(), NOW()),
    ('Phòng tắm',       'Vật dụng và phụ kiện phòng tắm hiện đại.', true, 4, NOW(), NOW()),
    ('Phòng ngủ',       'Chăn ga gối và đồ dùng phòng ngủ êm ái.', true, 5, NOW(), NOW())
) AS v(name, description, active, display_order, created_at, updated_at)
WHERE NOT EXISTS (SELECT 1 FROM categories);

-- 2) 12 hero products with correct per-item loremflickr images. Insert only when menu_items is empty.
INSERT INTO menu_items (name, description, price, image_url, available, display_order, average_rating, total_reviews, category_id, created_at, updated_at)
SELECT v.name, v.description, v.price, v.image_url, true, v.display_order, v.rating, 0, c.id, NOW(), NOW()
FROM (VALUES
    ('Nồi cơm điện Cuckoo 1.8L',   'Nồi cơm điện cao cấp lòng nồi chống dính, giữ ấm 24h.',    1890000, 'https://loremflickr.com/500/400/rice,cooker?lock=1',     1,  4.7, 'Đồ dùng nhà bếp'),
    ('Bộ chảo chống dính 3 món',   'Bộ 3 chảo chống dính đáy từ, dùng cho mọi loại bếp.',      650000,  'https://loremflickr.com/500/400/frying,pan?lock=2',      2,  4.5, 'Đồ dùng nhà bếp'),
    ('Bộ dao nhà bếp 6 món',       'Bộ dao thép không gỉ kèm khay gỗ, sắc bén và bền đẹp.',    450000,  'https://loremflickr.com/500/400/kitchen,knife?lock=3',   3,  4.6, 'Đồ dùng nhà bếp'),
    ('Máy xay sinh tố Philips',    'Máy xay sinh tố 700W, cối thủy tinh, xay nhuyễn nhanh.',   890000,  'https://loremflickr.com/500/400/blender?lock=4',         4,  4.6, 'Điện gia dụng'),
    ('Ấm siêu tốc Sunhouse 1.8L',  'Ấm đun siêu tốc inox 304, sôi nhanh, tự ngắt an toàn.',    350000,  'https://loremflickr.com/500/400/electric,kettle?lock=5', 5,  4.4, 'Điện gia dụng'),
    ('Quạt điện đứng Panasonic',   'Quạt cây 3 tốc độ, hẹn giờ, gió êm tiết kiệm điện.',       750000,  'https://loremflickr.com/500/400/electric,fan?lock=6',    6,  4.5, 'Điện gia dụng'),
    ('Máy hút bụi cầm tay',        'Máy hút bụi không dây, lực hút mạnh, gọn nhẹ tiện dụng.',   1200000, 'https://loremflickr.com/500/400/vacuum,cleaner?lock=7',  7,  4.3, 'Dụng cụ vệ sinh'),
    ('Cây lau nhà xoay 360 độ',    'Cây lau nhà tự vắt, xoay 360 độ, lau sạch mọi góc.',       250000,  'https://loremflickr.com/500/400/mop,cleaning?lock=8',    8,  4.4, 'Dụng cụ vệ sinh'),
    ('Bộ khăn tắm cotton cao cấp', 'Bộ khăn tắm cotton mềm mại, thấm hút tốt, bền màu.',        320000,  'https://loremflickr.com/500/400/bath,towel?lock=9',      9,  4.6, 'Phòng tắm'),
    ('Kệ để đồ nhà tắm inox',      'Kệ nhà tắm inox 304 chống gỉ, nhiều tầng, dễ lắp đặt.',     480000,  'https://loremflickr.com/500/400/bathroom,shelf?lock=10', 10, 4.5, 'Phòng tắm'),
    ('Bộ chăn ga gối cotton',      'Bộ chăn ga gối cotton 100%, êm ái, thoáng mát 4 mùa.',      850000,  'https://loremflickr.com/500/400/bedding,bed?lock=11',    11, 4.7, 'Phòng ngủ'),
    ('Đèn ngủ để bàn LED',         'Đèn ngủ LED ánh sáng ấm, cảm ứng, tiết kiệm điện.',        220000,  'https://loremflickr.com/500/400/table,lamp?lock=12',     12, 4.4, 'Phòng ngủ')
) AS v(name, description, price, image_url, display_order, rating, catname)
JOIN categories c ON c.name = v.catname
WHERE NOT EXISTS (SELECT 1 FROM menu_items);

-- 3) 100 generated products (display_order 101..200) with per-type names and correct per-type images.
--    Same logic as the old procedural seed: k = (i-1)%5 picks the category template; type/tag = (i/5)%8; brand = i%12; variant = (i*3)%8.
--    Insert only when no generated rows exist yet (hero rows use display_order 1..12).
INSERT INTO menu_items (name, description, price, image_url, available, display_order, average_rating, total_reviews, category_id, created_at, updated_at)
SELECT
    (t.types)[1 + ((g.i / 5) % 8)]
      || ' ' || (ARRAY['Sunhouse','Philips','Panasonic','Lock&Lock','Elmich','Sharp','Toshiba','Kangaroo','Comet','Bluestone','Xiaomi','Electrolux'])[1 + (g.i % 12)]
      || ' ' || (t.vars)[1 + ((g.i * 3) % 8)],
    t.descr,
    t.baseprice + (g.i % 15) * t.step,
    'https://loremflickr.com/500/400/' || (t.tags)[1 + ((g.i / 5) % 8)] || '?lock=' || g.i,
    true,
    100 + g.i,
    round((3.5 + (g.i % 15) * 0.1)::numeric, 1),
    0,
    c.id,
    NOW(), NOW()
FROM generate_series(1, 100) AS g(i)
JOIN (VALUES
    (0, 'Đồ dùng nhà bếp',
        ARRAY['Nồi cơm điện','Chảo chống dính','Bộ nồi inox','Máy xay thịt','Bộ dao nhà bếp','Nồi áp suất','Ấm đun siêu tốc','Bình giữ nhiệt'],
        ARRAY['rice,cooker','frying,pan','cookware,pot','meat,grinder','kitchen,knife','pressure,cooker','electric,kettle','thermos,flask'],
        ARRAY['1.8L','2 lớp','Inox 304','Cao cấp','Mini','Deluxe','5 món','Chống dính'],
        300000, 90000, 'Đồ dùng nhà bếp chính hãng, bền đẹp cho gia đình.'),
    (1, 'Điện gia dụng',
        ARRAY['Máy xay sinh tố','Quạt điện đứng','Bàn ủi hơi nước','Lò vi sóng','Nồi chiên không dầu','Máy lọc nước','Bếp từ đôi','Máy ép trái cây'],
        ARRAY['blender','electric,fan','steam,iron','microwave','air,fryer','water,purifier','induction,cooker','juicer'],
        ARRAY['700W','3 tốc độ','Inverter','Digital','5L','2024','Smart','Eco'],
        600000, 150000, 'Điện gia dụng tiện nghi, tiết kiệm điện, bảo hành chính hãng.'),
    (2, 'Dụng cụ vệ sinh',
        ARRAY['Cây lau nhà 360','Chổi quét nhà','Máy hút bụi cầm tay','Thùng rác thông minh','Bộ cọ rửa','Giá phơi đồ','Cây lau kính','Máy xịt rửa'],
        ARRAY['mop','broom','vacuum,cleaner','trash,bin','cleaning,brush','drying,rack','squeegee','pressure,washer'],
        ARRAY['Tự vắt','Cảm ứng','Không dây','Inox','Đa năng','Gấp gọn','Pro','Mini'],
        120000, 45000, 'Dụng cụ dọn dẹp nhà cửa gọn gàng, sạch sẽ.'),
    (3, 'Phòng tắm',
        ARRAY['Bộ khăn tắm cotton','Kệ nhà tắm inox','Vòi sen tăng áp','Gương LED cảm ứng','Máy sấy tay','Thảm chống trượt','Bộ phụ kiện toilet','Hộp đựng mỹ phẩm'],
        ARRAY['bath,towel','bathroom,shelf','shower','bathroom,mirror','hand,dryer','bath,mat','toilet','cosmetics'],
        ARRAY['4 món','3 tầng','Cao cấp','Chống mờ','Tự động','Chống trượt','Set','Treo tường'],
        100000, 55000, 'Vật dụng phòng tắm hiện đại, chống gỉ, dễ lắp đặt.'),
    (4, 'Phòng ngủ',
        ARRAY['Bộ chăn ga gối','Đèn ngủ để bàn','Gối cao su non','Rèm cửa cản sáng','Tủ vải đa năng','Máy khuếch tán tinh dầu','Nệm topper','Chăn lông cừu'],
        ARRAY['bedding','table,lamp','pillow','curtain','wardrobe','diffuser','mattress','blanket'],
        ARRAY['1m8','LED','Êm ái','Cản sáng','Gấp gọn','Mini','Cao cấp','2024'],
        250000, 80000, 'Đồ dùng phòng ngủ êm ái, chất liệu cao cấp.')
) AS t(k, catname, types, tags, vars, baseprice, step, descr) ON t.k = (g.i - 1) % 5
JOIN categories c ON c.name = t.catname
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE display_order > 100);

-- Keep identity sequences in sync with the highest inserted id.
SELECT setval(pg_get_serial_sequence('menu_items', 'id'), COALESCE((SELECT MAX(id) FROM menu_items), 1), true);
SELECT setval(pg_get_serial_sequence('categories', 'id'), COALESCE((SELECT MAX(id) FROM categories), 1), true);
