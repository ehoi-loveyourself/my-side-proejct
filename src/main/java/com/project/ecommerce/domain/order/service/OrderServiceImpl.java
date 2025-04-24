package com.project.ecommerce.domain.order.service;

import com.project.ecommerce.common.exception.*;
import com.project.ecommerce.common.security.UserContext;
import com.project.ecommerce.domain.order.dto.OrderRequest;
import com.project.ecommerce.domain.order.entity.Order;
import com.project.ecommerce.domain.product.entity.Product;
import com.project.ecommerce.domain.product.repository.ProductRepository;
import com.project.ecommerce.domain.user.entity.Address;
import com.project.ecommerce.domain.user.entity.User;
import com.project.ecommerce.domain.user.repository.AddressRepository;
import com.project.ecommerce.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class OrderServiceImpl implements OrderService {
    private final UserRepository userRepository;

    private final TransactionTemplate transactionTemplate;
    private final UserContext userContext;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse.CreateResponse createOrder(OrderRequest.CreateRequest request) {
        log.info("주문 생성 시작");

        return transactionTemplate.execute(status -> {
           try {
               // 현재 로그인한 사용자 정보 가져오기
               Long currentUserId = userContext.getCurrentUserId();
               User user = userRepository.findById(currentUserId)
                       .orElseThrow(() -> {
                           log.error("주문 생성 실패: 사용자를 찾을 수 없음, userId={}", currentUserId);
                           return new UserException(UserErrorMessages.NOT_FOUND_USER, HttpStatus.NOT_FOUND);
                       });

               // 배송지 정보 처리
               Address shippingAddress = resolveShippingAddress(request, user);

               // 주문 아이템 검증 및 상품 가져오기: 해당 상품이 있는지, 재고가 있는지
               List<OrderItemInfo> orderItemInfos = validateAndGetOrderItems(request.getOrderItems());
               
               // 주문 번호 생성하기
               String orderNumber = generateOrderNumber();

               // 총 주문 금액
               BigDecimal totalAmount = orderItemInfos.stream()
                       .map(itemInfo -> itemInfo.product.getPrice().multiply(BigDecimal.valueOf(itemInfo.quantity)))
                       .reduce(BigDecimal.ZERO, BigDecimal::add);

               Order order = Order.builder()
                       .orderNumber(orderNumber)
                       .totalAmount(totalAmount)
                       .deliveryAddress(shippingAddress)
                       .deliveryMessage(request.getDeliveryMessage())
                       .user(user)
                       .build();


           }
        });
        // 1. 존재하는 사용자인지 확인
        // 2. 주문 엔티티 생성
        // 3. 주문 아이템 생성 및 설정
        // 4. 주문 저장
        return null;
    }

    private String generateOrderNumber() {
        LocalDate today = LocalDate.now();
        String datePart = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return datePart + "-" + randomPart;
    }

    private Address resolveShippingAddress(OrderRequest.CreateRequest request, User user) {
        switch (request.getDeliveryOption()) {
            case DEFAULT_ADDRESS -> {
                // 사용자의 기본 배송지 조회
                return addressRepository.findByUserAndIsDefault(user, true)
                        .orElseThrow(() -> new AddressException(AddressErrorMessages.NOT_FOUND_DEFAULT_ADDRESS, HttpStatus.NOT_FOUND));
            }
            case SAVED_ADDRESS -> {
                // 사용자가 직접 지정한 배송지
                if (request.getAddressId() == null) {
                    throw new AddressException(AddressErrorMessages.REQUIRED_ADDRESS, HttpStatus.BAD_REQUEST);
                }

                return addressRepository.findByIdAndUser(request.getAddressId(), user)
                        .orElseThrow(() -> new AddressException(AddressErrorMessages.NOT_FOUND_ADDRESS, HttpStatus.NOT_FOUND));
            }
            case NEW_ADDRESS -> {
                // 새로운 배송지로 지정
                if (request.getNewDeliveryInfo() == null) {
                    throw new AddressException(AddressErrorMessages.INVALID_NEW_ADDRESS, HttpStatus.BAD_REQUEST);
                }

                Address shippingAddress = Address.builder()
                        .streetAddress(request.getNewDeliveryInfo().getStreetAddress())
                        .city(request.getNewDeliveryInfo().getCity())
                        .zipCode(request.getNewDeliveryInfo().getZipCode())
                        .isDefault(false)
                        .recipientName(request.getNewDeliveryInfo().getRecipientName())
                        .recipientPhone(request.getNewDeliveryInfo().getRecipientPhone())
                        .user(user)
                        .build();

                if (request.getNewDeliveryInfo().isSaveToAddressBook()) {
                    addressRepository.save(shippingAddress);
                }

                return shippingAddress;
            }
            default -> throw new AddressException(AddressErrorMessages.INVALID_DELIVERY_OPTION, HttpStatus.BAD_REQUEST);
        }
    }

    private List<OrderItemInfo> validateAndGetOrderItems(List<OrderRequest.CreateRequest.OrderItemDto> orderItemDtos) {
        return orderItemDtos.stream()
                .map(itemDto -> {
                    Product product = productRepository.findById(itemDto.getProductId())
                            .orElseThrow(() -> new ProductException(ProductErrorMessages.NOT_FOUND_PRODUCT, HttpStatus.NOT_FOUND));

                    if (product.getStock() < itemDto.getQuantity()) {
                        throw new ProductException(String.format(ProductErrorMessages.INSUFFICIENT_STOCK,
                                product.getName(),
                                itemDto.getQuantity(),
                                product.getStock())
                                , HttpStatus.BAD_REQUEST);
                    }

                    return new OrderItemInfo(product, itemDto.getQuantity());
                })
                .toList();
    }

    // 주문 아이템 정보를 담는 내부 클래스
    @Getter
    @AllArgsConstructor
    private static class OrderItemInfo {
        private Product product;
        private int quantity;
    }
}