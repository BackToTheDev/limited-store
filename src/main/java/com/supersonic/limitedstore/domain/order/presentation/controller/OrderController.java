package com.supersonic.limitedstore.domain.order.presentation.controller;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.domain.order.presentation.dto.req.OrderRequestDto;
import com.supersonic.limitedstore.domain.order.presentation.dto.res.OrderResponseDto;
import com.supersonic.limitedstore.domain.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponseDto> createOrder(
        @RequestBody @Valid OrderRequestDto dto,
        HttpServletRequest request) {
        UUID memberId = UUID.fromString((String) request.getAttribute("memberId"));
        return orderService.createOrder(memberId, dto);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponseDto> getOrder(@PathVariable UUID orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping
    public ApiResponse<List<OrderResponseDto>> getMyOrders(HttpServletRequest request) {
        UUID memberId = UUID.fromString((String) request.getAttribute("memberId"));
        return orderService.getMyOrders(memberId);
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponseDto> cancelOrder(
        @PathVariable UUID orderId,
        HttpServletRequest request) {
        UUID memberId = UUID.fromString((String) request.getAttribute("memberId"));
        return orderService.cancelOrder(memberId, orderId);
    }
}
