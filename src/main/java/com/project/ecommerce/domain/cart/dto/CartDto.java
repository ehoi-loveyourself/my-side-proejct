package com.project.ecommerce.domain.cart.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

public class CartDto {

    @Getter
    @Builder
    public static class CartResponse {
        private Long cartId;

        // 장바구니에 담은 아이템을 리스트로 리턴
        private List<CartItemDto.CartItemResponse> items;

        private int totalItems;
        private BigDecimal totalPrice;

        // user 자체를 반납하는 거보다는 user id만 반납하는게 좋을까?
//        private UserDto.UserResponse user;
    }
}
