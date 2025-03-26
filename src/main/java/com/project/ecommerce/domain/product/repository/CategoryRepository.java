package com.project.ecommerce.domain.product.repository;

import com.project.ecommerce.domain.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    List<Category> findByParentCategory_Id(Long categoryId);
}
