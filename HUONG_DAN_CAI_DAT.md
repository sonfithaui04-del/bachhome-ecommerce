# Hướng dẫn Cài đặt & Triển khai Hệ thống Food Ordering (Cho máy mới)

Tài liệu này hướng dẫn chi tiết cách cài đặt môi trường và chạy dự án khi chuyển sang một máy tính mới hoàn toàn.

---

## 🏗️ 1. Yêu cầu Hệ thống (Prerequisites)

Hãy cài đặt các phần mềm sau trước khi bắt đầu:

1.  **Docker Desktop** (Bắt buộc cho Backend)
    *   Tải về: [Docker Desktop](https://www.docker.com/products/docker-desktop)
    *   *Lưu ý*: Sau khi cài, hãy bật Docker Desktop lên và đợi nó khởi động xong.
2.  **Node.js LTS** (Bắt buộc cho Frontend)
    *   Tải về: [Node.js](https://nodejs.org/) (Chọn bản LTS - ví dụ v18 hoặc v20).
    *   Dùng để chạy website khách hàng và trang quản trị.
3.  **Git**
    *   Tải về: [Git SCM](https://git-scm.com/)
    *   Dùng để tải mã nguồn về máy.

*Không cần cài Java/Maven nếu bạn chạy Backend bằng Docker (vì Docker sẽ tự làm việc đó).*

---

## 🚀 2. Tải Mã Nguồn

Mở **Command Prompt (CMD)** hoặc **PowerShell** hoặc **Git Bash**:

```bash
# Clone dự án về máy
git clone <link-repo-cua-ban>
cd food-ordering
```

---

## 🐳 3. Chạy Backend (Microservices)

Chúng ta sẽ dùng Docker Compose để chạy toàn bộ: 6 Databases, RabbitMQ, Eureka, và 7 Microservices.

### Bước 3.1: Khởi động hệ thống
Tại thư mục gốc `food-ordering`, chạy lệnh:

```powershell
docker-compose up -d --build
```
*Lần đầu chạy sẽ mất khoảng 5-15 phút để tải thư viện và build các services. Hãy kiên nhẫn.*

### Bước 3.2: Kiểm tra trạng thái
Mở Docker Desktop hoặc chạy lệnh `docker-compose ps`.
Đảm bảo các container đều có trạng thái **Running (Up)**.

Truy cập thử:
*   **Eureka (Quản lý Service):** [http://localhost:8761](http://localhost:8761)
*   **API Gateway:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Bước 3.3: Nạp dữ liệu vào Database (QUAN TRỌNG)
Khi chạy trên máy mới, Database sẽ trống trơn. Bạn cần nạp dữ liệu từ thư mục `SQL_Backup`.

Chạy các lệnh sau trong PowerShell (hoặc Terminal):

```powershell
# 1. Nạp Service Menu (Danh mục, Món ăn)
cat "SQL_Backup/menu_service.sql" | docker exec -i postgres-menu psql -U postgres -d food_ordering_menu

# 2. Nạp Service Auth (Tài khoản users)
cat "SQL_Backup/auth_service.sql" | docker exec -i postgres-auth psql -U postgres -d food_ordering_auth

# 3. Nạp Service Payment (Cấu hình thanh toán)
cat "SQL_Backup/payment_service.sql" | docker exec -i postgres-payment psql -U postgres -d food_ordering_payment

# Các service khác (Order, Inventory, Notification) thường tự sinh dữ liệu khi chạy, không cần backup.
```
*Lưu ý: Nếu lệnh `cat` không chạy trên CMD Windows cũ, hãy dùng PowerShell.*

---

## 💻 4. Chạy Frontend (Giao diện)

Dự án có 2 ứng dụng Frontend riêng biệt. Bạn cần mở 2 cửa sổ Terminal khác nhau để chạy song song.

### Bước 4.1: Chạy Web Khách Hàng (Customer App)

Mở Terminal mới, từ thư mục gốc:
```powershell
cd frontend

# Cài đặt thư viện (chỉ cần chạy lần đầu)
npm install

# Kiểm tra file cấu hình
# Đảm bảo file .env tồn tại (nếu chưa có, tạo file .env với nội dung: VITE_API_URL=http://localhost:8080)

# Chạy ứng dụng
npm run dev
```
👉 Truy cập: **[http://localhost:5173](http://localhost:5173)**

### Bước 4.2: Chạy Web Quản Trị (Admin Panel)

Mở thêm 1 Terminal mới nữa:
```powershell
cd frontend-admin

# Cài đặt thư viện
npm install

# Chạy ứng dụng
npm run dev
```
👉 Truy cập: **[http://localhost:5174](http://localhost:5174)**

---

## ☸️ 5. Chạy trên Kubernetes (Nâng cao - Optional)

Nếu bạn cần demo Kubernetes trên máy mới:

1.  **Bật Kubernetes** trong cài đặt Docker Desktop.
2.  **Build Docker Images** (K8s không tự build như Compose):
    Mở PowerShell tại thư mục gốc và chạy:
    ```powershell
    docker-compose build
    ```
3.  **Deploy lên K8s**:
    ```powershell
    kubectl apply -f k8s/namespace.yaml
    kubectl apply -f k8s/databases.yaml
    kubectl apply -f k8s/configmap.yaml
    kubectl apply -f k8s/deployments.yaml
    kubectl apply -f k8s/ingress.yaml
    ```
4.  **Kiểm tra**: `kubectl get pods -n food-ordering`

---

## ❓ 6. Xử lý sự cố (Troubleshooting)

1.  **Lỗi "Port already in use"**:
    *   Tắt các ứng dụng đang chiếm cổng 8080, 5432...
    *   Hoặc chạy `docker-compose down` rồi thử lại.

2.  **Không đăng nhập được**:
    *   Kiểm tra xem đã nạp dữ liệu `auth_service.sql` chưa (Bước 3.3).
    *   Tài khoản mặc định thường là: `admin@gmail.com` / `123456` (hoặc check trong sql).

3.  **Frontend không load được sản phẩm**:
    *   Kiểm tra API Gateway (localhost:8080) có chạy không.
    *   Kiểm tra Service Menu có chạy không (check Eureka).
    *   Inspect Element (F12) -> Network xem API trả về lỗi gì.

4.  **Docker báo lỗi bộ nhớ (OOM)**:
    *   Dự án này khá nặng (15+ containers). Hãy tăng RAM cho Docker Desktop lên ít nhất 4GB (trong Settings -> Resources).
