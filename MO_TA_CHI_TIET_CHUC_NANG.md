# MÔ TẢ CHI TIẾT CÁC CHỨC NĂNG HỆ THỐNG

Dưới đây là các bảng mô tả chi tiết quy trình nghiệp vụ (Use Case Specifications) và biểu đồ tuần tự (Sequence Diagrams) cho các chức năng cốt lõi của **Hệ thống đặt món ăn trực tuyến**.

---

## 1. UC_DangKy: Đăng ký tài khoản

### Bảng mô tả chi tiết

| Thuộc tính | Chi tiết |
| :--- | :--- |
| **Mã Use case** | `UC_DangKy` |
| **Tên Use case** | Đăng ký tài khoản khách hàng |
| **Tác nhân** | Khách hàng (Vãng lai) |
| **Mô tả** | Cho phép khách hàng mới tạo tài khoản để sử dụng các dịch vụ đặt món. |
| **Sự kiện kích hoạt** | Người dùng bấm nút "Đăng ký" trên trang chủ hoặc trang đăng nhập. |
| **Tiền điều kiện** | Người dùng chưa đăng nhập. |
| **Hậu điều kiện** | Tài khoản mới được tạo, lưu vào CSDL và người dùng có thể đăng nhập. |

#### Luồng sự kiện chính (Main Flow)

| # | Thực hiện bởi | Hành động |
|---|---|---|
| 1 | Người dùng | Nhấn chọn chức năng "Đăng ký" trên giao diện. |
| 2 | Hệ thống | Hiển thị form đăng ký (Email, Password, Full Name, Phone). |
| 3 | Người dùng | Nhập đầy đủ thông tin vào form và nhấn "Gửi". |
| 4 | Hệ thống | Kiểm tra tính hợp lệ của dữ liệu (Format email, độ dài mật khẩu...). |
| 5 | Hệ thống | Kiểm tra email đã tồn tại trong hệ thống chưa (Call Auth Service). |
| 6 | Hệ thống | Tạo tài khoản mới, mã hóa mật khẩu và lưu vào Database. |
| 7 | Hệ thống | Thông báo "Đăng ký thành công" và chuyển hướng về trang đăng nhập. |

#### Luồng sự kiện thay thế (Alternative Flow)

| # | Tình huống | Hành xử của hệ thống |
|---|---|---|
| 4a | Dữ liệu không hợp lệ | Hiển thị thông báo lỗi cụ thể tại trường nhập liệu tương ứng và yêu cầu nhập lại (Quay lại B3). |
| 5a | Email đã tồn tại | Hiển thị thông báo "Email này đã được sử dụng" và yêu cầu dùng email khác (Quay lại B3). |

### Biểu đồ tuần tự (Sequence Diagram)

```mermaid
sequenceDiagram
    autonumber
    actor U as Người dùng
    participant FE as Frontend App
    participant GW as API Gateway
    participant Auth as Auth Service
    participant DB as Auth DB

    U->>FE: 1. Click "Đăng ký"
    FE-->>U: 2. Hiển thị Form Đăng ký
    U->>FE: 3. Nhập Info & Submit
    FE->>GW: POST /api/auth/register
    GW->>Auth: Forward Request
    
    activate Auth
    Auth->>Auth: Validate Data
    Auth->>DB: Check Email Exists?
    alt Email đã tồn tại
        DB-->>Auth: True
        Auth-->>GW: 409 Conflict
        GW-->>FE: Error: Email exists
        FE-->>U: Hiển thị lỗi
    else Email hợp lệ
        DB-->>Auth: False
        Auth->>Auth: Hash Password
        Auth->>DB: Save User
        DB-->>Auth: Success
        Auth-->>GW: 201 Created
        GW-->>FE: Success Response
        FE-->>U: Thông báo & Redirect Login
    end
    deactivate Auth
```

---

## 2. UC_DangNhap: Đăng nhập hệ thống

### Bảng mô tả chi tiết

| Thuộc tính | Chi tiết |
| :--- | :--- |
| **Mã Use case** | `UC_DangNhap` |
| **Tên Use case** | Đăng nhập |
| **Tác nhân** | Khách hàng, Admin, Staff |
| **Mô tả** | Xác thực danh tính người dùng để truy cập vào các chức năng được phân quyền. |
| **Sự kiện kích hoạt** | Người dùng mở ứng dụng hoặc truy cập chức năng yêu cầu xác thực. |
| **Tiền điều kiện** | Đã có tài khoản được kích hoạt trên hệ thống. |
| **Hậu điều kiện** | Người dùng nhận được JWT Token và truy cập được Homepage/Dashboard. |

