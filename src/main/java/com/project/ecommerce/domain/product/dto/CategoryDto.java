package com.project.ecommerce.domain.product.dto;

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
    }
}