package com.project.ecommerce.domain.order.entity;

public enum PaymentMethod {
    CREDIT_CARD,    // 신용카드
    CASH,           // 현금
    BANK_TRANSFER,  // 계좌이체
    MOBILE_PAYMENT, // 모바일 결제
    VIRTUAL_ACCOUNT // 가상계좌
}