#### Luồng sự kiện chính

| # | Thực hiện bởi | Hành động |
|---|---|---|
| 1 | Người dùng | Nhập Email và Mật khẩu vào form đăng nhập, nhấn "Đăng nhập". |
| 2 | Hệ thống | Gửi yêu cầu xác thực đến Auth Service. |
| 3 | Hệ thống | Kiểm tra thông tin đăng nhập trong Database. |
| 4 | Hệ thống | So khớp mật khẩu (đã mã hóa). |
| 5 | Hệ thống | Tạo JWT Token (chứa UserID, Roles) và Refresh Token. |
| 6 | Hệ thống | Trả về Token và thông tin cơ bản của User. |
| 7 | Người dùng | Nhận thông báo thành công và chuyển vào trang chủ. |

#### Luồng sự kiện thay thế

| # | Tình huống | Hành xử của hệ thống |
|---|---|---|
| 3a | Email không tồn tại | Thông báo "Tài khoản hoặc mật khẩu không chính xác". |
| 4a | Sai mật khẩu | Thông báo "Tài khoản hoặc mật khẩu không chính xác". |

### Biểu đồ tuần tự

```mermaid
sequenceDiagram
    autonumber
    actor U as Người dùng
    participant FE as Frontend
    participant Auth as Auth Service
    participant DB as Auth DB

    U->>FE: Nhập Email/Pass & Login
    FE->>Auth: POST /api/auth/login
    activate Auth
    Auth->>DB: Find User by Email
    alt User Not Found
        DB-->>Auth: Null
        Auth-->>FE: 401 Unauthorized
        FE-->>U: Lỗi đăng nhập
    else User Found
        DB-->>Auth: User Data
        Auth->>Auth: Check Password (BCrypt)
        alt Password Wrong
            Auth-->>FE: 401 Unauthorized
            FE-->>U: Lỗi đăng nhập
        else Password Valid
            Auth->>Auth: Generate JWT Token
            Auth-->>FE: 200 OK (JWT + Info)
            FE->>FE: Lưu Token (LocalStorage)
            FE-->>U: Chuyển hướng Dashboard
        end
    end
    deactivate Auth
```

---

## 3. UC_XemMenu: Xem danh sách món ăn

### Bảng mô tả chi tiết

| Thuộc tính | Chi tiết |
| :--- | :--- |
| **Mã Use case** | `UC_XemMenu` |
| **Tên Use case** | Xem và tìm kiếm món ăn |
| **Tác nhân** | Khách hàng, Khách vãng lai |
| **Mô tả** | Cho phép người dùng xem danh sách món ăn, lọc theo danh mục hoặc tìm kiếm theo tên. |
| **Sự kiện kích hoạt** | Người dùng truy cập trang chủ hoặc trang thực đơn. |
| **Tiền điều kiện** | Hệ thống Menu Service đang hoạt động. |
| **Hậu điều kiện** | Danh sách món ăn được hiển thị. |

#### Luồng sự kiện chính

| # | Thực hiện bởi | Hành động |
|---|---|---|
| 1 | Người dùng | Truy cập vào màn hình "Thực đơn". |
| 2 | Hệ thống | Gửi yêu cầu lấy danh sách danh mục (Categories) và món ăn (Products). |
| 3 | Hệ thống | Truy vấn dữ liệu từ Menu Database. |
| 4 | Hệ thống | Trả về danh sách món ăn, hình ảnh, giá. |
| 5 | Người dùng | Xem danh sách, có thể chọn bộ lọc Category hoặc gõ từ khóa tìm kiếm. |
| 6 | Hệ thống | Cập nhật danh sách hiển thị theo điều kiện lọc/tìm kiếm. |

### Biểu đồ tuần tự

```mermaid
sequenceDiagram
    autonumber
    actor U as Khách hàng
    participant FE as Frontend
    participant GW as API Gateway
    participant Menu as Menu Service
    participant DB as Menu DB

    U->>FE: Truy cập Menu Page
    par Get Categories
        FE->>GW: GET /api/categories
        GW->>Menu: Forward
        Menu->>DB: Find All Categories
        DB-->>Menu: List Categories
        Menu-->>FE: JSON Categories
    and Get Products
        FE->>GW: GET /api/menu?page=0&size=10
        GW->>Menu: Forward
        Menu->>DB: Find Products (Pagination)
        DB-->>Menu: List Products
        Menu-->>FE: JSON Products
    end
    FE-->>U: Hiển thị giao diện Grid món ăn
    
    opt Tìm kiếm
        U->>FE: Nhập từ khóa "Pizza"
        FE->>GW: GET /api/menu/search?q=Pizza
        GW->>Menu: Search Request
        Menu->>DB: Query LIKE %Pizza%
        DB-->>Menu: Result List
        Menu-->>FE: Updated List
        FE-->>U: Cập nhật giao diện
    end
```

