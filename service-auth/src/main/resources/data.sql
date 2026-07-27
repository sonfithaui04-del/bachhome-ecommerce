-- BachHome (auth service): đẩy sequence id của bảng users lên dải 1000+.
--
-- Lý do: service-order seed sẵn 17 đơn hàng mẫu với user_id 2..10, nhưng hai
-- service dùng hai database riêng nên không có khoá ngoại nào ràng buộc. Nếu
-- người dùng đăng ký mới được cấp id 2, 3, 4... họ sẽ "thừa kế" đúng những đơn
-- hàng mẫu đó khi mở trang Đơn hàng của tôi.
--
-- Đẩy sequence lên 1000 để tài khoản đăng ký mới luôn nằm ngoài dải của dữ liệu
-- mẫu. Idempotent: chạy lại nhiều lần không làm tụt id đã cấp.
-- Dạng SELECT ... FROM ... WHERE để nếu không tìm thấy sequence thì trả về 0 dòng
-- (không lỗi), tránh làm service-auth chết lúc khởi động.
SELECT setval(s.seq, GREATEST(1000, COALESCE((SELECT MAX(id) FROM users), 0)), true)
FROM (SELECT pg_get_serial_sequence('users', 'id') AS seq) s
WHERE s.seq IS NOT NULL;
