package com.supersonic.limitedstore.domain.order.repository;

import com.supersonic.limitedstore.domain.order.entity.OrderEventLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEventLogRepository extends JpaRepository<OrderEventLog, UUID> {
    List<OrderEventLog> findAllByOrderId(UUID orderId);
}
