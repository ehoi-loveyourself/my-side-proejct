package com.project.ecommerce.domain.cart.dto;

import com.project.ecommerce.domain.product.dto.ProductDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

public class CartItemDto {

    @Getter
    @Builder
    public static class CartItemResponse {
        private Long cartItemId;
        private ProductDto.ProductSimpleResponse product;
        private int quantity;
        private BigDecimal totalPrice;
        private BigDecimal pricePerProduct;
    }
}
