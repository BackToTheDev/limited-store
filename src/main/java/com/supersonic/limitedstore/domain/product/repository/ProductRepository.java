package com.supersonic.limitedstore.domain.product.repository;

import com.supersonic.limitedstore.domain.product.entity.Product;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByNameContaining(String keyword);

    List<Product> findByIsDeletedFalse();
}
