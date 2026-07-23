-- BachHome sample data (order service): 17 orders + their order_items.
-- PLAIN SQL only (no procedural blocks / dollar-quoting) so Spring Boot ScriptUtils splits it correctly on ";".
-- Idempotent: every INSERT carries a WHERE NOT EXISTS guard.
-- Order totals are recomputed from order_items (minus points discount) so they always match.

-- 1) Orders (explicit ids 1..17). total_amount starts at 0 and is recomputed from items below.
INSERT INTO orders (id, user_id, email, total_amount, status, delivery_address, phone_number, customer_name, notes, payment_method, points_used, payment_status, created_at, updated_at)
SELECT v.id, v.user_id, v.email, v.total_amount, v.status, v.delivery_address, v.phone_number, v.customer_name, v.notes, v.payment_method, v.points_used, v.payment_status, v.created_at, v.updated_at
FROM (VALUES
    (1,  2,  'an.nguyen@example.com', 0, 'COMPLETED',  '12 Lê Lợi, Q.1, TP.HCM',            '0901234567', 'Nguyễn Văn An', 'Giao giờ hành chính',      'COD',   0, 'SUCCESS', NOW() - INTERVAL '20 day', NOW() - INTERVAL '19 day'),
    (2,  3,  'binh.tran@example.com', 0, 'COMPLETED',  '45 Trần Hưng Đạo, Q.5, TP.HCM',     '0902345678', 'Trần Thị Bình', NULL,                       'SEPAY', 0, 'SUCCESS', NOW() - INTERVAL '18 day', NOW() - INTERVAL '17 day'),
    (3,  4,  'cuong.le@example.com',  0, 'DELIVERING', '78 Nguyễn Trãi, Q.Thanh Xuân, HN',  '0903456789', 'Lê Văn Cường',  'Gọi trước khi giao',       'COD',   0, 'PENDING', NOW() - INTERVAL '2 day',  NOW() - INTERVAL '1 day'),
    (4,  2,  'an.nguyen@example.com', 0, 'COMPLETED',  '12 Lê Lợi, Q.1, TP.HCM',            '0901234567', 'Nguyễn Văn An', NULL,                       'SEPAY', 0, 'SUCCESS', NOW() - INTERVAL '15 day', NOW() - INTERVAL '14 day'),
    (5,  5,  'dung.pham@example.com', 0, 'PENDING',    '9 Hai Bà Trưng, Q.1, TP.HCM',       '0904567890', 'Phạm Thị Dung', 'Thanh toán khi nhận hàng', 'COD',   0, 'PENDING', NOW() - INTERVAL '1 day',  NOW() - INTERVAL '1 day'),
    (6,  6,  'em.hoang@example.com',  0, 'CANCELLED',  '23 Cầu Giấy, Q.Cầu Giấy, HN',       '0905678901', 'Hoàng Văn Em',  'Khách đổi ý',              'COD',   0, 'FAILED',  NOW() - INTERVAL '10 day', NOW() - INTERVAL '10 day'),
    (7,  3,  'binh.tran@example.com', 0, 'COMPLETED',  '45 Trần Hưng Đạo, Q.5, TP.HCM',     '0902345678', 'Trần Thị Bình', NULL,                       'SEPAY', 0, 'SUCCESS', NOW() - INTERVAL '12 day', NOW() - INTERVAL '11 day'),
    (8,  7,  'giang.vu@example.com',  0, 'CONFIRMED',  '56 Điện Biên Phủ, Q.3, TP.HCM',     '0906789012', 'Vũ Thị Giang',  NULL,                       'COD',   0, 'PENDING', NOW() - INTERVAL '3 day',  NOW() - INTERVAL '3 day'),
    (9,  4,  'cuong.le@example.com',  0, 'PREPARING',  '78 Nguyễn Trãi, Q.Thanh Xuân, HN',  '0903456789', 'Lê Văn Cường',  'Đóng gói cẩn thận',        'SEPAY', 0, 'SUCCESS', NOW() - INTERVAL '2 day',  NOW() - INTERVAL '2 day'),
    (10, 8,  'hung.do@example.com',   0, 'READY',      '101 Lạc Long Quân, Q.Tây Hồ, HN',   '0907890123', 'Đỗ Văn Hùng',   NULL,                       'COD',   0, 'PENDING', NOW() - INTERVAL '4 day',  NOW() - INTERVAL '3 day'),
    (11, 2,  'an.nguyen@example.com', 0, 'COMPLETED',  '12 Lê Lợi, Q.1, TP.HCM',            '0901234567', 'Nguyễn Văn An', NULL,                       'SEPAY', 0, 'SUCCESS', NOW() - INTERVAL '8 day',  NOW() - INTERVAL '7 day'),
    (12, 5,  'dung.pham@example.com', 0, 'DELIVERING', '9 Hai Bà Trưng, Q.1, TP.HCM',       '0904567890', 'Phạm Thị Dung', 'Giao buổi tối',            'COD',   0, 'PENDING', NOW() - INTERVAL '1 day',  NOW()),
    (13, 9,  'lan.bui@example.com',   0, 'COMPLETED',  '34 Phan Đình Phùng, Q.Ba Đình, HN', '0908901234', 'Bùi Thị Lan',   'Dùng điểm tích lũy',       'SEPAY', 5, 'SUCCESS', NOW() - INTERVAL '9 day',  NOW() - INTERVAL '8 day'),
    (14, 6,  'em.hoang@example.com',  0, 'PENDING',    '23 Cầu Giấy, Q.Cầu Giấy, HN',       '0905678901', 'Hoàng Văn Em',  NULL,                       'COD',   0, 'PENDING', NOW() - INTERVAL '1 day',  NOW()),
    (15, 7,  'giang.vu@example.com',  0, 'COMPLETED',  '56 Điện Biên Phủ, Q.3, TP.HCM',     '0906789012', 'Vũ Thị Giang',  NULL,                       'SEPAY', 0, 'SUCCESS', NOW() - INTERVAL '6 day',  NOW() - INTERVAL '5 day'),
    (16, 3,  'binh.tran@example.com', 0, 'CONFIRMED',  '45 Trần Hưng Đạo, Q.5, TP.HCM',     '0902345678', 'Trần Thị Bình', 'Giao cuối tuần',           'COD',   0, 'PENDING', NOW() - INTERVAL '2 day',  NOW() - INTERVAL '2 day'),
    (17, 10, 'minh.ngo@example.com',  0, 'COMPLETED',  '67 Nguyễn Huệ, Q.1, TP.HCM',        '0909012345', 'Ngô Văn Minh',  'Khách VIP',                'SEPAY', 0, 'SUCCESS', NOW() - INTERVAL '5 day',  NOW() - INTERVAL '4 day')
) AS v(id, user_id, email, total_amount, status, delivery_address, phone_number, customer_name, notes, payment_method, points_used, payment_status, created_at, updated_at)
WHERE NOT EXISTS (SELECT 1 FROM orders);

