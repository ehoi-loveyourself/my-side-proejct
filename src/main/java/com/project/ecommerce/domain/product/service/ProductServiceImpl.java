package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.common.exception.CategoryErrorMessages;
import com.project.ecommerce.common.exception.CategoryException;
import com.project.ecommerce.common.exception.ProductErrorMessages;
import com.project.ecommerce.common.exception.ProductException;
import com.project.ecommerce.domain.product.dto.ProductDto;
import com.project.ecommerce.domain.product.entity.Category;
import com.project.ecommerce.domain.product.entity.Product;
import com.project.ecommerce.domain.product.entity.ProductCategory;
import com.project.ecommerce.domain.product.entity.ProductStatus;
import com.project.ecommerce.domain.product.repository.CategoryRepository;
import com.project.ecommerce.domain.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

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
    public ProductDto.ProductResponse registerProduct(ProductDto.ProductRegisterRequest request, long sellerId) {
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

    private void addCategoriesToProduct(List<Long> categoryIds, Product product) {
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryException(CategoryErrorMessages.NOT_FOUND_CATEGORY, HttpStatus.NOT_FOUND));

            ProductCategory productCategory = new ProductCategory(product, category);
            product.addProductCategory(productCategory);
        }
    }
}