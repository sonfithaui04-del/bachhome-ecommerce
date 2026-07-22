# 🔐 Admin User Setup Guide

## ⚠️ Vấn Đề: 403 Forbidden khi login Admin Panel

**Nguyên nhân:** Chưa có user với role ADMIN trong database.

---

## ✅ Giải Pháp 1: Tạo Admin User (Nhanh nhất)

### Bước 1: Tạo user qua Customer App
1. Mở http://localhost:3001
2. Click **Register**
3. Điền thông tin:
   - Full Name: `Admin User`
   - Email: `admin@foodorder.com`
   - Phone: `0123456789`
   - Password: `admin123`
4. Click **Create Account**

### Bước 2: Kết nối Database và Update Role

#### **Cách 1: Dùng Docker Exec (Khuyên dùng)**

```bash
# Kết nối vào PostgreSQL container của service-auth
docker exec -it postgres-auth psql -U postgres -d authdb

# Kiểm tra user vừa tạo
SELECT id, full_name, email, role FROM users;

# Update role thành ADMIN cho user vừa tạo (thay USER_ID bằng id thực tế)
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@foodorder.com';

# Kiểm tra lại
SELECT id, full_name, email, role FROM users;

# Thoát
\q
```

#### **Cách 2: Dùng DBeaver/pgAdmin**

**Connection Details:**
- Host: `localhost`
- Port: `5432`
- Database: `authdb`
- Username: `postgres`
- Password: `postgres`

**SQL Command:**
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@foodorder.com';
```

### Bước 3: Login vào Admin Panel

1. Mở http://localhost:3002
2. Login với:
   - Email: `admin@foodorder.com`
   - Password: `admin123`
3. Sau khi update role, bạn sẽ login được! ✅

---

## ✅ Giải Pháp 2: Thêm Admin Registration Endpoint (Backend)

### Nếu muốn tự động tạo admin từ đầu:

Thêm endpoint mới vào **AuthController.java**:

```java
@PostMapping("/register-admin")
public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request) {
    // Validate admin creation (có thể thêm secret key)
    User user = new User();
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setFullName(request.getFullName());
    user.setPhoneNumber(request.getPhoneNumber());
    user.setRole(UserRole.ADMIN); // ← Set role ADMIN
    user.setStatus(UserStatus.ACTIVE);
    
    User savedUser = userRepository.save(user);
    
    String token = jwtService.generateToken(savedUser.getEmail());
    
    return ResponseEntity.ok(new AuthResponse(
        token,
        savedUser.getId(),
        savedUser.getEmail(),
        savedUser.getFullName(),
        savedUser.getRole().name()
    ));
}
```

Sau đó dùng curl hoặc Postman:

```bash
curl -X POST http://localhost:8080/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@foodorder.com",
    "password": "admin123",
    "fullName": "Admin User",
    "phoneNumber": "0123456789"
  }'
```

---

## ✅ Giải Pháp 3: Initial Data Setup (Khuyên dùng cho Production)

### Tạo AdminDataLoader.java

Tạo file: `service-auth/src/main/java/com/foodordering/auth/infrastructure/config/AdminDataLoader.java`

```java
package com.foodordering.auth.infrastructure.config;

import com.foodordering.auth.domain.model.User;
import com.foodordering.auth.domain.model.UserRole;
import com.foodordering.auth.domain.model.UserStatus;
import com.foodordering.auth.infrastructure.persistence.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminDataLoader {

    @Bean
    CommandLineRunner initAdminUser(UserRepository userRepository, 
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin exists
            if (userRepository.findByEmail("admin@foodorder.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@foodorder.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("System Administrator");
                admin.setPhoneNumber("0123456789");
                admin.setRole(UserRole.ADMIN);
                admin.setStatus(UserStatus.ACTIVE);
                
                userRepository.save(admin);
                System.out.println("✅ Admin user created: admin@foodorder.com / admin123");
            }
        };
    }
}
```

Sau đó rebuild và restart service-auth:

```bash
cd service-auth
mvn clean package -DskipTests
docker-compose up -d --build service-auth
```

---

## 🎯 Khuyến Nghị

**Cho Development (Nhanh nhất):**
1. ✅ Dùng **Giải Pháp 1** - Tạo user qua app, update role qua SQL

**Cho Production:**
1. ✅ Dùng **Giải Pháp 3** - AdminDataLoader tự động tạo admin khi start

---

## 🔍 Kiểm Tra Backend Services

Trước khi login, đảm bảo services đang chạy:

```bash
# Kiểm tra containers
docker-compose ps

# Kiểm tra service-auth logs
docker logs service-auth

# Test API Gateway
curl http://localhost:8080/api/auth/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456"}'
```

---

## ✅ Sau Khi Fix

**Test Admin Login:**
1. Mở http://localhost:3002
2. Login:
   ```
   Email: admin@foodorder.com
   Password: admin123
   ```
3. Nếu role = ADMIN → Login thành công! 🎉
4. Nếu role ≠ ADMIN → Vẫn bị 403 (cần update role)

---

## 📝 Test Credentials

**Admin Account:**
- Email: `admin@foodorder.com`
- Password: `admin123`
- Role: `ADMIN` ⚠️ (Phải set trong database)

**Customer Account (để test):**
- Email: `customer@test.com`
- Password: `123456`
- Role: `CUSTOMER`

---

## 🎯 Summary

1. **Tạo user** qua http://localhost:3001/register
2. **Update role** qua SQL: `UPDATE users SET role = 'ADMIN' WHERE email = 'admin@foodorder.com';`
3. **Login admin** tại http://localhost:3002

**Lỗi 403 = Chưa có ADMIN role!** ⚠️
