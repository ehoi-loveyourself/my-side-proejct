package com.project.ecommerce.domain.product.service;

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

import static org.assertj.core.api.Assertions.assertThat;
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
}