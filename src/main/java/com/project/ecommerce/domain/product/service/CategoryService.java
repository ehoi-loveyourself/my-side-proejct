package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.domain.product.dto.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    Page<CategoryDto.CategoryResponse> getCategoryList(Pageable pageable);
}
