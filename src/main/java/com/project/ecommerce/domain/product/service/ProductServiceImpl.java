package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.common.exception.ProductErrorMessages;
import com.project.ecommerce.common.exception.ProductException;
import com.project.ecommerce.domain.product.dto.ProductDto;
import com.project.ecommerce.domain.product.entity.Product;
import com.project.ecommerce.domain.product.entity.ProductStatus;
import com.project.ecommerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

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
}