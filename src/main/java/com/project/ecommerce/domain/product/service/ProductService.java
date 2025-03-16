package com.project.ecommerce.domain.product.service;

import com.project.ecommerce.domain.product.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<ProductDto.ProductSimpleResponse> getProductList(Pageable pageable);
}
