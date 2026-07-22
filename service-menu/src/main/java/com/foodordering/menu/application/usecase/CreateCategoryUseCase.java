package com.foodordering.menu.application.usecase;

import com.foodordering.menu.application.dto.CreateCategoryDto;
import com.foodordering.menu.application.dto.CategoryDto;
import com.foodordering.menu.domain.model.Category;
import com.foodordering.menu.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Tạo category mới
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDto execute(CreateCategoryDto request) {
        log.info("[CREATE_CATEGORY] Creating category: {}", request.getName());
        
        // Kiểm tra tên đã tồn tại chưa
        if (categoryRepository.existsByName(request.getName())) {
            log.error("[CREATE_CATEGORY] Category name already exists: {}", request.getName());
            throw new RuntimeException("Tên category đã tồn tại");
        }
        
        // Tạo category entity
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .active(true)
                .build();
        
        // Lưu vào database
        Category savedCategory = categoryRepository.save(category);
        log.info("[CREATE_CATEGORY] Category created with ID: {}", savedCategory.getId());
        
        return toDto(savedCategory);
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
