package com.supersonic.limitedstore.domain.order.service;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.domain.order.entity.OrderEventLog;
import com.supersonic.limitedstore.domain.order.presentation.dto.res.OrderEventLogResponseDto;
import com.supersonic.limitedstore.domain.order.presentation.dto.res.OrderResponseDto;
import com.supersonic.limitedstore.domain.order.repository.OrderEventLogRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderEventLogService {

    private final OrderEventLogRepository orderEventLogRepository;

    @Transactional(readOnly = true)
    public ApiResponse<List<OrderEventLogResponseDto>> getAllLogs() {
        List<OrderEventLogResponseDto> logs =
            orderEventLogRepository.findAll()
                .stream()
                .map(OrderEventLogResponseDto::from)
                .toList();

        return ApiResponse.ok(logs);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<OrderEventLogResponseDto>> getLogsByOrderId(UUID orderId) {
        List<OrderEventLogResponseDto> logs =
            orderEventLogRepository.findAllByOrderId(orderId)
                .stream()
                .map(OrderEventLogResponseDto::from)
                .toList();

        return ApiResponse.ok(logs);
    }
}
