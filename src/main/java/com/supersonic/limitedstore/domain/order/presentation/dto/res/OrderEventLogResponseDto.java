package com.supersonic.limitedstore.domain.order.presentation.dto.res;

import com.supersonic.limitedstore.domain.order.entity.OrderEventLog;
import com.supersonic.limitedstore.domain.order.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventLogResponseDto {
    private UUID logId;
    private UUID orderId;
    private String eventType;
    private String message;
    private LocalDateTime createdAt;

    public static OrderEventLogResponseDto from(OrderEventLog log) {
        return OrderEventLogResponseDto.builder()
            .logId(log.getId())
            .orderId(log.getOrderId())
            .eventType(log.getEventType())
            .message(log.getMessage())
            .createdAt(log.getCreatedAt())
            .build();
    }
}
