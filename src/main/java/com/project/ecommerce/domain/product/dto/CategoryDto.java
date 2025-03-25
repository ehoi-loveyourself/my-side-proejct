package com.project.ecommerce.domain.product.dto;

import com.project.ecommerce.domain.product.entity.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

public class CategoryDto {

    @Getter
    @Builder
    public static class CategoryResponse {
        private Long categoryId;
        private String name;
        private String description;
        private Long parentCategoryId;

        public static CategoryResponse of(Category category) {
            return CategoryResponse.builder()
                    .categoryId(category.getId())
                    .name(category.getName())
                    .description(category.getDescription())
                    .parentCategoryId(category.getId())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CategoryRegisterRequest {

        @NotBlank(message = "카테고리명은 필수입니다.")
        private String name;

        private String description;

        private Long parentCategoryId; // null이면 최상위 카테고리
    }
}