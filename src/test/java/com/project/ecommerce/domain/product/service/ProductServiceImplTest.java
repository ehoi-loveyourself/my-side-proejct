package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.common.exception.ProductErrorMessages;
import com.project.ecommerce.common.exception.ProductException;
import com.project.ecommerce.common.exception.UserErrorMessages;
import com.project.ecommerce.common.exception.UserException;
import com.project.ecommerce.domain.product.dto.ProductDto;
import com.project.ecommerce.domain.product.entity.Category;
import com.project.ecommerce.domain.product.entity.Product;
import com.project.ecommerce.domain.product.entity.ProductCategory;
import com.project.ecommerce.domain.product.entity.ProductStatus;
import com.project.ecommerce.domain.product.repository.CategoryRepository;
import com.project.ecommerce.domain.product.repository.ProductRepository;
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
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private User seller;
    private User user;
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

        // 판매자 생성
        seller = User.builder()
                .name("판매자")
                .email("seller@example.com")
                .role(Role.SELLER)
                .build();
        setId(seller, 1L);

        // 일반 유저 생성
        user = User.builder()
                .name("일반 유저")
                .email("user@example.com")
                .role(Role.CUSTOMER)
                .build();
        setId(user, 99L);
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
        // 1. 상품 등록 request dto 만들기
        List<Long> categoryIds = Arrays.asList(1L, 2L);
        ProductDto.ProductRegisterRequest request = ProductDto.ProductRegisterRequest.builder()
                .name("맥북 에어")
                .description("맥북 에어 15인치")
                .price(BigDecimal.valueOf(2_000_000))
                .stock(10)
                .imageUrls(Arrays.asList("image1.jpg", "image2.png"))
                .categoryIds(categoryIds)
                .build();

        // 2. 등록된 상품 인스턴스 만들기
        Product savedProduct = Product.builder()
                .name("맥북 에어")
                .description("맥북 에어 15인치")
                .price(BigDecimal.valueOf(2_000_000))
                .stock(10)
                .sellerId(SELLER_ID)
                .imageUrls(Arrays.asList("image1.jpg", "image2.png"))
                .status(ProductStatus.ACTIVE)
                .build();
        savedProduct.addProductCategory(new ProductCategory(savedProduct, category1));
        savedProduct.addProductCategory(new ProductCategory(savedProduct, category2));
        setId(savedProduct, 99L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category2));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));

        // when
        ProductDto.ProductResponse response = productService.registerProduct(request, seller.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(savedProduct.getName());
        assertThat(response.getDescription()).isEqualTo(savedProduct.getDescription());
        assertThat(response.getPrice()).isEqualTo(savedProduct.getPrice());
        assertThat(response.getStock()).isEqualTo(savedProduct.getStock());
        assertThat(response.getSellerId()).isEqualTo(savedProduct.getSellerId());
        assertThat(response.getImageUrls()).isEqualTo(savedProduct.getImageUrls());
        assertThat(response.getImageUrls()).containsExactly("image1.jpg", "image2.png");
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE.name());
        assertThat(response.getCategories()).hasSize(2);
    }

    @DisplayName("판매자가 규칙에 맞게 상품을 등록하지 않으면 실패한다")
    @Test
    void 상품_등록_테스트_판매자용_실패() throws Exception {
        // given
        List<Long> categoryIds = Arrays.asList(1L, 2L);
        ProductDto.ProductRegisterRequest request = ProductDto.ProductRegisterRequest.builder()
                .name("맥북 에어")
                .description("맥북 에어 15인치")
                .price(BigDecimal.valueOf(2_000_000))
                .stock(10)
                .imageUrls(Arrays.asList("image1.jpg", "image2.png"))
                .categoryIds(categoryIds)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> productService.registerProduct(request, user.getId()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorMessages.ONLY_FOR_SELLER);
    }

    @DisplayName("판매자가 상품을 수정하면 성공적으로 저장된다")
    @Test
    void 상품_수정_테스트_판매자용_성공() throws Exception {
        // given
        String updatedName = "상품 이름 변경";
        String updatedDesc = "상품 설명 변경";
        BigDecimal updatedPrice = BigDecimal.valueOf(2_000_000);
        ProductDto.ProductUpdateRequest request = ProductDto.ProductUpdateRequest.builder()
                .name(updatedName)
                .description(updatedDesc)
                .price(updatedPrice)
                .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // when
        ProductDto.ProductResponse response = productService.updateProduct(PRODUCT_ID, request, SELLER_ID);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(updatedName);
        assertThat(response.getDescription()).isEqualTo(updatedDesc);
        assertThat(response.getPrice()).isEqualTo(updatedPrice);

        // 직접 product 객체의 값이 변경되었는지 검증 -> 더티 체킹이 제대로 동작했는지 확인 가능
        assertThat(product.getName()).isEqualTo(updatedName);
        assertThat(product.getDescription()).isEqualTo(updatedDesc);
        assertThat(product.getPrice()).isEqualTo(updatedPrice);

        // save() 로직을 작성하지 않았는데, save()를 호출하지 않고도 더티 체킹이 동작했는지 확인 가능
        verify(productRepository, never()).save(any());
    }

    @DisplayName("존재하지 않는 상품을 변경하고자 했을 때는 에러를 던진다")
    @Test
    void 상품_수정_테스트_판매자용_실패() throws Exception {
        // given
        String updatedName = "상품 이름 변경";
        String updatedDesc = "상품 설명 변경";
        BigDecimal updatedPrice = BigDecimal.valueOf(2_000_000);
        ProductDto.ProductUpdateRequest request = ProductDto.ProductUpdateRequest.builder()
                .name(updatedName)
                .description(updatedDesc)
                .price(updatedPrice)
                .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, request, SELLER_ID))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining(ProductErrorMessages.NOT_FOUND_PRODUCT);
    }

    @DisplayName("판매자가 상품을 일부만 수정하면 나머지는 기존 값을 유지하고 수정 값만 바뀌어야 한다")
    @Test
    void 상품_일부_수정_테스트_판매자용_성공() throws Exception {
        // given
        String updatedName = "상품 이름 변경";
        String updatedDesc = "상품 설명 변경";
        BigDecimal updatedPrice = BigDecimal.valueOf(2_000_000);
        ProductDto.ProductUpdateRequest request = ProductDto.ProductUpdateRequest.builder()
                .name(updatedName)
                .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // when
        ProductDto.ProductResponse response = productService.updateProduct(PRODUCT_ID, request, SELLER_ID);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(updatedName);
        assertThat(response.getDescription()).isNotEqualTo(updatedDesc);
        assertThat(response.getPrice()).isNotEqualTo(updatedPrice);

        assertThat(response.getDescription()).isEqualTo(product.getDescription());
        assertThat(response.getPrice()).isEqualTo(product.getPrice());

        // 직접 product 객체의 값이 변경되었는지 검증 -> 더티 체킹이 제대로 동작했는지 확인 가능
        assertThat(product.getName()).isEqualTo(updatedName);
        assertThat(product.getDescription()).isNotEqualTo(updatedDesc);
        assertThat(product.getPrice()).isNotEqualTo(updatedPrice);

        // save() 로직을 작성하지 않았는데, save()를 호출하지 않고도 더티 체킹이 동작했는지 확인 가능
        verify(productRepository, never()).save(any());
    }

    @DisplayName("판매자가 상품을 삭제하면 상품이 삭제된다")
    @Test
    void 상품_삭제_테스트_판매자용_성공() throws Exception {
        // given
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // when
        productService.deleteProduct(PRODUCT_ID, SELLER_ID);

        // then
        Product deleted = productRepository.findById(PRODUCT_ID)
                .orElseThrow(() -> new ProductException(ProductErrorMessages.NOT_FOUND_PRODUCT, HttpStatus.NOT_FOUND));

        assertThat(deleted).isNotNull();
        assertThat(deleted.getStatus()).isEqualTo(ProductStatus.INACTIVE);

        verify(productRepository, never()).save(any(Product.class));
    }

    @DisplayName("존재하지 않는 상품을 삭제하려고 할 때 에러가 발생한다")
    @Test
    void 미존재_상품_삭제_테스트_판매자용() throws Exception {
        // given
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.deleteProduct(PRODUCT_ID, SELLER_ID))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining(ProductErrorMessages.NOT_FOUND_PRODUCT);
    }

    @DisplayName("해당 제품의 판매자가 아닌 사람이 삭제하려고 할 때 에러가 발생한다")
    @Test
    void 다른_판매자의_상품_삭제_테스트_판매자용() throws Exception {
        // given
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.deleteProduct(PRODUCT_ID, 2L))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining(ProductErrorMessages.NO_AUTHORIZATION);
    }

    @DisplayName("판매자가 재고를 수정하면 상품의 재고가 변경된다")
    @Test
    void 재고_수정_테스트_성공_판매자용() throws Exception {
        // given
        ProductDto.StockUpdateRequest request = ProductDto.StockUpdateRequest.builder()
                .stock(10_000)
                .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // when
        ProductDto.ProductResponse response = productService.updateStock(request, PRODUCT_ID, SELLER_ID);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStock()).isEqualTo(request.getStock());

        verify(productRepository).findById(anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    @DisplayName("수정하려는 재고 수량이 음수이면 에러가 발생한다")
    @Test
    void 재고_수정_음수_테스트_판매자용() throws Exception {
        // given
        ProductDto.StockUpdateRequest request = ProductDto.StockUpdateRequest.builder()
                .stock(-10_000)
                .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.updateStock(request, PRODUCT_ID, SELLER_ID))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining(ProductErrorMessages.STOCK_MUST_MORE_THAN_ZERO);
    }

    @DisplayName("다른 판매자의 재고를 수정하면 권한 없음 에러가 뜬다")
    @Test
    void 다른_판매자의_상품_수정_테스트_판매자용() throws Exception {
        // given
        ProductDto.StockUpdateRequest request = ProductDto.StockUpdateRequest.builder()
                .stock(10_000)
                .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.updateStock(request, PRODUCT_ID, 2L))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining(ProductErrorMessages.NO_AUTHORIZATION);
    }

    @DisplayName("없는 상품의 재고를 수정하려고 하면 not_found 에러가 뜬다")
    @Test
    void 없는_상품_수정_테스트_판매자용() throws Exception {
        // given
        ProductDto.StockUpdateRequest request = ProductDto.StockUpdateRequest.builder()
                .stock(10_000)
                .build();

        long nonExistingProductId = 99L;
        when(productRepository.findById(nonExistingProductId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.updateStock(request, nonExistingProductId, SELLER_ID))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining(ProductErrorMessages.NOT_FOUND_PRODUCT);
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