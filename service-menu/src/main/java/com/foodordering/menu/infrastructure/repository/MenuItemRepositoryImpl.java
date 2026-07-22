package com.foodordering.menu.infrastructure.repository;

import com.foodordering.menu.domain.model.MenuItem;
import com.foodordering.menu.domain.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * MenuItem Repository Implementation (Adapter Pattern)
 */
@Component
@RequiredArgsConstructor
public class MenuItemRepositoryImpl implements MenuItemRepository {

    private final JpaMenuItemRepository jpaRepository;

    @Override
    public Optional<MenuItem> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<MenuItem> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<MenuItem> findAllAvailable() {
        return jpaRepository.findAllAvailable();
    }

    @Override
    public List<MenuItem> findByCategoryId(Long categoryId) {
        return jpaRepository.findByCategoryId(categoryId);
    }

    @Override
    public MenuItem save(MenuItem menuItem) {
        return jpaRepository.save(menuItem);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
