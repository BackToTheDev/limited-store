package com.supersonic.limitedstore.order.service;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.common.exception.CustomException;
import com.supersonic.limitedstore.common.exception.ErrorCode;
import com.supersonic.limitedstore.domain.order.entity.Order;
import com.supersonic.limitedstore.domain.order.entity.OrderStatus;
import com.supersonic.limitedstore.domain.order.infrastructure.client.ProductClient;
import com.supersonic.limitedstore.domain.order.presentation.dto.req.OrderRequestDto;
import com.supersonic.limitedstore.domain.order.presentation.dto.res.OrderResponseDto;
import com.supersonic.limitedstore.domain.order.repository.OrderEventLogRepository;
import com.supersonic.limitedstore.domain.order.repository.OrderRepository;
import com.supersonic.limitedstore.domain.order.service.OrderService;
import com.supersonic.limitedstore.domain.product.entity.Product;
import com.supersonic.limitedstore.domain.product.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderEventLogRepository orderEventLogRepository;

    private UUID memberId;
    private UUID productId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }


    @Test
    void 주문_생성_성공() {
        OrderRequestDto dto = OrderRequestDto.builder()
            .productId(productId)
            .build();

        Product product = Product.builder()
            .id(productId)
            .stock(10)
            .build();

        when(productClient.exists(productId))
            .thenReturn(true);

        when(productRepository.findByIdAndIsDeletedFalse(productId))
            .thenReturn(Optional.of(product));

        when(orderRepository.existsByMemberIdAndProductIdAndIsDeletedFalse(memberId, productId))
            .thenReturn(false);

        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<OrderResponseDto> response =
            orderService.createOrder(memberId, dto);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getData().getOrderStatus()).isEqualTo(OrderStatus.READY);
        assertThat(product.getStock()).isEqualTo(9);

        verify(orderEventLogRepository).save(any());
    }

    @Test
    void 주문_생성_실패_상품없음() {
        OrderRequestDto dto = OrderRequestDto.builder()
            .productId(productId)
            .build();

        when(productClient.exists(any(UUID.class)))
        .thenReturn(false);

        assertThatThrownBy(() ->
            orderService.createOrder(memberId, dto))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }

    @Test
    void 주문_생성_실패_재고없음() {
        OrderRequestDto dto = OrderRequestDto.builder()
            .productId(productId)
            .build();

        Product product = Product.builder()
            .id(productId)
            .stock(0)
            .build();

        when(productClient.exists(productId))
            .thenReturn(true);

        when(productRepository.findByIdAndIsDeletedFalse(productId))
            .thenReturn(Optional.of(product));

        assertThatThrownBy(() ->
            orderService.createOrder(memberId, dto))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.OUT_OF_STOCK.getMessage());
    }

    @Test
    void 주문_생성_실패_이미구매함() {
        OrderRequestDto dto = OrderRequestDto.builder()
            .productId(productId)
            .build();

        Product product = Product.builder()
            .id(productId)
            .stock(10)
            .build();

        when(productClient.exists(productId))
            .thenReturn(true);

        when(productRepository.findByIdAndIsDeletedFalse(productId))
            .thenReturn(Optional.of(product));

        when(orderRepository.existsByMemberIdAndProductIdAndIsDeletedFalse(memberId, productId))
            .thenReturn(true);

        assertThatThrownBy(() ->
            orderService.createOrder(memberId, dto))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.ALREADY_PURCHASED.getMessage());
    }

    @Test
    void 주문_단건_조회_성공() {
        Order order = Order.builder()
            .id(orderId)
            .memberId(memberId)
            .productId(productId)
            .orderStatus(OrderStatus.READY)
            .build();

        when(orderRepository.findByIdAndIsDeletedFalse(orderId))
            .thenReturn(Optional.of(order));

        ApiResponse<OrderResponseDto> response =
            orderService.getOrder(orderId);

        assertThat(response.getData().getOrderId()).isEqualTo(orderId);
    }

    @Test
    void 주문_단건_조회_실패() {
        when(orderRepository.findByIdAndIsDeletedFalse(orderId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(orderId))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    void 내_주문_목록_조회() {
        when(orderRepository.findAllByMemberIdAndIsDeletedFalse(memberId))
            .thenReturn(List.of());

        ApiResponse<List<OrderResponseDto>> response =
            orderService.getMyOrders(memberId);

        assertThat(response.getData()).isEmpty();
    }


    @Test
    void 주문_취소_성공() {
        Order order = Order.builder()
            .id(orderId)
            .memberId(memberId)
            .productId(productId)
            .orderStatus(OrderStatus.READY)
            .build();

        Product product = Product.builder()
            .id(productId)
            .stock(5)
            .build();

        when(orderRepository.findByIdAndIsDeletedFalse(orderId))
            .thenReturn(Optional.of(order));

        when(productRepository.findByIdAndIsDeletedFalse(productId))
            .thenReturn(Optional.of(product));

        ApiResponse<OrderResponseDto> response =
            orderService.cancelOrder(memberId, orderId);

        assertThat(response.getData().getOrderStatus())
            .isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStock()).isEqualTo(6);

        verify(orderEventLogRepository).save(any());
    }

    @Test
    void 주문_취소_실패_권한없음() {
        Order order = Order.builder()
            .id(orderId)
            .memberId(UUID.randomUUID())
            .orderStatus(OrderStatus.READY)
            .build();

        when(orderRepository.findByIdAndIsDeletedFalse(orderId))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
            orderService.cancelOrder(memberId, orderId))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.NO_PERMISSION.getMessage());
    }

    @Test
    void 주문_취소_실패_이미취소됨() {
        Order order = Order.builder()
            .id(orderId)
            .memberId(memberId)
            .orderStatus(OrderStatus.CANCELLED)
            .build();

        when(orderRepository.findByIdAndIsDeletedFalse(orderId))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
            orderService.cancelOrder(memberId, orderId))
            .isInstanceOf(CustomException.class)
            .hasMessage(ErrorCode.ALREADY_CANCELLED.getMessage());
    }
}
