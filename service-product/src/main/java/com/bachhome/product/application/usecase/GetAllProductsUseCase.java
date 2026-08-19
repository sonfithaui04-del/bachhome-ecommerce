package com.bachhome.product.application.usecase;

import com.bachhome.product.application.dto.ProductDto;
import com.bachhome.product.domain.model.Product;
import com.bachhome.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case: Lấy tất cả sản phẩm
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetAllProductsUseCase {

    private final ProductRepository productRepository;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ProductDto> execute(boolean availableOnly) {
        log.info("[GET_ALL_PRODUCTS] Fetching sản phẩm, availableOnly: {}", availableOnly);
        
        List<Product> products = availableOnly 
            ? productRepository.findAllAvailable()
            : productRepository.findAll();
        
        log.info("[GET_ALL_PRODUCTS] Found {} sản phẩm", products.size());
        
        return products.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ProductDto toDto(Product item) {
        ProductDto.ProductDtoBuilder builder = ProductDto.builder()
                .id(item.getId())
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
                .updatedAt(item.getUpdatedAt());

        if (item.getCategory() != null) {
            builder.categoryId(item.getCategory().getId())
                   .categoryName(item.getCategory().getName());
        }

        return builder.build();
    }
}
