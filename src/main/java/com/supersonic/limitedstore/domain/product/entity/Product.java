package com.supersonic.limitedstore.domain.product.entity;

import com.supersonic.limitedstore.common.entity.BaseEntity;
import com.supersonic.limitedstore.common.exception.CustomException;
import com.supersonic.limitedstore.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column()
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stock;

    @Column()
    private LocalDateTime releaseAt;

    public void update(String name, String description, Integer price, Integer stock, LocalDateTime releaseAt) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (stock != null) this.stock = stock;
        if (releaseAt != null) this.releaseAt = releaseAt;
    }

    public void decreaseStock() {
        if (this.stock <= 0) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }
        this.stock -= 1;
    }

    public void increaseStock() {
        this.stock += 1;
    }
}
