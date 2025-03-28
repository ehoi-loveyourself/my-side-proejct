package com.project.ecommerce.domain.cart.service;

import com.project.ecommerce.domain.cart.dto.CartDto;

public interface CartService {
    // 장바구니 조회
    CartDto.CartResponse getCart(Long userId);

    // 장바구니에 아이템 담기
    CartDto.CartResponse addItemToCart(Long userId, CartDto.AddItemRequest request);

    // 장바구니의 아이템 수량 변경
    CartDto.CartResponse updateItemQuantity(Long userId, CartDto.UpdateItemQuantityRequest request);

    // 장바구니에서 아이템 삭제
    CartDto.CartResponse deleteItemFromCart(Long userId, Long productId);
}
