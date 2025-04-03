package com.project.ecommerce.domain.cart.repository;

import com.project.ecommerce.domain.cart.entity.Cart;
import com.project.ecommerce.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}
