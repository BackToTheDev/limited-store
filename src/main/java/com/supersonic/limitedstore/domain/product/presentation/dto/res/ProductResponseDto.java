package com.supersonic.limitedstore.domain.product.presentation.dto.res;

import com.supersonic.limitedstore.common.entity.BaseEntity;
import com.supersonic.limitedstore.domain.product.entity.Product;
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
public class ProductResponseDto {

    private UUID id;
    private String name;
    private String description;
    private Integer price;
    private Integer stock;
    private LocalDateTime releaseAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponseDto from(Product product) {
        return ProductResponseDto.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .stock(product.getStock())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .releaseAt(product.getReleaseAt())
            .build();
    }
}
