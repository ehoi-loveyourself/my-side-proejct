package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.domain.product.dto.CategoryDto;
import com.project.ecommerce.domain.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Page<CategoryDto.CategoryResponse> getCategoryList(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(CategoryDto.CategoryResponse::of);
    }
}