---

## 4. UC_DatHang: Đặt món (Tạo đơn hàng)

### Bảng mô tả chi tiết

| Thuộc tính | Chi tiết |
| :--- | :--- |
| **Mã Use case** | `UC_DatHang` |
| **Tên Use case** | Đặt hàng (Checkout) |
| **Tác nhân** | Khách hàng (Đã đăng nhập) |
| **Mô tả** | Người dùng chọn món, xác nhận giỏ hàng và tạo đơn hàng mới. |
| **Sự kiện kích hoạt** | Người dùng nhấn nút "Đặt hàng" hoặc "Thanh toán" từ giỏ hàng. |
| **Tiền điều kiện** | Giỏ hàng không rỗng, người dùng đã đăng nhập. |
| **Hậu điều kiện** | Đơn hàng được tạo ở trạng thái PENDING, tồn kho được giữ (reserved). |

#### Luồng sự kiện chính

| # | Thực hiện bởi | Hành động |
|---|---|---|
| 1 | Người dùng | Xem lại giỏ hàng, chọn địa chỉ giao hàng và nhấn "Xác nhận đặt hàng". |
| 2 | Hệ thống | Validate thông tin đơn hàng (Sản phẩm, số lượng, giá). |
| 3 | Hệ thống | Gọi Order Service để tạo đơn hàng (Status: PENDING). |
| 4 | Hệ thống | Order Service publish sự kiện `order.created`. |
| 5 | Hệ thống | Inventory Service lắng nghe, kiểm tra & trừ tồn kho. |
| 6 | Hệ thống | Notification Service lắng nghe, gửi email xác nhận cho khách. |
| 7 | Hệ thống | Trả về mã đơn hàng và chuyển hướng sang trang thanh toán. |

#### Luồng sự kiện thay thế

| # | Tình huống | Hành xử của hệ thống |
|---|---|---|
| 5a | Hết hàng | Inventory Service báo lỗi -> Order Service cập nhật trạng thái CANCELLED -> Thông báo cho người dùng "Món ăn đã hết". |

### Biểu đồ tuần tự

```mermaid
sequenceDiagram
    autonumber
    actor U as Khách hàng
    participant FE as Frontend
    participant Order as Order Service
    participant Inv as Inventory Service
    participant MQ as RabbitMQ
    participant Noti as Notification Service

    U->>FE: Click "Đặt hàng"
    FE->>Order: POST /api/orders (Items, Address...)
    activate Order
    Order->>Order: Create Order (State=PENDING)
    Order->>MQ: Publish "order.created"
    Order-->>FE: 201 Created (OrderID)
    deactivate Order
    
    FE-->>U: Chuyển sang trang Thanh toán
    
    par Inventory Process
        MQ->>Inv: Consume "order.created"
        activate Inv
        Inv->>Inv: Check & Deduct Stock
        alt Out of Stock
            Inv->>MQ: Publish "stock.failed"
            MQ->>Order: Update Order -> CANCELLED
        else Success
            Inv->>MQ: Publish "stock.succeed"
        end
        deactivate Inv
    and Notification
        MQ->>Noti: Consume "order.created"
        activate Noti
        Noti->>U: Gửi Email "Xác nhận đơn hàng"
        deactivate Noti
    end
```

---

## 5. UC_ThanhToan: Thanh toán đơn hàng

### Bảng mô tả chi tiết

| Thuộc tính | Chi tiết |
| :--- | :--- |
| **Mã Use case** | `UC_ThanhToan` |
| **Tên Use case** | Thanh toán trực tuyến |
| **Tác nhân** | Khách hàng |
| **Mô tả** | Thực hiện thanh toán cho đơn hàng đã tạo thông qua cổng thanh toán (Mock/VNPay). |
| **Sự kiện kích hoạt** | Người dùng chọn phương thức thanh toán và nhấn "Thanh toán". |
| **Tiền điều kiện** | Đơn hàng tồn tại và ở trạng thái PENDING/CONFIRMED. |
| **Hậu điều kiện** | Giao dịch được ghi nhận, đơn hàng cập nhật trạng thái PAID/PREPARING. |

