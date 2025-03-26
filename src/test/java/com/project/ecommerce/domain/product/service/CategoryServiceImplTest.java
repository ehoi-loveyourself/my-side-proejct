package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.common.exception.CategoryErrorMessages;
import com.project.ecommerce.common.exception.CategoryException;
import com.project.ecommerce.common.exception.UserErrorMessages;
import com.project.ecommerce.common.exception.UserException;
import com.project.ecommerce.domain.product.dto.CategoryDto;
import com.project.ecommerce.domain.product.entity.Category;
import com.project.ecommerce.domain.product.repository.CategoryRepository;
import com.project.ecommerce.domain.user.entity.Role;
import com.project.ecommerce.domain.user.entity.User;
import com.project.ecommerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category1;
    private Category category2;
    private User seller;
    private User user;

    @BeforeEach
    void setUp() {
        category1 = Category.builder()
                .name("카테고리1")
                .description("카테고리1 설명")
                .build();
        ReflectionTestUtils.setField(category1, "id", 1L);

        category2 = Category.builder()
                .name("카테고리2")
                .description("카테고리2 설명")
                .build();
        ReflectionTestUtils.setField(category2, "id", 2L);

        // 판매자 생성
        seller = User.builder()
                .name("판매자")
                .email("seller@example.com")
                .role(Role.SELLER)
                .build();
        ReflectionTestUtils.setField(seller, "id", 1L);

        // 일반 유저 생성
        user = User.builder()
                .name("일반 유저")
                .email("user@example.com")
                .role(Role.CUSTOMER)
                .build();
        ReflectionTestUtils.setField(user, "id", 99L);
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

    @DisplayName("카테고리 생성을 성공한다.")
    @Test
    void 카테고리_생성_판매자용_성공() {
        // given
        CategoryDto.CategoryRegisterRequest request = CategoryDto.CategoryRegisterRequest.builder()
                .name("가방")
                .description("가방 카테고리입니다.")
                .build(); // 최상위 카테고리

        Category savedCategory = Category.builder()
                .name("가방")
                .description("가방 카테고리입니다.")
                .build(); // 최상위 카테고리
        ReflectionTestUtils.setField(savedCategory, "id", 1L);

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));

        // when
        CategoryDto.CategoryResponse response = categoryService.registerCategory(request, seller.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(savedCategory.getName());
        assertThat(response.getDescription()).isEqualTo(savedCategory.getDescription());

        verify(categoryRepository).existsByName(savedCategory.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @DisplayName("일반 고객이 카테고리를 생성하려고 하면 실패한다.")
    @Test
    void 카테고리_생성_판매자용_실패_권한문제() {
        // given
        CategoryDto.CategoryRegisterRequest request = CategoryDto.CategoryRegisterRequest.builder()
                .name("가방")
                .description("가방 카테고리입니다.")
                .build(); // 최상위 카테고리

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> categoryService.registerCategory(request, user.getId()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorMessages.ONLY_FOR_SELLER);
    }

    @DisplayName("이미 존재하는 카테고리를 생성하려고 하면 실패한다.")
    @Test
    void 카테고리_생성_판매자용_실패_이름중복() {
        // given
        Category existingCategory = Category.builder()
                .name("가방")
                .description("가방 카테고리입니다.")
                .build();

        CategoryDto.CategoryRegisterRequest request = CategoryDto.CategoryRegisterRequest.builder()
                .name("가방")
                .description("가방 카테고리입니다.")
                .build(); // 최상위 카테고리

        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        when(categoryRepository.existsByName(request.getName())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.registerCategory(request, seller.getId()))
                .isInstanceOf(CategoryException.class)
                .hasMessageContaining(CategoryErrorMessages.ALREADY_EXIST);
    }

    @DisplayName("판매자가 카테고리를 수정시 성공한다.")
    @Test
    void 카테고리_수정_판매자용_성공() {
        // given
        CategoryDto.CategoryUpdateRequest request = CategoryDto.CategoryUpdateRequest.builder()
                .name("수정된 카테고리 이름")
                .description("수정된 카테고리 설명")
                .build();

        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(category1.getId())).thenReturn(Optional.of(category1));

        // when
        CategoryDto.CategoryResponse response = categoryService.updateCategory(request, seller.getId(), category1.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getDescription()).isEqualTo(request.getDescription());
    }

    @DisplayName("일반 고객이 카테고리를 수정시실패한다.")
    @Test
    void 카테고리_수정_판매자용_실패_권한문제() {
        // given
        CategoryDto.CategoryUpdateRequest request = CategoryDto.CategoryUpdateRequest.builder()
                .name("수정된 카테고리 이름")
                .description("수정된 카테고리 설명")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(request, user.getId(), category1.getId()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorMessages.ONLY_FOR_SELLER);
    }

    @DisplayName("카테고리를 수정시 자기자신을 상위카테고리로 지정시 실패한다.")
    @Test
    void 카테고리_수정_판매자용_실패_본인카테고리() {
        // given
        CategoryDto.CategoryUpdateRequest request = CategoryDto.CategoryUpdateRequest.builder()
                .name("수정된 카테고리 이름")
                .description("수정된 카테고리 설명")
                .parentCategoryId(1L)
                .build();

        ReflectionTestUtils.setField(category1, "id", 1L);
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(category1.getId())).thenReturn(Optional.of(category1));

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(request, seller.getId(), category1.getId()))
                .isInstanceOf(CategoryException.class)
                .hasMessageContaining(CategoryErrorMessages.CANNOT_ASSIGN_MYSELF);
    }
}