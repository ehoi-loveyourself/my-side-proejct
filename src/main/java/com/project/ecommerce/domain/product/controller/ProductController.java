package com.project.ecommerce.domain.product.controller;

import com.project.ecommerce.domain.product.dto.ProductDto;
import com.project.ecommerce.domain.product.service.ProductService;
import com.project.ecommerce.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.project.ecommerce.common.utils.ResponseUtil.createSuccessResponse;

@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@RestController
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProductList(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductDto.ProductSimpleResponse> response = productService.getProductList(pageable);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable Long productId) {
        ProductDto.ProductResponse response = productService.getProduct(productId);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductDto.ProductSimpleResponse> response = productService.searchProduct(keyword, pageable);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registerProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProductDto.ProductRegisterRequest request
    ) {
        Long sellerId = ((User) userDetails).getId();
        ProductDto.ProductResponse response = productService.registerProduct(request, sellerId);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody ProductDto.ProductUpdateRequest request
    ) {
        Long sellerId = ((User) userDetails).getId();
        ProductDto.ProductResponse response = productService.updateProduct(productId, request, sellerId);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<Map<String, Object>> updateStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody ProductDto.StockUpdateRequest request
    ) {
        Long sellerId = ((User) userDetails).getId();
        ProductDto.ProductResponse response = productService.updateStock(request, productId, sellerId);

        return ResponseEntity.ok(createSuccessResponse(response));
    }
}