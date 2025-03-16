package com.project.ecommerce.domain.product.dto;

import com.project.ecommerce.domain.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

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
}