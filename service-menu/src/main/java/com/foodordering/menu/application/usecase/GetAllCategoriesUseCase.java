package com.foodordering.menu.application.usecase;

import com.foodordering.menu.application.dto.CategoryDto;
import com.foodordering.menu.domain.model.Category;
import com.foodordering.menu.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case: Lấy tất cả categories
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetAllCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> execute(boolean activeOnly) {
        log.info("[GET_ALL_CATEGORIES] Fetching categories, activeOnly: {}", activeOnly);
        
        List<Category> categories = activeOnly 
            ? categoryRepository.findAllActive()
            : categoryRepository.findAll();
        
        log.info("[GET_ALL_CATEGORIES] Found {} categories", categories.size());
        
        return categories.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
