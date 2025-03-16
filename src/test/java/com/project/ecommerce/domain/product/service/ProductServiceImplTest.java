package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.common.exception.ProductErrorMessages;
import com.project.ecommerce.common.exception.ProductException;
import com.project.ecommerce.domain.product.dto.ProductDto;
import com.project.ecommerce.domain.product.entity.Category;
import com.project.ecommerce.domain.product.entity.Product;
import com.project.ecommerce.domain.product.entity.ProductCategory;
import com.project.ecommerce.domain.product.entity.ProductStatus;
import com.project.ecommerce.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private Category category1;
    private Category category2;
    private ProductCategory productCategory1;
    private ProductCategory productCategory2;
    private final long PRODUCT_ID = 1L;
    private final long SELLER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 카테고리 생성
        category1 = Category.builder()
                .name("전자제품")
                .description("전자제품 카테고리")
                .build();
        setId(category1, 1L);

        category2 = Category.builder()
                .name("노트북")
                .description("노트북 카테고리")
                .build();
        setId(category2, 2L);

        // 상품 생성
        product = Product.builder()
                .name("맥북 프로")
                .description("애플 맥북 프로 16인치")
                .price(BigDecimal.valueOf(2_500_000))
                .stock(10)
                .sellerId(SELLER_ID)
                .imageUrls(Arrays.asList("image1.jpg", "image2.png"))
                .status(ProductStatus.ACTIVE)
                .build();
        setId(product, PRODUCT_ID);

        // ProductCategory 생성
        productCategory1 = new ProductCategory(product, category1);
        setId(productCategory1, 1L);

        productCategory2 = new ProductCategory(product, category2);
        setId(productCategory2, 2L);

        // product 의 getCategories 메소드가 카테고리 목록을 반환하도록 설정
        List<ProductCategory> productCategories = Arrays.asList(productCategory1, productCategory2);
        product.setProductCategoriesForTest(productCategories);
    }

    @DisplayName("상품 목록 조회 시 활성화된 상품만 조회된다.")
    @Test
    void 상품_목록_조회_테스트_성공() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").descending());
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByStatus(ProductStatus.ACTIVE, pageable)).thenReturn(productPage);

        // when
        Page<ProductDto.ProductSimpleResponse> response = productService.getProductList(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(products.size());
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo(product.getName());

        verify(productRepository).findByStatus(ProductStatus.ACTIVE, pageable);
    }

    @DisplayName("상품 상세 조회 시 카테고리와 함께 조회된다")
    @Test
    void 상품_상세_조회_테스트_성공() throws Exception {
        // given
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // when
        ProductDto.ProductResponse response = productService.getProduct(PRODUCT_ID);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("맥북 프로");
        assertThat(response.getDescription()).isEqualTo("애플 맥북 프로 16인치");
        assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(2_500_000));
        assertThat(response.getStock()).isEqualTo(10);
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE.name());
        assertThat(response.getImageUrls()).containsExactly("image1.jpg", "image2.png");

        // 카테고리 검증
        assertThat(response.getCategories()).hasSize(2);
        assertThat(response.getCategories().get(0).getName()).isEqualTo("전자제품");
        assertThat(response.getCategories().get(1).getName()).isEqualTo("노트북");

        verify(productRepository).findById(1L);
    }

    @DisplayName("없는 상품 아이디로 조회시 에러 발생")
    @Test
    void 상품_상세_조회_테스트_실패() throws Exception {
        // given
        Long nonExistingProductId = 999L;
        when(productRepository.findById(nonExistingProductId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProduct(nonExistingProductId))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining(ProductErrorMessages.NOT_FOUND_PRODUCT);

        verify(productRepository).findById(nonExistingProductId);
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

    @DisplayName("상품명 키워드로 검색 시 해당 키워드를 포함하는 활성화된 상품만 조회된다")
    @Test
    void 상품_검색_테스트_성송() throws Exception {
        // given
        String keyword = "맥북";
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id").descending());
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByNameContainingIgnoreCaseAndStatus(keyword, ProductStatus.ACTIVE, pageable)).thenReturn(productPage);

        // when
        Page<ProductDto.ProductSimpleResponse> response = productService.searchProduct(keyword, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(products.size());
        assertThat(response.getContent().get(0).getName()).isEqualTo(product.getName());
        assertThat(response.getContent().get(0).getName()).contains(keyword);

        verify(productRepository).findByNameContainingIgnoreCaseAndStatus(keyword, ProductStatus.ACTIVE, pageable);
    }

    @DisplayName("판매자가 상품을 등록하면 성공적으로 저장된다")
    @Test
    void 상품_등록_테스트_판매자용_성공() throws Exception {
        // given

        // when

        // then
    }
}