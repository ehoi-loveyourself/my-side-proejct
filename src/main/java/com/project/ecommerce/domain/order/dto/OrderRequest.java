package com.project.ecommerce.domain.order.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

public class OrderRequest {

    @Getter
    public static class CreateRequest {
        // 주문 아이템들
        private List<OrderItemDto> orderItems;
        // 배송 옵션
        private DeliveryOption deliveryOption;
        // 배송 메시지
        private String deliveryMessage;

        @Getter
        public static class OrderItemDto {
            private Long productId;
            private int quantity;;
            private BigDecimal price; // 구매 당시의 가격
        }

        public enum DeliveryOption {
            DEFAULT_ADDRESS,    // 기본 배송지 사용
            SAVED_ADDRESS,      // 저장된 다른 주소 사용
            NEW_ADDRESS         // 새로운 주소 사용
        }

        // 저장된 다른 주소를 사용한다면
        private Long addressId;

        // 새로운 주소로 배송할 경우
        private DeliveryInfoDto newDeliveryInfo;

        @Getter
        public static class DeliveryInfoDto {
            // 주소
            private String streetAddress;
            private String city;
            // 우편번호
            private String zipCode;
            // 수령인
            private String recipientName;
            // 수령인 연락처
            private String recipientPhone;
            // 주소록 저장 여부
            private boolean saveToAddressBook;
        }
    }
}