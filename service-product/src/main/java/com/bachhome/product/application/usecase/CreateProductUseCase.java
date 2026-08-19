package com.bachhome.product.application.usecase;

import com.bachhome.product.application.dto.CreateProductDto;
import com.bachhome.product.application.dto.ProductDto;
import com.bachhome.product.domain.model.Category;
import com.bachhome.product.domain.model.Product;
import com.bachhome.product.domain.repository.CategoryRepository;
import com.bachhome.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Tạo sản phẩm mới
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductDto execute(CreateProductDto request) {
        log.info("[CREATE_PRODUCT] Creating sản phẩm: {}", request.getName());
        
        // Kiểm tra category có tồn tại không
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.error("[CREATE_PRODUCT] Category not found: {}", request.getCategoryId());
                    return new RuntimeException("Category không tồn tại");
                });
        
        // Tạo sản phẩm entity
        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .brand(request.getBrand())
                .origin(request.getOrigin())
                .warrantyMonths(request.getWarrantyMonths())
                .material(request.getMaterial())
                .specification(request.getSpecification())
                .available(true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        
        // Lưu vào database
        Product savedItem = productRepository.save(product);
        log.info("[CREATE_PRODUCT] Sản phẩm created with ID: {}", savedItem.getId());
        
        return toDto(savedItem);
    }

    private ProductDto toDto(Product item) {
        return ProductDto.builder()
                .id(item.getId())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .available(item.getAvailable())
                .displayOrder(item.getDisplayOrder())
                .averageRating(item.getAverageRating())
                .totalReviews(item.getTotalReviews())
                .brand(item.getBrand())
                .origin(item.getOrigin())
                .warrantyMonths(item.getWarrantyMonths())
                .material(item.getMaterial())
                .specification(item.getSpecification())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
