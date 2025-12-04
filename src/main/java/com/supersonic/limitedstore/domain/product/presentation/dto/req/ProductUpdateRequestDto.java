package com.supersonic.limitedstore.domain.product.presentation.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateRequestDto {
    private String name;
    private String description;
    private Integer price;
    private Integer stock;
    private String releaseAt;
}
