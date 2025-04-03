package com.project.ecommerce.domain.cart;

import com.project.ecommerce.domain.cart.dto.CartDto;
import com.project.ecommerce.domain.cart.service.CartService;
import com.project.ecommerce.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.project.ecommerce.common.utils.ResponseUtil.createSuccessResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        CartDto.CartResponse response = cartService.getCart(userId);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addItemToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CartDto.AddItemRequest request
    ) {
        Long userId = ((User) userDetails).getId();
        CartDto.CartResponse response = cartService.addItemToCart(userId, request);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PatchMapping("/items")
    public ResponseEntity<Map<String, Object>> updateItemQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CartDto.UpdateItemQuantityRequest request
    ) {
        Long userId = ((User) userDetails).getId();
        CartDto.CartResponse response = cartService.updateItemQuantity(userId, request);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteItemFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long cartItemId
    ) {
        Long userId = ((User) userDetails).getId();
        CartDto.CartResponse response = cartService.deleteItemFromCart(userId, cartItemId);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((User) userDetails).getId();
        cartService.clearCart(userId);

        return ResponseEntity.ok(createSuccessResponse(null));
    }
}
