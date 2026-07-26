package com.bachhome.product.interfaces.controller;

import com.bachhome.product.application.dto.CreateProductDto;
import com.bachhome.product.application.dto.ProductDto;
import com.bachhome.product.application.usecase.CreateProductUseCase;
import com.bachhome.product.application.usecase.GetAllProductsUseCase;
import com.bachhome.product.domain.model.Category;
import com.bachhome.product.domain.model.Product;
import com.bachhome.product.domain.repository.CategoryRepository;
import com.bachhome.product.domain.repository.ProductRepository;
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
 * Product Controller - REST API cho sản phẩm
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product", description = "API quản lý sản phẩm")
public class ProductController {

    private final GetAllProductsUseCase getAllProductsUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Lấy tất cả sản phẩm
     * GET /products
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm", description = "Lấy tất cả sản phẩm hoặc chỉ món available")
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "true") boolean availableOnly) {
        log.info("[PRODUCT-CONTROLLER] Get all sản phẩm, availableOnly: {}", availableOnly);
        List<ProductDto> products = getAllProductsUseCase.execute(availableOnly);
        return ResponseEntity.ok(products);
    }

    /**
     * Tạo sản phẩm mới
     * POST /products
     */
    @PostMapping
    @Operation(summary = "Tạo sản phẩm mới", description = "Thêm sản phẩm mới vào danh mục")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductDto request) {
        log.info("[PRODUCT-CONTROLLER] Create sản phẩm: {}", request.getName());
        ProductDto product = createProductUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Cập nhật sản phẩm
     * PUT /products/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật sản phẩm", description = "Cập nhật thông tin sản phẩm")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductDto request) {
        log.info("[PRODUCT-CONTROLLER] Update sản phẩm: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
        
        if (request.getAvailable() != null) {
            product.setAvailable(request.getAvailable());
        }
        
        if (request.getDisplayOrder() != null) {
            product.setDisplayOrder(request.getDisplayOrder());
        }
        
        Product updated = productRepository.save(product);
        return ResponseEntity.ok(convertToDto(updated));
    }

    /**
     * Xóa sản phẩm
     * DELETE /products/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa sản phẩm", description = "Xóa sản phẩm khỏi danh mục")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("[PRODUCT-CONTROLLER] Delete sản phẩm: {}", id);
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Product Controller is running");
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setDisplayOrder(product.getDisplayOrder());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        dto.setAvailable(product.getAvailable() != null && product.getAvailable());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}
