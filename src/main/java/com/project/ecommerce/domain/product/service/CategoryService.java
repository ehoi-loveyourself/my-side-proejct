package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.domain.product.dto.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    Page<CategoryDto.CategoryResponse> getCategoryList(Pageable pageable);

    CategoryDto.CategoryResponse getCategory(Long categoryId);

    CategoryDto.CategoryResponse registerCategory(CategoryDto.CategoryRegisterRequest request, Long sellerId);

    CategoryDto.CategoryResponse updateCategory(CategoryDto.CategoryUpdateRequest request, Long sellerId, Long categoryId);

    void deleteCategory(Long categoryId, Long sellerId);
}
