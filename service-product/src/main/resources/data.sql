-- BachHome sample data (product service): 5 categories + 12 hero products + 100 generated products = 112 products.
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

-- 2) 12 hero products with correct per-item loremflickr images. Insert only when products is empty.
INSERT INTO products (name, description, price, image_url, available, display_order, average_rating, total_reviews, category_id, created_at, updated_at)
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
WHERE NOT EXISTS (SELECT 1 FROM products);

-- 3) 100 generated products (display_order 101..200) with per-type names and correct per-type images.
--    Same logic as the old procedural seed: k = (i-1)%5 picks the category template; type/tag = (i/5)%8; brand = i%12; variant = (i*3)%8.
--    Insert only when no generated rows exist yet (hero rows use display_order 1..12).
INSERT INTO products (name, description, price, image_url, available, display_order, average_rating, total_reviews, category_id, created_at, updated_at)
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
WHERE NOT EXISTS (SELECT 1 FROM products WHERE display_order > 100);

-- Keep identity sequences in sync with the highest inserted id.
SELECT setval(pg_get_serial_sequence('products', 'id'), COALESCE((SELECT MAX(id) FROM products), 1), true);
SELECT setval(pg_get_serial_sequence('categories', 'id'), COALESCE((SELECT MAX(id) FROM categories), 1), true);

-- =====================================================================
-- 4) Thông số sản phẩm (thương hiệu, xuất xứ, bảo hành, chất liệu, thông số kỹ thuật).
--    Dùng UPDATE thay vì INSERT để áp được cho cả CSDL đã có dữ liệu lẫn cài mới.
--    Chỉ ghi vào các cột đang trống nên chạy lại nhiều lần không đè dữ liệu sửa tay.
-- =====================================================================

-- 4a) Thương hiệu: tên sản phẩm có dạng "<loại> <thương hiệu> <phiên bản>"
UPDATE products p
SET brand = b.brand
FROM (VALUES
    ('Sunhouse'), ('Philips'), ('Panasonic'), ('Lock&Lock'), ('Elmich'), ('Sharp'),
    ('Toshiba'), ('Kangaroo'), ('Comet'), ('Bluestone'), ('Xiaomi'), ('Electrolux'), ('Cuckoo')
) AS b(brand)
WHERE p.brand IS NULL AND p.name LIKE '%' || b.brand || '%';

-- 4b) Xuất xứ suy ra từ thương hiệu
UPDATE products SET origin = 'Việt Nam'
WHERE origin IS NULL AND brand IN ('Sunhouse', 'Kangaroo', 'Comet', 'Elmich', 'Lock&Lock', 'Bluestone');

UPDATE products SET origin = 'Nhập khẩu chính hãng'
WHERE origin IS NULL AND brand IN ('Philips', 'Panasonic', 'Sharp', 'Toshiba', 'Xiaomi', 'Electrolux', 'Cuckoo');

-- Sản phẩm không mang thương hiệu ngoài thì gắn nhãn riêng của cửa hàng
UPDATE products SET brand = 'BachHome' WHERE brand IS NULL;

UPDATE products SET origin = 'Việt Nam' WHERE origin IS NULL;

-- 4c) Chất liệu, thông số kỹ thuật và bảo hành theo từng loại sản phẩm.
--     Sản phẩm khớp nhiều tiền tố thì lấy tiền tố dài nhất (cụ thể nhất).
UPDATE products p
SET material = s.material,
    specification = s.specification,
    warranty_months = COALESCE(p.warranty_months, s.warranty_months)
