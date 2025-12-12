package com.supersonic.limitedstore.domain.order.presentation.controller;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.domain.order.presentation.dto.res.OrderEventLogResponseDto;
import com.supersonic.limitedstore.domain.order.repository.OrderEventLogRepository;
import com.supersonic.limitedstore.domain.order.service.OrderEventLogService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/order-logs")
@RequiredArgsConstructor
public class OrderEventLogController {

    private final OrderEventLogService orderEventLogService;

    @GetMapping
    public ApiResponse<List<OrderEventLogResponseDto>> getAllLogs() {
        return orderEventLogService.getAllLogs();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<List<OrderEventLogResponseDto>> getLogsByOrderId(@PathVariable UUID orderId) {
        return orderEventLogService.getLogsByOrderId(orderId);
    }
}
