package com.project.ecommerce.domain.order.service;

import com.project.ecommerce.domain.order.dto.OrderRequest;
import com.project.ecommerce.domain.order.dto.OrderResponse;
import com.project.ecommerce.domain.order.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    // 주문 생성
    OrderResponse.CreateResponse createOrder(OrderRequest.CreateRequest request);

    // 주문 목록 조회
    List<OrderResponse> getOrderByUserId(Long userId);

    // 주문 상세 조회
    OrderDetailResponse getOrderByOrderId(Long orderId);

    // 주문 상태 변경
    OrderDetailResponse updateOrderStatus(Long orderId, OrderStatus status);

    // 주문 결제 처리 todo: 추가 필요

    // 주문 취소
    OrderDetailResponse cancelOrder(Long orderId);
}
