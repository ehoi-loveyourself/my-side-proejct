package com.project.ecommerce.domain.product.dto;

import com.project.ecommerce.common.exception.ProductErrorMessages;
import com.project.ecommerce.domain.product.entity.Product;
import com.project.ecommerce.domain.product.entity.ProductCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

public class ProductDto {

    @Getter
    @Builder
    public static class ProductSimpleResponse {
        private Long productId;
        private String name;
        private BigDecimal price;
        private String mainImage;
        private String status;

        public static ProductSimpleResponse of(Product product) {
            String mainImage = product.getImageUrls() != null && !product.getImageUrls().isEmpty()
                    ? product.getImageUrls().get(0)
                    : null;

            return ProductSimpleResponse.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .mainImage(mainImage)
                    .status(product.getStatus().name())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProductResponse {
        private Long productId;
        private String name;
        private String description;
        private BigDecimal price;
        private int stock;
        private Long sellerId;
        private List<String> imageUrls;
        private String status;
        private List<CategoryDto.CategoryResponse> categories;

        public static ProductResponse of(Product product) {
            List<CategoryDto.CategoryResponse> categoryResponse = product.getProductCategories().stream()
                    .map(ProductCategory::getCategory)
                    .map(category -> CategoryDto.CategoryResponse.builder()
                            .categoryId(category.getId())
                            .name(category.getName())
                            .description(category.getDescription())
                            .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                            .build())
                    .toList();

            return ProductResponse.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .stock(product.getStock())
                    .sellerId(product.getSellerId())
                    .imageUrls(product.getImageUrls())
                    .status(product.getStatus().name())
                    .categories(categoryResponse)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProductRegisterRequest {

        @NotBlank(message = "상품 이름은 필수입니다")
        private String name;

        private String description;

        @NotNull(message = "가격은 필수입니다")
        @Min(value = 100, message = "가격은 100원 이상이어야 합니다")
        private BigDecimal price;

        @NotNull(message = "재고는 필수입니다")
        @Min(value = 0, message = ProductErrorMessages.STOCK_MUST_MORE_THAN_ZERO)
        private int stock;

        private List<String> imageUrls;

        @NotEmpty(message = "최소 하나의 카테고리는 필수입니다")
        private List<Long> categoryIds;
    }

    @Getter
    @Builder
    public static class ProductUpdateRequest {

        private String name;

        private String description;

        @Min(value = 100, message = "가격은 100원 이상이어야 합니다")
        private BigDecimal price;
    }

    @Builder
    @Getter
    public static class StockUpdateRequest {

        @Min(value = 0, message = ProductErrorMessages.STOCK_MUST_MORE_THAN_ZERO)
        private int stock;
    }
}