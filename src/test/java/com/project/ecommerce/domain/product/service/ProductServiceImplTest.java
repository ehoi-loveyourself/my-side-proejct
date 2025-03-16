package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.domain.product.dto.ProductDto;
import com.project.ecommerce.domain.product.entity.Product;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private final long PRODUCT_ID = 1;
    private final long SELLER_ID = 1;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .name("테스트 상품 이름")
                .description("테스트 상품 설명")
                .price(BigDecimal.valueOf(10000))
                .stock(10)
                .sellerId(SELLER_ID)
                .imageUrls(Collections.singletonList("image1.jpg"))
                .status(ProductStatus.ACTIVE)
                .build();
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
}