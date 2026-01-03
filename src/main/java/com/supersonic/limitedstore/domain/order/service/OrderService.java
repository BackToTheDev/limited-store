package com.supersonic.limitedstore.domain.order.service;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.common.exception.CustomException;
import com.supersonic.limitedstore.common.exception.ErrorCode;
import com.supersonic.limitedstore.domain.order.entity.Order;
import com.supersonic.limitedstore.domain.order.entity.OrderEventLog;
import com.supersonic.limitedstore.domain.order.entity.OrderStatus;
import com.supersonic.limitedstore.domain.order.infrastructure.client.ProductClient;
import com.supersonic.limitedstore.domain.order.presentation.dto.req.OrderRequestDto;
import com.supersonic.limitedstore.domain.order.presentation.dto.res.OrderResponseDto;
import com.supersonic.limitedstore.domain.order.repository.OrderEventLogRepository;
import com.supersonic.limitedstore.domain.order.repository.OrderRepository;
import com.supersonic.limitedstore.domain.product.entity.Product;
import com.supersonic.limitedstore.domain.product.repository.ProductRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class OrderService {

    /**
     * OrderService는 현재 모놀리식 -> MSA 전환 과도기 상태
     *
     * - 상품 존재 여부 : ProductClient (외부 서비스 책임)
     * - 재고 확인/차감 : ProductRepository (로컬 트랜젝션 유지)
     *
     * 재고 차감 API 분리 시 Product 엔티티 및 Repository 의존은 제거
     */


    private final OrderRepository orderRepository;
    private final OrderEventLogRepository orderEventLogRepository;
    private final ProductRepository productRepository; // 재고 차감 API 분리 시 제거 예정
    private final ProductClient productClient;

    @Transactional
    public ApiResponse<OrderResponseDto> createOrder(UUID memberId, OrderRequestDto dto) {

        UUID productId = dto.getProductId();

        if (!productClient.exists(productId)) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 재고 차감 API 분리 시 productRepository 제거 예정
        Product product = productRepository.findByIdAndIsDeletedFalse(dto.getProductId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 재고 확인
        if (product.getStock() <= 0) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }

        // 1인 1개 구매 제한
        boolean alreadyOrdered = orderRepository.existsByMemberIdAndProductIdAndIsDeletedFalse(
            memberId, dto.getProductId()
        );

        if (alreadyOrdered) {
            throw new CustomException(ErrorCode.ALREADY_PURCHASED);
        }

        // 주문 생성
        Order order = Order.create(memberId, dto.getProductId());
        orderRepository.save(order);

        // 현재 재고 차감은 OrderCreate 내부에 존재
        // 이는 모놀리식 구조를 유지하기 위한 과도기 상태이며,
        // 재고 차감 API(Product 서비스) 분리 시 제거 예정
        product.decreaseStock();
        productRepository.save(product);

        // 주문 생성 로그
        OrderEventLog log = OrderEventLog.of(
            order.getId(),
            "ORDER_CREATED",
            "주문이 생성되었습니다."
        );
        orderEventLogRepository.save(log);

        // response 변환 후 반환
        return ApiResponse.ok(OrderResponseDto.from(order));
    }

    @Transactional(readOnly = true)
    public ApiResponse<OrderResponseDto> getOrder(UUID orderId) {
        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        return ApiResponse.ok(OrderResponseDto.from(order));
    }

    // 트랜잭션에 대해서 이해를 못하고 있는 상태
    // 어떤 경우에서 stream을 써야하나? 분명히 편한 기능이나 지금 상황은 이해를 못함
    // map? 형상 변화로 기억하고 있는데
    @Transactional(readOnly = true)
    public ApiResponse<List<OrderResponseDto>> getMyOrders(UUID memberId) {
        List<OrderResponseDto> orders = orderRepository
            .findAllByMemberIdAndIsDeletedFalse(memberId)
            .stream()
            .map(OrderResponseDto::from)
            .toList();

        return ApiResponse.ok(orders);
    }

    @Transactional
    public ApiResponse<OrderResponseDto> cancelOrder(UUID memberId, UUID orderId) {
        //주문 확인
        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 주문자 확인
        if (!order.getMemberId().equals(memberId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        // 취소된 주문인지 확인
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ALREADY_CANCELLED);
        }

        // 주문 취소
        order.updateStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        Product product = productRepository.findByIdAndIsDeletedFalse(order.getProductId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        product.increaseStock();
        productRepository.save(product);

        orderEventLogRepository.save(
            OrderEventLog.of(order.getId(), "ORDER_CANCELLED", "주문이 취소되었습니다."));

        return ApiResponse.ok(OrderResponseDto.from(order));
    }

}