FROM (
    SELECT DISTINCT ON (pr.id)
           pr.id, t.material, t.specification, t.warranty_months
    FROM products pr
    JOIN (VALUES
        ('Nồi cơm điện',            'Lòng nồi hợp kim chống dính',   'Dung tích 1.8L · Công suất 860W',       12),
        ('Bộ chảo chống dính',      'Hợp kim nhôm phủ chống dính',   'Bộ 3 cỡ 20/24/26cm · Đáy từ 3 lớp',     12),
        ('Chảo chống dính',         'Hợp kim nhôm phủ chống dính',   'Đường kính 26cm · Đáy từ 3 lớp',        12),
        ('Bộ nồi inox',             'Inox 304 cao cấp',              'Bộ 3 cỡ 16/20/24cm · Dùng được bếp từ', 12),
        ('Máy xay thịt',            'Cối inox 304',                  'Công suất 300W · Dung tích 2L',         12),
        ('Bộ dao nhà bếp',          'Thép không gỉ kèm khay gỗ',     'Bộ 6 món · Lưỡi dày 2mm',                6),
        ('Nồi áp suất',             'Inox 304 cao cấp',              'Dung tích 5L · Áp suất 80kPa',          12),
        ('Ấm đun siêu tốc',         'Inox 304',                      'Dung tích 1.8L · Công suất 1500W',      12),
        ('Ấm siêu tốc',             'Inox 304',                      'Dung tích 1.8L · Công suất 1500W',      12),
        ('Bình giữ nhiệt',          'Inox 304 hai lớp chân không',   'Dung tích 500ml · Giữ nhiệt 12 giờ',     6),
        ('Máy xay sinh tố',         'Cối thủy tinh chịu nhiệt',      'Công suất 700W · 2 tốc độ',             12),
        ('Quạt điện đứng',          'Nhựa ABS, động cơ lõi đồng',    '3 tốc độ · Hẹn giờ 120 phút',           12),
        ('Bàn ủi hơi nước',         'Mặt đế gốm chống dính',         'Công suất 2000W · Bình chứa 300ml',     12),
        ('Lò vi sóng',              'Khoang tráng men chống bám',    'Dung tích 20L · Công suất 800W',        12),
        ('Nồi chiên không dầu',     'Lòng nồi chống dính',           'Dung tích 5L · Công suất 1500W',        12),
        ('Máy lọc nước',            'Vỏ nhựa ABS, lõi lọc RO',       '9 lõi lọc · Công suất 15 lít/giờ',      24),
        ('Bếp từ đôi',              'Mặt kính chịu nhiệt',           '2 vùng nấu · Tổng công suất 4000W',     24),
        ('Máy ép trái cây',         'Lưới ép inox 304',              'Công suất 400W · Miệng ép 75mm',        12),
        ('Cây lau nhà',             'Bông microfiber, thân inox',    'Xoay 360 độ · Cơ chế tự vắt',            6),
        ('Chổi quét nhà',           'Sợi PET mềm, cán nhựa',         'Cán dài 120cm · Kèm hót rác',            6),
        ('Máy hút bụi cầm tay',     'Vỏ nhựa ABS',                   'Lực hút 12kPa · Pin 2200mAh',           12),
        ('Thùng rác thông minh',    'Nhựa PP nguyên sinh',           'Dung tích 12L · Cảm ứng hồng ngoại',    12),
        ('Bộ cọ rửa',               'Sợi nylon, tay cầm nhựa',       'Bộ 4 món · Treo tường tiện lợi',         3),
        ('Giá phơi đồ',             'Inox không gỉ',                 'Gấp gọn · Tải trọng 20kg',              12),
        ('Cây lau kính',            'Lưỡi cao su silicon, cán nhôm', 'Cán dài 1.2m · Lưỡi gạt 35cm',           6),
        ('Máy xịt rửa',             'Nhựa ABS, bơm lõi đồng',        'Áp lực 110 bar · Công suất 1400W',      12),
        ('Bộ khăn tắm cotton',      'Cotton 100%',                   'Bộ 4 món · Định lượng 500gsm',           6),
        ('Kệ để đồ nhà tắm inox',   'Inox 304 chống gỉ',             '3 tầng · Khoan tường hoặc dán keo',     12),
        ('Kệ nhà tắm inox',         'Inox 304 chống gỉ',             '3 tầng · Khoan tường hoặc dán keo',     12),
        ('Vòi sen tăng áp',         'Nhựa ABS mạ crôm',              '3 chế độ phun · Tăng áp tới 200%',      12),
        ('Gương LED cảm ứng',       'Kính cường lực viền nhôm',      'Đèn LED 18W · Chống mờ hơi nước',       12),
        ('Máy sấy tay',             'Vỏ inox sơn tĩnh điện',         'Công suất 1800W · Cảm biến tự động',    12),
        ('Thảm chống trượt',        'Cao su non phủ nhung',          'Kích thước 40x60cm · Đế chống trượt',    3),
        ('Bộ phụ kiện toilet',      'Inox 304 chống gỉ',             'Bộ 5 món · Kèm phụ kiện lắp đặt',       12),
        ('Hộp đựng mỹ phẩm',        'Nhựa acrylic trong suốt',       '3 ngăn · Kích thước 25x15x20cm',         3),
        ('Bộ chăn ga gối',          'Cotton 100% dệt sợi 40s',       'Bộ 4 món · Dùng cho nệm 1m8 x 2m',       6),
        ('Đèn ngủ để bàn',          'Nhựa ABS, chip LED',            'Công suất 5W · Ánh sáng vàng 3000K',    12),
        ('Gối cao su non',          'Cao su non memory foam',        'Kích thước 60x40cm · Cao 12cm',          6),
        ('Rèm cửa cản sáng',        'Vải blackout 3 lớp',            'Cản sáng 90% · Khổ 1.4m x 2.4m',         6),
        ('Tủ vải đa năng',          'Khung thép bọc vải oxford',     '3 tầng · Tải trọng 40kg',                6),
        ('Máy khuếch tán tinh dầu', 'Nhựa PP vân gỗ',                'Dung tích 300ml · Công suất 12W',       12),
        ('Nệm topper',              'Bông ép, vỏ cotton',            'Dày 5cm · Kích thước 1m6 x 2m',         12),
        ('Chăn lông cừu',           'Sợi polyester lông cừu',        'Kích thước 2m x 2.3m · Nặng 2.5kg',      6)
    ) AS t(prefix, material, specification, warranty_months)
      ON pr.name LIKE t.prefix || '%'
    ORDER BY pr.id, length(t.prefix) DESC
) AS s
WHERE p.id = s.id AND p.specification IS NULL;

