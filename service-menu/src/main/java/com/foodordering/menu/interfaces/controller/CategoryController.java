package com.foodordering.menu.interfaces.controller;

import com.foodordering.menu.application.dto.CategoryDto;
import com.foodordering.menu.application.dto.CreateCategoryDto;
import com.foodordering.menu.application.usecase.CreateCategoryUseCase;
import com.foodordering.menu.application.usecase.GetAllCategoriesUseCase;
import com.foodordering.menu.domain.model.Category;
import com.foodordering.menu.domain.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Category Controller - REST API for Categories
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "API quản lý danh mục món ăn")
public class CategoryController {

    private final GetAllCategoriesUseCase getAllCategoriesUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final CategoryRepository categoryRepository;

    /**
     * Lấy tất cả categories
     * GET /categories
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách categories", description = "Lấy tất cả categories hoặc chỉ active categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        log.info("[CATEGORY-CONTROLLER] Get all categories, activeOnly: {}", activeOnly);
        List<CategoryDto> categories = getAllCategoriesUseCase.execute(activeOnly);
        return ResponseEntity.ok(categories);
    }

    /**
     * Tạo category mới
     * POST /categories
     */
    @PostMapping
    @Operation(summary = "Tạo category mới", description = "Tạo danh mục món ăn mới")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CreateCategoryDto request) {
        log.info("[CATEGORY-CONTROLLER] Create category: {}", request.getName());
        CategoryDto category = createCategoryUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    /**
     * Cập nhật category
     * PUT /categories/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật category", description = "Cập nhật thông tin danh mục")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CreateCategoryDto request) {
        log.info("[CATEGORY-CONTROLLER] Update category: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        
        Category updated = categoryRepository.save(category);
        return ResponseEntity.ok(convertToDto(updated));
    }

    /**
     * Xóa category
     * DELETE /categories/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa category", description = "Xóa danh mục món ăn")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("[CATEGORY-CONTROLLER] Delete category: {}", id);
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Category Controller is running");
    }

    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setActive(category.getActive() != null && category.getActive());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
}
