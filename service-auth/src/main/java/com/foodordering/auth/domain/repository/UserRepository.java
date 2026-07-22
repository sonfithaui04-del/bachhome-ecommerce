package com.foodordering.auth.domain.repository;

import com.foodordering.auth.domain.model.User;
import java.util.Optional;

/**
 * User Repository Interface (Domain Layer)
 * Đây là contract, implementation sẽ ở Infrastructure layer
 */
public interface UserRepository {
    
    /**
     * Tìm user theo email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Kiểm tra email đã tồn tại chưa
     */
    boolean existsByEmail(String email);
    
    /**
     * Lưu user mới hoặc cập nhật
     */
    User save(User user);
    
    /**
     * Tìm user theo ID
     */
    /**
     * Tìm user theo ID
     */
    Optional<User> findById(Long id);

    /**
     * Lấy danh sách tất cả user
     */
    java.util.List<User> findAll();
}
