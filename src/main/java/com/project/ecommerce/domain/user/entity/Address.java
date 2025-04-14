package com.project.ecommerce.domain.user.entity;

import com.project.ecommerce.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @Column(nullable = false)
    private String streetAddress;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private boolean isDefault;

    // todo: 아래 컬럼 엔티티에 추가하기
    @Column(nullable = false)
    private String recipientName;

    // todo: 아래 컬럼 엔티티에 추가하기
    @Column(nullable = false)
    private String recipientPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder

    public Address(String streetAddress, String city, String zipCode, boolean isDefault, String recipientName, String recipientPhone, User user) {
        this.streetAddress = streetAddress;
        this.city = city;
        this.zipCode = zipCode;
        this.isDefault = isDefault;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.user = user;
    }
}