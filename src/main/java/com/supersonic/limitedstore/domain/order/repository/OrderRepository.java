package com.supersonic.limitedstore.domain.order.repository;

import com.supersonic.limitedstore.domain.order.entity.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByIdAndIsDeletedFalse(UUID id);

    List<Order> findAllByMemberIdAndIsDeletedFalse(UUID memberId);

    boolean existsByMemberIdAndProductIdAndIsDeletedFalse(UUID memberId, UUID productId);

}
