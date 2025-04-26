package com.project.ecommerce.domain.order.service;

import com.project.ecommerce.common.exception.*;
import com.project.ecommerce.common.security.UserContext;
import com.project.ecommerce.domain.order.dto.OrderRequest;
import com.project.ecommerce.domain.order.dto.OrderResponse;
import com.project.ecommerce.domain.order.entity.Order;
import com.project.ecommerce.domain.order.entity.OrderItem;
import com.project.ecommerce.domain.order.repository.OrderRepository;
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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
    private final OrderRepository orderRepository;

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

               // 주문 엔티티 생성
               Order order = Order.builder()
                       .orderNumber(orderNumber)
                       .totalAmount(totalAmount)
                       .deliveryAddress(shippingAddress)
                       .deliveryMessage(request.getDeliveryMessage())
                       .user(user)
                       .build();

               // 주문 아이템 생성 및 세팅
               List<OrderItem> orderItems = createOrderItems(orderItemInfos, order);
               order.setOrderItems(orderItems);

               // 재고 감소 로직
               decreaseProductStock(orderItemInfos);

               // 주문 저장
               Order savedOrder = orderRepository.save(order);
               log.info("주문 생성 완료, 주문 정보={}", savedOrder.toString());

               return OrderResponse.CreateResponse.from(order);
           } catch (Exception e) {
               status.setRollbackOnly();
               log.error("주문 생성중 오류 발생", e);
               throw e;
           }
        });
    }

    private void decreaseProductStock(List<OrderItemInfo> orderItemInfos) {
        orderItemInfos.forEach(info -> {
            try {
                info.getProduct().decreaseQuantity(info.getQuantity());
                productRepository.save(info.getProduct());

                log.debug("상품 재고 감소: 상품 id={}, 감소량={}, 남은 재고={}",
                        info.getProduct().getId(), info.getQuantity(), info.getProduct().getStock());
            } catch (OptimisticLockingFailureException e) {
                log.warn("재고 감소 중 낙관적 락 충돌 발생 -> 재시도, product id={}", info.getProduct().getId());
                retryDecreaseStock(info.getProduct().getId(), info.getQuantity());
            }
        });
    }

    /**
     * [여기서 product가 아니라 productId를 파라미터로 넘기는 이유가 궁금해서 찾아봄]
     * 낙관적 락 매커니즘과 관련이 있음!
     * 낙관적 락 예외가 발생했다는 것은, 이미 해당 엔티티의 버전 정보가 다른 트랜잭션에 의해 변경되었다는 !
     * 1. 그래서 최신 버전의 엔티티를 조회할 필요가 있음: 기존 엔티티 객체를 그대로 사용하면 또 다시 락에 걸릴 수가 있음
     * 2. @Retryable 어노테이션은, 트랜잭션을 분리해서 새로운 트랜잭션에서 메서드를 실행함. 이때 엔티티 자체는 트랜잭션 간에
     *      직렬화/역직렬화되지 않고, ID만 전달하는 것이 안전함
     * 3. 영속성 컨텍스트 고려: 하나의 트랜잭션이 실패하고 새로운 트랜잭션이 시작될 때, 영속성 컨텍스트는 초기화되므로
     * 처음부터 다시 ID로 조회하는 것이 영속성 문제를 방지한다.
     * @param productId
     * @param quantity
     */
    // todo retryable annotation
    private void retryDecreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorMessages.NOT_FOUND_PRODUCT, HttpStatus.NOT_FOUND));

        product.decreaseQuantity(quantity);
        productRepository.save(product);
        log.debug("상품 재고 감소 재시도 성공: productId={}, 감소량={}, 남은 재고={}",
                product.getId(), quantity, product.getStock());
    }

    private List<OrderItem> createOrderItems(List<OrderItemInfo> orderItemInfos, Order order) {
        return orderItemInfos.stream()
                .map(info -> OrderItem.builder()
                        .price(info.product.getPrice())
                        .quantity(info.quantity)
                        .order(order)
                        .product(info.product)
                        .build())
                .collect(Collectors.toList());
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