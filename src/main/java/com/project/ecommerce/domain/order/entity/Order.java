package com.project.ecommerce.domain.order.entity;

import com.project.ecommerce.domain.common.BaseEntity;
import com.project.ecommerce.domain.user.entity.Address;
import com.project.ecommerce.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String deliveryMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address deliveryAddress;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Order(String orderNumber, BigDecimal totalAmount, String deliveryMessage,
                 User user, Address deliveryAddress, Payment payment, List<OrderItem> orderItems) {
        this.orderNumber = orderNumber;
        this.orderItems = orderItems;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.deliveryMessage = deliveryMessage;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.user = user;
        this.payment = payment;
    }
}