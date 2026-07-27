# Chatbot AI tư vấn sản phẩm — BachHome

Tính năng AI: chatbot tư vấn đồ gia dụng, dùng **Google Gemini** (gói miễn phí).
Kiến trúc: `service-ai` (Spring Boot, port 8087) đăng ký Eureka, gọi Gemini + lấy sản phẩm
từ `service-product`. Frontend có widget chat nổi ở góc phải.

```
Frontend (widget) ──POST /api/ai/chat──► API Gateway (8080) ──lb──► service-ai (8087)
                                                                     │  lấy sản phẩm
                                                                     ▼  từ service-product
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
- Đổi model: sửa `GEMINI_MODEL` trong `.env` (mặc định `gemini-3.5-flash-lite`).
- Chatbot chỉ tư vấn sản phẩm đang bán (lấy từ service-product) — chống bịa sản phẩm.

## Xử lý sự cố: chatbot trả lời "Xin lỗi, trợ lý đang bận"
Đây là câu fallback khi gọi Gemini lỗi. Xem nguyên nhân thật trong log:
```bash
docker compose logs --tail 50 service-ai
```
Hay gặp nhất là **HTTP 429 — hết quota gói miễn phí**. Quota tính **theo từng model**,
nên chỉ cần đổi `GEMINI_MODEL` sang model khác là có hạn mức mới:

| Model | Ghi chú (kiểm tra 26/07/2026) |
|-------|-------------------------------|
| `gemini-3.5-flash-lite` | Mặc định, hạn mức rộng |
| `gemini-3.1-flash-lite` | Dự phòng |
| `gemini-3.6-flash` | Mạnh hơn, hạn mức hẹp hơn |
| `gemini-2.5-flash` | Chỉ 20 request/ngày |
| `gemini-2.0-flash*`, `gemini-2.5-flash-lite` | Đã bị khoá với tài khoản mới |

Đổi xong chạy `docker compose up -d service-ai` (không cần build lại).
- File liên quan:
  - `service-ai/` — toàn bộ service backend.
  - `api-gateway/src/main/resources/application.yml` — route `/api/ai/**`.
  - `docker-compose.yml` — container `service-ai`.
  - `frontend/src/components/AiChatWidget.jsx` — widget chat.