-- Sản phẩm không khớp tiền tố nào vẫn có thông số tối thiểu để trang chi tiết không trống
UPDATE products
SET material = COALESCE(material, 'Theo mô tả nhà sản xuất'),
    specification = COALESCE(specification, 'Xem chi tiết trong phần mô tả sản phẩm'),
    warranty_months = COALESCE(warranty_months, 12)
WHERE material IS NULL OR specification IS NULL OR warranty_months IS NULL;

-- =====================================================================
-- 5) Ảnh sản phẩm: dùng bộ ảnh nội bộ trong frontend/public/products
--    thay cho ảnh ngẫu nhiên từ loremflickr (ảnh cũ thường không đúng sản phẩm
--    và phụ thuộc mạng). Chỉ thay các URL loremflickr nên chạy lại không đè
--    ảnh do quản trị viên tự tải lên.
-- =====================================================================
UPDATE products p
SET image_url = s.img
FROM (
    SELECT DISTINCT ON (pr.id) pr.id, t.img
    FROM products pr
    JOIN (VALUES
        ('Nồi cơm điện',            '/products/noi-com-dien.svg'),
        ('Bộ chảo chống dính',      '/products/chao-chong-dinh.svg'),
        ('Chảo chống dính',         '/products/chao-chong-dinh.svg'),
        ('Bộ nồi inox',             '/products/bo-noi-inox.svg'),
        ('Máy xay thịt',            '/products/may-xay-thit.svg'),
        ('Bộ dao nhà bếp',          '/products/bo-dao-nha-bep.svg'),
        ('Nồi áp suất',             '/products/noi-ap-suat.svg'),
        ('Ấm đun siêu tốc',         '/products/am-sieu-toc.svg'),
        ('Ấm siêu tốc',             '/products/am-sieu-toc.svg'),
        ('Bình giữ nhiệt',          '/products/binh-giu-nhiet.svg'),
        ('Máy xay sinh tố',         '/products/may-xay-sinh-to.svg'),
        ('Quạt điện đứng',          '/products/quat-dien-dung.svg'),
        ('Bàn ủi hơi nước',         '/products/ban-ui-hoi-nuoc.svg'),
        ('Lò vi sóng',              '/products/lo-vi-song.svg'),
        ('Nồi chiên không dầu',     '/products/noi-chien-khong-dau.svg'),
        ('Máy lọc nước',            '/products/may-loc-nuoc.svg'),
        ('Bếp từ đôi',              '/products/bep-tu-doi.svg'),
        ('Máy ép trái cây',         '/products/may-ep-trai-cay.svg'),
        ('Cây lau nhà',             '/products/cay-lau-nha.svg'),
        ('Chổi quét nhà',           '/products/choi-quet-nha.svg'),
        ('Máy hút bụi',             '/products/may-hut-bui.svg'),
        ('Thùng rác thông minh',    '/products/thung-rac.svg'),
        ('Bộ cọ rửa',               '/products/bo-co-rua.svg'),
        ('Giá phơi đồ',             '/products/gia-phoi-do.svg'),
        ('Cây lau kính',            '/products/cay-lau-kinh.svg'),
        ('Máy xịt rửa',             '/products/may-xit-rua.svg'),
        ('Bộ khăn tắm',             '/products/bo-khan-tam.svg'),
        ('Kệ để đồ nhà tắm inox',   '/products/ke-nha-tam.svg'),
        ('Kệ nhà tắm inox',         '/products/ke-nha-tam.svg'),
        ('Vòi sen tăng áp',         '/products/voi-sen.svg'),
        ('Gương LED cảm ứng',       '/products/guong-led.svg'),
        ('Máy sấy tay',             '/products/may-say-tay.svg'),
        ('Thảm chống trượt',        '/products/tham-chong-truot.svg'),
        ('Bộ phụ kiện toilet',      '/products/phu-kien-toilet.svg'),
        ('Hộp đựng mỹ phẩm',        '/products/hop-my-pham.svg'),
        ('Bộ chăn ga gối',          '/products/chan-ga-goi.svg'),
        ('Đèn ngủ để bàn',          '/products/den-ngu-de-ban.svg'),
        ('Gối cao su non',          '/products/goi-cao-su-non.svg'),
        ('Rèm cửa cản sáng',        '/products/rem-cua.svg'),
        ('Tủ vải đa năng',          '/products/tu-vai.svg'),
        ('Máy khuếch tán tinh dầu', '/products/khuech-tan-tinh-dau.svg'),
        ('Nệm topper',              '/products/nem-topper.svg'),
        ('Chăn lông cừu',           '/products/chan-long-cuu.svg')
    ) AS t(prefix, img)
      ON pr.name LIKE t.prefix || '%'
    ORDER BY pr.id, length(t.prefix) DESC
) AS s
WHERE p.id = s.id AND (p.image_url IS NULL OR p.image_url LIKE '%loremflickr%');

-- Sản phẩm không khớp tiền tố nào vẫn có ảnh mặc định theo danh mục
UPDATE products p
SET image_url = CASE c.name
        WHEN 'Đồ dùng nhà bếp'  THEN '/products/bo-noi-inox.svg'
        WHEN 'Điện gia dụng'    THEN '/products/lo-vi-song.svg'
        WHEN 'Dụng cụ vệ sinh'  THEN '/products/bo-co-rua.svg'
        WHEN 'Dọn dẹp nhà cửa'  THEN '/products/bo-co-rua.svg'
        WHEN 'Phòng tắm'        THEN '/products/bo-khan-tam.svg'
        WHEN 'Đồ dùng phòng tắm' THEN '/products/bo-khan-tam.svg'
        WHEN 'Phòng ngủ'        THEN '/products/chan-ga-goi.svg'
        WHEN 'Đồ dùng phòng ngủ' THEN '/products/chan-ga-goi.svg'
        ELSE '/products/bo-noi-inox.svg'
    END
FROM categories c
WHERE p.category_id = c.id
  AND (p.image_url IS NULL OR p.image_url LIKE '%loremflickr%');