-- 2) Order items (ids auto-generated). menu_item_id/name/price/image reference the 12 hero products.
INSERT INTO order_items (order_id, menu_item_id, menu_item_name, quantity, price, subtotal, image_url)
SELECT v.order_id, v.menu_item_id, v.menu_item_name, v.quantity, v.price, v.subtotal, v.image_url
FROM (VALUES
    (1,  1,  'Nồi cơm điện Cuckoo 1.8L',   1, 1890000, 1890000, 'https://loremflickr.com/500/400/rice,cooker?lock=1'),
    (1,  5,  'Ấm siêu tốc Sunhouse 1.8L',  2, 350000,  700000,  'https://loremflickr.com/500/400/electric,kettle?lock=5'),
    (2,  4,  'Máy xay sinh tố Philips',    1, 890000,  890000,  'https://loremflickr.com/500/400/blender?lock=4'),
    (2,  8,  'Cây lau nhà xoay 360 độ',    1, 250000,  250000,  'https://loremflickr.com/500/400/mop,cleaning?lock=8'),
    (3,  7,  'Máy hút bụi cầm tay',        1, 1200000, 1200000, 'https://loremflickr.com/500/400/vacuum,cleaner?lock=7'),
    (4,  11, 'Bộ chăn ga gối cotton',      1, 850000,  850000,  'https://loremflickr.com/500/400/bedding,bed?lock=11'),
    (4,  12, 'Đèn ngủ để bàn LED',         2, 220000,  440000,  'https://loremflickr.com/500/400/table,lamp?lock=12'),
    (5,  2,  'Bộ chảo chống dính 3 món',   1, 650000,  650000,  'https://loremflickr.com/500/400/frying,pan?lock=2'),
    (5,  3,  'Bộ dao nhà bếp 6 món',       1, 450000,  450000,  'https://loremflickr.com/500/400/kitchen,knife?lock=3'),
    (6,  6,  'Quạt điện đứng Panasonic',   1, 750000,  750000,  'https://loremflickr.com/500/400/electric,fan?lock=6'),
    (7,  9,  'Bộ khăn tắm cotton cao cấp', 2, 320000,  640000,  'https://loremflickr.com/500/400/bath,towel?lock=9'),
    (7,  10, 'Kệ để đồ nhà tắm inox',      1, 480000,  480000,  'https://loremflickr.com/500/400/bathroom,shelf?lock=10'),
    (8,  5,  'Ấm siêu tốc Sunhouse 1.8L',  1, 350000,  350000,  'https://loremflickr.com/500/400/electric,kettle?lock=5'),
    (8,  8,  'Cây lau nhà xoay 360 độ',    2, 250000,  500000,  'https://loremflickr.com/500/400/mop,cleaning?lock=8'),
    (9,  1,  'Nồi cơm điện Cuckoo 1.8L',   1, 1890000, 1890000, 'https://loremflickr.com/500/400/rice,cooker?lock=1'),
    (10, 4,  'Máy xay sinh tố Philips',    2, 890000,  1780000, 'https://loremflickr.com/500/400/blender?lock=4'),
    (10, 2,  'Bộ chảo chống dính 3 món',   1, 650000,  650000,  'https://loremflickr.com/500/400/frying,pan?lock=2'),
    (11, 12, 'Đèn ngủ để bàn LED',         3, 220000,  660000,  'https://loremflickr.com/500/400/table,lamp?lock=12'),
    (12, 7,  'Máy hút bụi cầm tay',        1, 1200000, 1200000, 'https://loremflickr.com/500/400/vacuum,cleaner?lock=7'),
    (12, 9,  'Bộ khăn tắm cotton cao cấp', 1, 320000,  320000,  'https://loremflickr.com/500/400/bath,towel?lock=9'),
    (13, 11, 'Bộ chăn ga gối cotton',      1, 850000,  850000,  'https://loremflickr.com/500/400/bedding,bed?lock=11'),
    (14, 3,  'Bộ dao nhà bếp 6 món',       2, 450000,  900000,  'https://loremflickr.com/500/400/kitchen,knife?lock=3'),
    (15, 6,  'Quạt điện đứng Panasonic',   1, 750000,  750000,  'https://loremflickr.com/500/400/electric,fan?lock=6'),
    (15, 5,  'Ấm siêu tốc Sunhouse 1.8L',  1, 350000,  350000,  'https://loremflickr.com/500/400/electric,kettle?lock=5'),
    (16, 10, 'Kệ để đồ nhà tắm inox',      1, 480000,  480000,  'https://loremflickr.com/500/400/bathroom,shelf?lock=10'),
    (17, 1,  'Nồi cơm điện Cuckoo 1.8L',   1, 1890000, 1890000, 'https://loremflickr.com/500/400/rice,cooker?lock=1'),
    (17, 4,  'Máy xay sinh tố Philips',    1, 890000,  890000,  'https://loremflickr.com/500/400/blender?lock=4'),
    (17, 8,  'Cây lau nhà xoay 360 độ',    1, 250000,  250000,  'https://loremflickr.com/500/400/mop,cleaning?lock=8')
) AS v(order_id, menu_item_id, menu_item_name, quantity, price, subtotal, image_url)
WHERE NOT EXISTS (SELECT 1 FROM order_items);

-- 3) Recompute order totals from items (subtract points discount = points_used * 1000, floor at 0).
UPDATE orders o
SET total_amount = GREATEST(sub.s - COALESCE(o.points_used, 0) * 1000, 0)
FROM (SELECT order_id, SUM(subtotal) AS s FROM order_items GROUP BY order_id) sub
WHERE sub.order_id = o.id;

-- Keep identity sequences in sync with the highest inserted id.
SELECT setval(pg_get_serial_sequence('orders', 'id'), COALESCE((SELECT MAX(id) FROM orders), 1), true);
SELECT setval(pg_get_serial_sequence('order_items', 'id'), COALESCE((SELECT MAX(id) FROM order_items), 1), true);
