package com.project.ecommerce.domain.cart.service;

import com.project.ecommerce.common.exception.*;
import com.project.ecommerce.domain.cart.dto.CartDto;
import com.project.ecommerce.domain.cart.entity.Cart;
import com.project.ecommerce.domain.cart.entity.CartItem;
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
//                .orElse(null); // as-is 그냥 null을 리턴
                .orElseGet(() -> Cart.builder()
                        .user(user)
                        .build()); // to-be 빈 장바구니를 만들어서 리턴
        // 장바구니가 없다고 에러를 날릴 필요가 없음, 그냥 빈 바구니를 리턴하면 됨

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
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                                    .user(user)
                                            .build();

                    return cartRepository.save(newCart);
                });

        // 카트에 cartItems에 add 한다.
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductException(ProductErrorMessages.NOT_FOUND_PRODUCT, HttpStatus.NOT_FOUND));

        cart.addItems(product, request.getQuantity());

        return CartDto.CartResponse.of(cart);
    }

    @Override
    public CartDto.CartResponse updateItemQuantity(Long userId, CartDto.UpdateItemQuantityRequest request) {
        // 유저를 찾고
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        // 카트를 찾아서
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CartException(CartItemErrorMessages.EMPTY_CART, HttpStatus.BAD_REQUEST));

        cart.updateQuantity(request.getCartItemId(), request.getQuantity());

        return CartDto.CartResponse.of(cart);
    }

    @Override
    public CartDto.CartResponse deleteItemFromCart(Long userId, Long cartItemId) {
        // 유저를 찾고
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        // 카트를 찾아서
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            return CartDto.CartResponse.empty();
        }

        cart.deleteItems(cartItemId);

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

        cart.getCartItems().forEach(CartItem::removeFromCart);// 각각 아이템에서도 장바구니 연관관계 지우기
        cart.getCartItems().clear(); // 장바구니에서도 아이템 리스트 다 지우고 -> 연관관계 확실히 양쪽 다 끊어주기
    }
}