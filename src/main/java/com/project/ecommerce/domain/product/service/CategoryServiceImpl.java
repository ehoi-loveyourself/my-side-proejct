package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.common.exception.CategoryErrorMessages;
import com.project.ecommerce.common.exception.CategoryException;
import com.project.ecommerce.common.exception.UserErrorMessages;
import com.project.ecommerce.common.exception.UserException;
import com.project.ecommerce.domain.product.dto.CategoryDto;
import com.project.ecommerce.domain.product.entity.Category;
import com.project.ecommerce.domain.product.repository.CategoryRepository;
import com.project.ecommerce.domain.user.entity.User;
import com.project.ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public Page<CategoryDto.CategoryResponse> getCategoryList(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(CategoryDto.CategoryResponse::of);
    }

    @Override
    public CategoryDto.CategoryResponse getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryException(CategoryErrorMessages.NOT_FOUND_CATEGORY, HttpStatus.NOT_FOUND));

        return CategoryDto.CategoryResponse.of(category);
    }

    @Override
    public CategoryDto.CategoryResponse createCategory(CategoryDto.CategoryRegisterRequest request, Long sellerId) {
        // 판매자인지 검증
        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        user.checkSeller();

        // 카테고리명 중복 검사
        if (categoryRepository.existsByName(request.getName())) {
            throw new CategoryException(UserErrorMessages.ALREADY_EXIST, HttpStatus.BAD_REQUEST);
        }

        // todo: 그때 다빈님이 엔티티 만드는 것과 관련해서, 빌더로 할지, create 메서드를 static으로 할지 알려준 게 있었는데, 까먹었다.
        // 카테고리 생성
        Category parentyCategory = request.getParentCategoryId() == null ? null : categoryRepository.findById(request.getParentCategoryId())
                .orElseThrow(() -> new CategoryException(CategoryErrorMessages.NOT_FOUND_CATEGORY, HttpStatus.NOT_FOUND));

        Category newCategory = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parentCategory(parentyCategory)
                .build();

        Category savedCategory = categoryRepository.save(newCategory);

        return CategoryDto.CategoryResponse.of(savedCategory);
    }
}
