package com.project.ecommerce.domain.product.entity;

import com.project.ecommerce.common.exception.ProductErrorMessages;
import com.project.ecommerce.common.exception.ProductException;
import com.project.ecommerce.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private int stock;

    private Long sellerId;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCategory> productCategories = new ArrayList<>();

    @Builder
    public Product(String name, String description, BigDecimal price, int stock, Long sellerId, List<String> imageUrls, ProductStatus status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.sellerId = sellerId;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.status = status;
    }

    public void setProductCategoriesForTest(List<ProductCategory> productCategories) {
        this.productCategories = productCategories;
    }

    public void addProductCategory(ProductCategory productCategory) {
        if (this.productCategories == null) {
            this.productCategories = new ArrayList<>();
        }
        this.productCategories.add(productCategory);

        productCategory.mapProduct(this);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updatePrice(BigDecimal price) {
        this.price = price;
    }

    public void delete() {
        this.status = ProductStatus.INACTIVE;
    }

    public Product updateStock(int stock) {
        this.stock = stock;
        return this;
    }

    public void decreaseQuantity(int quantity) {
        if (this.stock < quantity) {
            throw new ProductException(String.format(ProductErrorMessages.INSUFFICIENT_STOCK,
                    this.name,
                    quantity,
                    this.stock)
                    , HttpStatus.BAD_REQUEST);
        }
    }
}