package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.common.exception.*;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public Page<ProductDto.ProductSimpleResponse> getProductList(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable)
            .map(ProductDto.ProductSimpleResponse::of);
    }

    @Override
    public ProductDto.ProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorMessages.NOT_FOUND_PRODUCT, HttpStatus.NOT_FOUND));

        return ProductDto.ProductResponse.of(product);
    }

    @Override
    public Page<ProductDto.ProductSimpleResponse> searchProduct(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndStatus(keyword, ProductStatus.ACTIVE, pageable)
                .map(ProductDto.ProductSimpleResponse::of);
    }

    @Override
    @Transactional
    public ProductDto.ProductResponse registerProduct(ProductDto.ProductRegisterRequest request, Long sellerId) {
        // 판매자인지에 대한 검증
        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND));

        if (user.getRole() != Role.SELLER) {
            throw new UserException(UserErrorMessages.ONLY_FOR_SELLER, HttpStatus.UNAUTHORIZED);
        }

        // 상품 생성
        Product newProduct = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .sellerId(sellerId)
                .imageUrls(request.getImageUrls())
                .status(ProductStatus.ACTIVE)
                .build();

        // 카테고리 연결
        addCategoriesToProduct(request.getCategoryIds(), newProduct);

        // 상품 저장
        Product savedProduct = productRepository.save(newProduct);

        // 응답 생성
        return ProductDto.ProductResponse.of(savedProduct);
    }

    @Override
    @Transactional
    public ProductDto.ProductResponse updateProduct(Long productId, ProductDto.ProductUpdateRequest request, Long sellerId) {
        // 수정해야 할 상품 찾기
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorMessages.NOT_FOUND_PRODUCT, HttpStatus.NOT_FOUND));

        // 판매자 검증 포함
        if (!product.getSellerId().equals(sellerId)) {
            throw new ProductException(ProductErrorMessages.NO_AUTHORIZATION, HttpStatus.FORBIDDEN);
        }

        // 상품 수정하기
        if (Objects.nonNull(request.getName())) {
            product.updateName(request.getName());
        }

        if (Objects.nonNull(request.getDescription())) {
            product.updateDescription(request.getDescription());
        }

        if (Objects.nonNull(request.getPrice())) {
            if (request.getPrice().compareTo(BigDecimal.valueOf(100)) < 0) {
                throw new ProductException(ProductErrorMessages.INVALID_PRICE, HttpStatus.BAD_REQUEST);
            }

            product.updatePrice(request.getPrice());
        }

        // 변경 사항 저장 (명확한 의도를 위해 추가)
//        productRepository.save(product);

        return ProductDto.ProductResponse.of(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorMessages.NOT_FOUND_PRODUCT, HttpStatus.NOT_FOUND));

        if (!product.getSellerId().equals(sellerId)) {
            throw new ProductException(ProductErrorMessages.NO_AUTHORIZATION, HttpStatus.FORBIDDEN);
        }

        product.delete();
    }

    @Override
    @Transactional
    public ProductDto.ProductResponse updateStock(ProductDto.StockUpdateRequest request, Long productId, Long sellerId) {
        // 수정해야 할 상품 찾기
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorMessages.NOT_FOUND_PRODUCT, HttpStatus.NOT_FOUND));

        // 판매자 검증 포함
        if (!product.getSellerId().equals(sellerId)) {
            throw new ProductException(ProductErrorMessages.NO_AUTHORIZATION, HttpStatus.FORBIDDEN);
        }

        // 재고 수정하기
        if (request.getStock() < 0) {
            throw new ProductException(ProductErrorMessages.STOCK_MUST_MORE_THAN_ZERO, HttpStatus.BAD_REQUEST);
        }

        if (request.getStock() == product.getStock()) {
            throw new ProductException(ProductErrorMessages.CANNOT_UPDATE_PRODUCT_WITH_SAME_STOCK, HttpStatus.BAD_REQUEST);
        }
        Product updatedStock = product.updateStock(request.getStock());

        return ProductDto.ProductResponse.of(updatedStock);
    }

    private void addCategoriesToProduct(List<Long> categoryIds, Product product) {
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryException(CategoryErrorMessages.NOT_FOUND_CATEGORY, HttpStatus.NOT_FOUND));

            ProductCategory productCategory = new ProductCategory(product, category);
            product.addProductCategory(productCategory);
        }
    }
}