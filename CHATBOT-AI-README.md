# Chatbot AI tư vấn sản phẩm — BachHome

Tính năng AI: chatbot tư vấn đồ gia dụng, dùng **Google Gemini** (gói miễn phí).
Kiến trúc: `service-ai` (Spring Boot, port 8087) đăng ký Eureka, gọi Gemini + lấy sản phẩm
từ `service-menu`. Frontend có widget chat nổi ở góc phải.

```
Frontend (widget) ──POST /api/ai/chat──► API Gateway (8080) ──lb──► service-ai (8087)
                                                                     │  lấy sản phẩm
                                                                     ▼  từ service-menu
                                                                  gọi Gemini API
```

## 1. Lấy Gemini API key (miễn phí)
1. Vào https://aistudio.google.com/app/apikey (đăng nhập Google).
2. Bấm **Create API key** → copy chuỗi key.

## 2. Khai báo key cho hệ thống
Tạo file `.env` **trong thư mục gốc dự án** (cùng chỗ `docker-compose.yml`) với nội dung:
```
GEMINI_API_KEY=dán_key_vào_đây
```
> Docker Compose tự đọc file `.env` này và truyền vào `service-ai`. Không commit key lên Git.

## 3. Chạy backend (đã có sẵn service-ai)
```bash
docker compose up -d --build
```
Kiểm tra service AI đã lên:
```bash
docker compose ps
docker compose logs -f service-ai
```
Test nhanh API (PowerShell hoặc curl):
```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"Tư vấn giúp tôi nồi chiên không dầu\"}"
```
→ Trả về `{"reply":"..."}` là chatbot chạy ok.

## 4. Chạy frontend
```bash
cd frontend
npm install
npm run dev
```
Mở http://localhost:3000 → góc phải dưới có nút chat 💬 → bấm để hỏi trợ lý.

## Ghi chú
- Chưa cấu hình key → chatbot vẫn chạy nhưng trả lời "chưa cấu hình khóa API".
- Đổi model: sửa `GEMINI_MODEL` trong `docker-compose.yml` (mặc định `gemini-1.5-flash`).
- Chatbot chỉ tư vấn sản phẩm đang bán (lấy từ service-menu) — chống bịa sản phẩm.
- File liên quan:
  - `service-ai/` — toàn bộ service backend.
  - `api-gateway/src/main/resources/application.yml` — route `/api/ai/**`.
  - `docker-compose.yml` — container `service-ai`.
  - `frontend/src/components/AiChatWidget.jsx` — widget chat.
