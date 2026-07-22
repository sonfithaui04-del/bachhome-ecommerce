package com.foodordering.auth.infrastructure.repository;

import com.foodordering.auth.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * JPA Repository Implementation
 * Spring Data JPA sẽ tự động implement interface này
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
