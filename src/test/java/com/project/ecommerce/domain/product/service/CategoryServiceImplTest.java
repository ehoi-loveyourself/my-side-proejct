package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.common.exception.CategoryErrorMessages;
import com.project.ecommerce.common.exception.CategoryException;
import com.project.ecommerce.domain.product.dto.CategoryDto;
import com.project.ecommerce.domain.product.entity.Category;
import com.project.ecommerce.domain.product.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = Category.builder()
                .name("카테고리1")
                .description("카테고리1 설명")
                .build();

        category2 = Category.builder()
                .name("카테고리2")
                .description("카테고리2 설명")
                .build();
    }

    @DisplayName("카테고리 목록 조회를 성공한다.")
    @Test
    void 카테고리_목록_조회_성공() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").descending());
        List<Category> categories = List.of(category1, category2);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        // when
        Page<CategoryDto.CategoryResponse> responses = categoryService.getCategoryList(pageable);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.getTotalElements()).isEqualTo(categories.size());
        assertThat(responses.getContent()).hasSize(2);

        verify(categoryRepository).findAll(pageable);
    }

    @DisplayName("카테고리 상세 조회를 성공한다.")
    @Test
    void 카테고리_상세_조회_성공() {
        // given
        Long categoryId = 1L;
        setId(category1, categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category1));

        // when
        CategoryDto.CategoryResponse response = categoryService.getCategory(categoryId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(category1.getName());
        assertThat(response.getDescription()).isEqualTo(category1.getDescription());

        verify(categoryRepository).findById(categoryId);
    }

    @DisplayName("존재하지 않는 카테고리 상세 조회를 요청하면 실패한다.")
    @Test
    void 존재하지_않는_카테고리_상세_조회_실패() {
        // given
        Long categoryId = 999L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategory(categoryId))
                .isInstanceOf(CategoryException.class)
                .hasMessageContaining(CategoryErrorMessages.NOT_FOUND_CATEGORY);

        verify(categoryRepository).findById(categoryId);
    }

    private void setId(Object entity, Long id) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}