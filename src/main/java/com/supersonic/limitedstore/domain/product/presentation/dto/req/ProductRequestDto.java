package com.supersonic.limitedstore.domain.product.presentation.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 100)
    private String description;

    @NotNull
    @PositiveOrZero
    private Integer price;

    @NotNull
    @PositiveOrZero
    private Integer stock;

    @NotNull
    private String releaseAt;
}
