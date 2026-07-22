# HƯỚNG DẪN DI CHUYỂN DỰ ÁN SANG MÁY MỚI (MIGRATION GUIDE)

Tài liệu này hướng dẫn cách cập nhật dự án trên một máy tính khác đang chạy phiên bản cũ của "Food Ordering System".

## Yêu cầu trên máy mới
- **Java 17+**
- **Maven**
- **Node.js 18+**
- **Docker Desktop** (đã bật Kubernetes trong Settings)

---

## BƯỚC 1: Dọn dẹp môi trường cũ (Clean Up)
Trước khi cập nhật code, hãy xóa deployment cũ để tránh xung đột.

1. Mở **PowerShell** (hoặc Terminal) tại thư mục dự án.
2. Xóa Kubernetes Namespace cũ:
   ```powershell
   kubectl delete namespace food-ordering
   ```
   *(Lệnh này có thể mất vài phút để hoàn tất)*

3. Xóa Docker Images cũ (tùy chọn nhưng khuyến khích):
   ```powershell
   # Xóa các image bắt đầu bằng food-ordering-
   docker images | Where-Object { $_ -match 'food-ordering' } | ForEach-Object { docker rmi $_.split(" ", [StringSplitOptions]::RemoveEmptyEntries)[2] -f }
   ```

---

## BƯỚC 2: Cập nhật Source Code
**QUAN TRỌNG:** Không nên ghi đè (merge) thư mục mới lên thư mục cũ.
1. Xóa toàn bộ thư mục dự án cũ trên máy mới (trừ khi bạn có file cấu hình riêng chưa backup).
2. Copy thư mục dự án mới vào vị trí mong muốn.
3. Đảm bảo cấu trúc thư mục bao gồm: `k8s/`, `service-*/`, `frontend/`, `docker-compose.yml`, ...

---

## BƯỚC 3: Quy trình Build & Deploy lại từ đầu

### 1. Build Backend (Maven Package)
Cần tạo các file `.jar` mới nhất cho các services.
```powershell
mvn clean package -DskipTests
```
*Nếu lệnh này lỗi ở root, hãy chạy lệnh trên trong từng thư mục con (`service-auth`, `service-order`, v.v.) hoặc sửa lại file `pom.xml` gốc nếu cần.*

### 2. Build Docker Images
Tạo lại các image mới từ file .jar vừa build.
```powershell
docker-compose build
```

### 3. Deploy lên Kubernetes
Chạy các lệnh sau theo thứ tự:

```powershell
# 1. Tạo Namespace
kubectl apply -f k8s/namespace.yaml

# 2. Config & Secrets (Database credentials, Eureka URL...)
kubectl apply -f k8s/configmap.yaml

# 3. Cơ sở dữ liệu & Message Broker (Postgres, RabbitMQ)
kubectl apply -f k8s/databases.yaml

# ĐỢI KHOẢNG 1-2 PHÚT CHO DB & RABBITMQ SẴN SÀNG TRƯỚC KHI CHẠY BƯỚC TIẾP THEO

# 4. Deploy các Microservices (Backend)
kubectl apply -f k8s/deployments.yaml
```

---

## BƯỚC 4: Khởi chạy Frontend
Với Frontend, bạn cần cài đặt lại các thư viện (dependencies).

**1. User App:**
```powershell
cd frontend
npm install  # Chỉ cần chạy lần đầu hoặc khi có thư viện mới
npm run dev
```

**2. Admin App:**
```powershell
cd frontend-admin
npm install
npm run dev
```

---

## BƯỚC 5: Kết nối (Port Forwarding)
Để truy cập API và Database từ máy tính (localhost):

**Mở Terminal mới và giữ cho lệnh này luôn chạy:**
```powershell
kubectl port-forward -n food-ordering svc/api-gateway 8080:8080
```
*(Bây giờ backend sẽ active tại `http://localhost:8080`)*

---

## Xử lý sự cố thường gặp (Troubleshooting)

**1. RabbitMQ chưa lên kịp khiến các services bị lỗi kết nối:**
- Chạy: `kubectl delete -f k8s/deployments.yaml` sau đó `kubectl apply -f k8s/deployments.yaml` lại để restart các services.

**2. Lỗi WebSocket (500 Error):**
- Đảm bảo `service-socket` đã running.
- Restart Gateway: `kubectl rollout restart deployment/api-gateway -n food-ordering`

**3. Database trống trơn:**
- Đây là bình thường vì Kubernetes trên Docker Desktop thường không giữ data khi xóa namespace trừ khi cấu hình HostPath đặc biệt. Bạn sẽ cần tạo lại dữ liệu test (Tài khoản, Menu, v.v.).

