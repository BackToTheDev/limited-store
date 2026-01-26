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

    private final OrderRepository orderRepository;
    private final OrderEventLogRepository orderEventLogRepository;
    private final ProductRepository productRepository;
    private final ProductClient productClient;

    @Transactional
    public ApiResponse<OrderResponseDto> createOrder(UUID memberId, OrderRequestDto dto) {

        UUID productId = dto.getProductId();

        if (!productClient.exists(productId)) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Product product = productRepository.findByIdAndIsDeletedFalse(dto.getProductId())
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStock() <= 0) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }

        boolean alreadyOrdered = orderRepository.existsByMemberIdAndProductIdAndIsDeletedFalse(
            memberId, dto.getProductId()
        );

        if (alreadyOrdered) {
            throw new CustomException(ErrorCode.ALREADY_PURCHASED);
        }

        Order order = Order.create(memberId, dto.getProductId());
        orderRepository.save(order);

        product.decreaseStock();
        productRepository.save(product);

        OrderEventLog log = OrderEventLog.of(
            order.getId(),
            "ORDER_CREATED",
            "주문이 생성되었습니다."
        );
        orderEventLogRepository.save(log);

        return ApiResponse.ok(OrderResponseDto.from(order));
    }

    @Transactional(readOnly = true)
    public ApiResponse<OrderResponseDto> getOrder(UUID orderId) {
        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        return ApiResponse.ok(OrderResponseDto.from(order));
    }

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

        Order order = orderRepository.findByIdAndIsDeletedFalse(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ALREADY_CANCELLED);
        }

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
