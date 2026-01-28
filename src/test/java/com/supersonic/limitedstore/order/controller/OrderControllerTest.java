package com.supersonic.limitedstore.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.common.exception.CustomException;
import com.supersonic.limitedstore.common.exception.ErrorCode;
import com.supersonic.limitedstore.domain.order.entity.OrderStatus;
import com.supersonic.limitedstore.domain.order.presentation.controller.OrderController;
import com.supersonic.limitedstore.domain.order.presentation.dto.req.OrderRequestDto;
import com.supersonic.limitedstore.domain.order.presentation.dto.res.OrderResponseDto;
import com.supersonic.limitedstore.domain.order.service.OrderService;
import com.supersonic.limitedstore.security.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    UUID productId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();

    @Test
    void 주문_생성_성공() throws Exception {
        //given
        OrderRequestDto request = OrderRequestDto.builder()
            .productId(productId)
            .build();

        OrderResponseDto response = OrderResponseDto.builder()
            .orderId(orderId)
            .memberId(memberId)
            .productId(productId)
            .orderStatus(OrderStatus.READY)
            .createdAt(LocalDateTime.now())
            .build();

        when(orderService.createOrder(any(), any()))
            .thenReturn(ApiResponse.ok(response));

        //when & then
        mockMvc.perform(post("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("memberId", memberId.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderStatus")
                .value(OrderStatus.READY.name()));
    }

    @Test
    void 주문_생성_실패_재고없음() throws Exception {
        // given
        OrderRequestDto request = OrderRequestDto.builder()
            .productId(productId)
            .build();

        when(orderService.createOrder(any(), any()))
            .thenThrow(new CustomException(ErrorCode.OUT_OF_STOCK));

        //when & then
        mockMvc.perform(post("/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .requestAttr("memberId", memberId.toString())
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(ErrorCode.OUT_OF_STOCK.getMessage()));
    }

    @Test
    void 주문_생성_실패_상품없음() throws Exception {
        OrderRequestDto request = OrderRequestDto.builder()
            .productId(productId)
            .build();

        when(orderService.createOrder(any(), any()))
            .thenThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        mockMvc.perform(post("/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .requestAttr("memberId", memberId.toString())
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));
    }

    @Test
    void 주문_생성_실패_중복주문() throws Exception {
        OrderRequestDto request = OrderRequestDto.builder()
            .productId(productId)
            .build();

        when(orderService.createOrder(any(), any()))
            .thenThrow(new CustomException(ErrorCode.ALREADY_PURCHASED));

        mockMvc.perform(post("/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .requestAttr("memberId", memberId.toString())
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_PURCHASED.getMessage()));
    }
}
