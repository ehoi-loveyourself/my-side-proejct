package com.project.ecommerce.domain.order.dto;

import com.project.ecommerce.domain.order.entity.Order;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {

    @Builder
    @Getter
    public static class CreateResponse {
        private Long orderId;
        private String orderNumber;
        private BigDecimal totalAmount;
        private String orderStatus;
        private List<OrderItemDto> orderItemDtos;

        @Builder
        @Getter
        public static class OrderItemDto {
            private Long productId;
            private String productName;
            private int quantity;;
            private BigDecimal price; // 구매 당시의 가격
            private BigDecimal totalPricePerProduct; // 구매 가격 * 해당 상품 구매 수량
        }

        public static OrderResponse.CreateResponse from(Order order) {
            List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                    .map(item ->
                        OrderItemDto.builder()
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .totalPricePerProduct(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .build())
                    .toList();

            return CreateResponse.builder()
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .totalAmount(order.getTotalAmount())
                    .orderStatus(order.getStatus().name())
                    .orderItemDtos(orderItemDtos)
                    .build();
        }
    }
}