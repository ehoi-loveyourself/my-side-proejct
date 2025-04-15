package com.project.ecommerce.domain.order.entity;

import com.project.ecommerce.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    private String transactionId; // 외부 결제 시스템의 트랜잭션 ID

    private String paymentDetails; // 결제 방법별 추가 정보 (JSON 문자열로 저장)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    public Payment(BigDecimal amount, PaymentStatus status, PaymentMethod method, String transactionId, String paymentDetails, Order order) {
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.transactionId = transactionId;
        this.paymentDetails = paymentDetails;
        this.order = order;
    }
}