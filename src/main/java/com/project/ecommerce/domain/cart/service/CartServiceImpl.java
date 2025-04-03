package com.project.ecommerce.domain.cart.service;

import com.project.ecommerce.common.exception.ProductErrorMessages;
import com.project.ecommerce.common.exception.ProductException;
import com.project.ecommerce.common.exception.UserErrorMessages;
import com.project.ecommerce.common.exception.UserException;
import com.project.ecommerce.domain.cart.dto.CartDto;
import com.project.ecommerce.domain.cart.entity.Cart;
import com.project.ecommerce.domain.cart.repository.CartRepository;
import com.project.ecommerce.domain.product.entity.Product;
import com.project.ecommerce.domain.product.repository.ProductRepository;
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
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public CartDto.CartResponse getCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        Cart cart = cartRepository.findByUser(user)
                .orElse(null); // 장바구니가 없다고 에러를 날릴 필요가 없음, 그냥 빈 바구니를 리턴하면 됨

        if (cart == null || cart.getCartItems().isEmpty()) {
            return CartDto.CartResponse.empty();
        }

        return CartDto.CartResponse.of(cart);
    }

    @Override
    public CartDto.CartResponse addItemToCart(Long userId, CartDto.AddItemRequest request) {
        // 유저를 찾고
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        // 카트를 찾는다. 카트가 없으면 카트를 생성해야 한다.
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .user(user)
                        .build());

        // 카트에 cartItems에 add 한다.
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductException(ProductErrorMessages.NOT_FOUND_PRODUCT, HttpStatus.NOT_FOUND));

        cart.addItems(product, request.getQuantity());

        return CartDto.CartResponse.of(cart);
    }

    @Override
    public CartDto.CartResponse updateItemQuantity(Long userId, CartDto.UpdateItemQuantityRequest request) {
        return null;
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