#### Luồng sự kiện chính

| # | Thực hiện bởi | Hành động |
|---|---|---|
| 1 | Người dùng | Chọn phương thức thanh toán (VD: Thẻ/QR) tại trang thanh toán. |
| 2 | Hệ thống | Payment Service tạo URL thanh toán (hoặc QR Code) từ 3rd Party Gateway. |
| 3 | Người dùng | Thực hiện quét mã/nhập thẻ trên cổng thanh toán. |
| 4 | Cổng TT | Xử lý giao dịch và gọi Webhook về Payment Service (IPN). |
| 5 | Hệ thống | Payment Service xác nhận tiền về, lưu Transaction Log. |
| 6 | Hệ thống | Payment Service gọi Order Service cập nhật trạng thái đơn -> PREPARING. |
| 7 | Hệ thống | Thông báo "Thanh toán thành công" cho người dùng. |

### Biểu đồ tuần tự

```mermaid
sequenceDiagram
    autonumber
    actor U as Khách hàng
    participant FE as Frontend
    participant Pay as Payment Service
    participant TP as SePay/VNPay
    participant Order as Order Service

    U->>FE: Chọn "Thanh toán ngay"
    FE->>Pay: POST /api/payment/create_payment_url
    Pay->>TP: Request Payment URL
    TP-->>Pay: Payment URL
    Pay-->>FE: Return URL
    FE-->>U: Redirect to Payment Page (Webview/NewTab)
    
    U->>TP: Nhập thẻ/Quét QR & Confirm
    TP-->>U: Success & Redirect back
    
    par Webhook Processing
        TP->>Pay: Webhook/IPN: Payment Success
        activate Pay
        Pay->>Pay: Validate Signature & Save Log
        Pay->>Order: PUT /orders/{id}/status (PREPARING)
        Pay-->>TP: 200 OK (Ack)
        deactivate Pay
    end
```

---

## 6. UC_QuanLyDon: Xử lý đơn hàng (Staff)

### Bảng mô tả chi tiết

| Thuộc tính | Chi tiết |
| :--- | :--- |
| **Mã Use case** | `UC_QuanLyDon` |
| **Tên Use case** | Cập nhật trạng thái đơn hàng |
| **Tác nhân** | Nhân viên (Staff/Admin) |
| **Mô tả** | Nhân viên bếp/quản lý cập nhật tiến độ đơn hàng (Đang chuẩn bị -> Đã xong -> Giao hàng). |
| **Sự kiện kích hoạt** | Nhân viên thao tác trên Admin Dashboard. |
| **Tiền điều kiện** | Nhân viên đã đăng nhập với quyền STAFF. |
| **Hậu điều kiện** | Trạng thái đơn hàng thay đổi, khách hàng nhận được thông báo cập nhật. |

#### Luồng sự kiện chính

| # | Thực hiện bởi | Hành động |
|---|---|---|
| 1 | Nhân viên | Xem danh sách đơn hàng "Chờ xử lý" (PREPARING). |
| 2 | Nhân viên | Sau khi nấu xong, nhấn nút chuyển trạng thái "Đã xong/Chờ giao" (DELIVERING). |
| 3 | Hệ thống | Cập nhật trạng thái trong Database. |
| 4 | Hệ thống | Gửi thông báo realtime (qua WebSocket hoặc RabbitMQ -> Notification) tới khách hàng. |
| 5 | Khách hàng | Nhận được thông báo "Món ăn của bạn đang được giao". |

### Biểu đồ tuần tự

```mermaid
sequenceDiagram
    autonumber
    actor S as Nhân viên
    participant AdminFE as Admin Dashboard
    participant Order as Order Service
    participant MQ as RabbitMQ (Notification)
    actor C as Khách hàng

    S->>AdminFE: Xem danh sách đơn
    AdminFE->>Order: GET /api/orders?status=PREPARING
    Order-->>AdminFE: List Orders
    
    S->>AdminFE: Click "Giao hàng" (Order #123)
    AdminFE->>Order: PATCH /api/orders/123/status (body: DELIVERING)
    activate Order
    Order->>Order: Update DB Status
    Order->>MQ: Publish event "order.status_changed"
    Order-->>AdminFE: 200 OK
    deactivate Order
    
    AdminFE-->>S: Cập nhật UI thành công
    
    MQ->>C: Push Notification: "Đơn #123 đang giao!"
```
