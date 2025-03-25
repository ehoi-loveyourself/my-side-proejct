package com.project.ecommerce.domain.product.controller;

import com.project.ecommerce.domain.product.dto.CategoryDto;
import com.project.ecommerce.domain.product.service.CategoryService;
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
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCategoryList(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<CategoryDto.CategoryResponse> response = categoryService.getCategoryList(pageable);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Map<String, Object>> getCategory(@PathVariable Long categoryId) {
        CategoryDto.CategoryResponse response = categoryService.getCategory(categoryId);

        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registerCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CategoryDto.CategoryRegisterRequest request
    ) {
        Long sellerId = ((User) userDetails).getId();
        CategoryDto.CategoryResponse response = categoryService.registerCategory(request, sellerId);

        return ResponseEntity.ok(createSuccessResponse(response));
    }
}
