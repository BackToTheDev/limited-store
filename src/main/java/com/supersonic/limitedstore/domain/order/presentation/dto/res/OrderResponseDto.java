package com.supersonic.limitedstore.domain.order.presentation.dto.res;

import com.supersonic.limitedstore.domain.order.entity.Order;
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
public class OrderResponseDto {
    private UUID orderId;
    private UUID memberId;
    private UUID productId;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;

    public static OrderResponseDto from(Order order) {
        return OrderResponseDto.builder()
            .orderId(order.getId())
            .memberId(order.getMemberId())
            .productId(order.getProductId())
            .orderStatus(order.getOrderStatus())
            .createdAt(order.getCreatedAt())
            .build();
    }

}
