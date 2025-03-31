package com.project.ecommerce.domain.cart.service;

import com.project.ecommerce.common.exception.UserErrorMessages;
import com.project.ecommerce.common.exception.UserException;
import com.project.ecommerce.domain.cart.dto.CartDto;
import com.project.ecommerce.domain.cart.entity.Cart;
import com.project.ecommerce.domain.cart.repository.CartRepository;
import com.project.ecommerce.domain.user.entity.User;
import com.project.ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class CartServiceImpl implements CartService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Override
    @Transactional(readOnly = true)
    public CartDto.CartResponse getCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        Cart cart = cartRepository.findById(user.getId())
                .orElse(null); // 장바구니가 없다고 에러를 날릴 필요가 없음, 그냥 빈 바구니를 리턴하면 됨

        if (cart == null || cart.getCartItems().isEmpty()) {
            return CartDto.CartResponse.empty();
        }

        return CartDto.CartResponse.of(cart);
    }

    @Override
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        Cart cart = cartRepository.findById(user.getId())
                .orElse(null);

        if (cart == null || cart.getCartItems().isEmpty()) {
            return;
        }

        cart.getCartItems().clear();
    }
}