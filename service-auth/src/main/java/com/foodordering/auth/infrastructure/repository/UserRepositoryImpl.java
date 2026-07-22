package com.foodordering.auth.infrastructure.repository;

import com.foodordering.auth.domain.model.User;
import com.foodordering.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * User Repository Implementation (Infrastructure Layer)
 * Adapter pattern: chuyển đổi giữa Domain Repository Interface và JPA Repository
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public java.util.List<User> findAll() {
        return jpaUserRepository.findAll();
    }
}
