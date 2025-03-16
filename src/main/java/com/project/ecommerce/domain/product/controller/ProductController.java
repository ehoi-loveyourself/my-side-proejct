package com.project.ecommerce.domain.product.controller;

import com.project.ecommerce.domain.product.dto.ProductDto;
import com.project.ecommerce.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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

    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("data", data);

        return result;
    }
}