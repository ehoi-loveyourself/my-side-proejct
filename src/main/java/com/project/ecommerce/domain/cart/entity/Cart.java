package com.project.ecommerce.domain.cart.entity;

import com.project.ecommerce.domain.common.BaseEntity;
import com.project.ecommerce.domain.product.entity.Product;
import com.project.ecommerce.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        CartItem cartItem = CartItem.builder()
                .cart(this)
                .product(product)
                .quantity(quantity)
                .build();

        // 같은 아이템이 있다면 수량만 증가시키는 게 좋을 것 같음
        Optional<CartItem> existingItem = this.cartItems.stream()
                .filter(item -> item.getProduct().equals(product))
                .findFirst();

        existingItem.ifPresentOrElse(
                item -> item.increaseQuantity(quantity),
                () -> this.cartItems.add(cartItem)
        );
    }
}
