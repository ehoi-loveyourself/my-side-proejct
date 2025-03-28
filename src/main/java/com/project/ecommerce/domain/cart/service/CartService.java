package com.project.ecommerce.domain.cart.service;

import com.project.ecommerce.domain.cart.dto.CartDto;

public interface CartService {
    // 장바구니 조회
    CartDto.CartResponse getCart(Long userId);

    // 장바구니에 아이템 담기

    // 장바구니의 아이템 수량 변경

    // 장바구니에서 아이템 삭제
}
