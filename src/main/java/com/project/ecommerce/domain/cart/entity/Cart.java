package com.project.ecommerce.domain.cart.entity;

import com.project.ecommerce.common.exception.CartException;
import com.project.ecommerce.common.exception.CartItemErrorMessages;
import com.project.ecommerce.domain.common.BaseEntity;
import com.project.ecommerce.domain.product.entity.Product;
import com.project.ecommerce.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @Builder
    public Cart(User user) {
        this.user = user;
    }

    //==연관관계 편의 메서드==//
    public void addItems(Product product, int quantity) {
        // 같은 아이템이 있다면 수량만 증가시키는 게 좋을 것 같음
        CartItem existingItem = this.cartItems.stream()
                .filter(item -> item.getProduct().equals(product))
                .findFirst()
                        .orElse(null);

        if (existingItem != null) {
            existingItem.increaseQuantity(quantity);
        } else {
            this.cartItems.add(CartItem.builder()
                    .cart(this)
                    .product(product)
                    .quantity(quantity)
                    .build());
        }
    }

    public void updateQuantity(Long cartItemId, int quantity) {
        // 수량을 변경할 장바구니 아이템을 찾는다. 없으면 에러, 있으면 수량만 변경
        CartItem existingItem = this.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new CartException(CartItemErrorMessages.NO_ITEM_FOR_UPDATE, HttpStatus.NOT_FOUND));

        existingItem.updateQuantity(quantity);
    }

    public void deleteItems(Long cartItemId) {
        CartItem cartItem = this.cartItems.stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new CartException(CartItemErrorMessages.NO_ITEM_FOR_DELETE, HttpStatus.NOT_FOUND));

        cartItem.removeFromCart(); // cartItem과 cart와의 연관관계를 끊으면 cartItem이 DB에서도 삭제됨
        this.cartItems.remove(cartItem);
    }
}
