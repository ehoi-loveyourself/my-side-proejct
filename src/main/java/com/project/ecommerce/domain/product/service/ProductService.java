package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.domain.product.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<ProductDto.ProductSimpleResponse> getProductList(Pageable pageable);

    ProductDto.ProductResponse getProduct(Long productId);

    Page<ProductDto.ProductSimpleResponse> searchProduct(String keyword, Pageable pageable);

    ProductDto.ProductResponse registerProduct(ProductDto.ProductRegisterRequest request, long sellerId);

    ProductDto.ProductResponse updateProduct(Long productId, ProductDto.ProductUpdateRequest request, long sellerId);

    void deleteProduct(Long productId, Long sellerId);
}
