package com.supersonic.limitedstore.domain.order.entity;

import com.supersonic.limitedstore.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id")
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(columnDefinition = "text")
    private String message;

    public static OrderEventLog of(UUID orderId, String eventType, String message) {
        return OrderEventLog.builder()
            .orderId(orderId)
            .eventType(eventType)
            .message(message)
            .build();
    }
}
