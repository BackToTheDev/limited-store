package com.supersonic.limitedstore.domain.product.presentation.controller;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.domain.product.presentation.dto.req.ProductRequestDto;
import com.supersonic.limitedstore.domain.product.presentation.dto.req.ProductUpdateRequestDto;
import com.supersonic.limitedstore.domain.product.presentation.dto.res.ProductResponseDto;
import com.supersonic.limitedstore.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/products")
@RestController
@AllArgsConstructor
public class ProductController {

    private ProductService productService;

    @PostMapping
    @Operation(summary = "상품 생성", description = "신규 상품을 등록합니다.")
    public ApiResponse<ProductResponseDto> createProduct(@RequestBody @Valid ProductRequestDto dto) {
        return productService.createProduct(dto);
    }

    @GetMapping
    @Operation(summary = "상품 리스트 조회", description = "상품 리스트를 조회합니다.")
    public ApiResponse<List<ProductResponseDto>> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "상품 조회", description = "상품을 조회합니다.")
    public ApiResponse<ProductResponseDto> getProductById(@PathVariable("id") UUID id) {
        return productService.getProductById(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "상품 수정", description = "상품을 수정합니다.")
    public ApiResponse<ProductResponseDto> updateProduct(@PathVariable("id") UUID id, @RequestBody @Valid ProductUpdateRequestDto dto) {
        return productService.updateProduct(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    public void deleteProduct(@PathVariable("id") UUID id) {
        productService.deleteProduct(id);
    }


    @GetMapping("/{id}/exists")
    public boolean exists(@PathVariable UUID id) {
        return productService.exists(id);
    }
}
