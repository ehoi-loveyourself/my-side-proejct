package com.project.ecommerce.domain.cart.dto;

import com.project.ecommerce.domain.cart.entity.CartItem;
import com.project.ecommerce.domain.product.dto.ProductDto;
import com.project.ecommerce.domain.product.entity.Product;
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
        private BigDecimal pricePerProduct;
        private BigDecimal totalPrice;

        public static CartItemResponse of(CartItem cartItem) {
            Product itemProduct = cartItem.getProduct();
            int itemQuantity = cartItem.getQuantity();
            BigDecimal itemPrice = itemProduct.getPrice();

            return CartItemResponse.builder()
                    .cartItemId(cartItem.getId())
                    .product(ProductDto.ProductSimpleResponse.of(itemProduct))
                    .quantity(itemQuantity)
                    .pricePerProduct(itemPrice)
                    .totalPrice(itemPrice.multiply(BigDecimal.valueOf(itemQuantity)))
                    .build();
        }
    }
}
