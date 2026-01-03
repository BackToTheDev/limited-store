package com.supersonic.limitedstore.domain.product.service;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.common.exception.CustomException;
import com.supersonic.limitedstore.common.exception.ErrorCode;
import com.supersonic.limitedstore.domain.product.entity.Product;
import com.supersonic.limitedstore.domain.product.presentation.dto.req.ProductRequestDto;
import com.supersonic.limitedstore.domain.product.presentation.dto.req.ProductUpdateRequestDto;
import com.supersonic.limitedstore.domain.product.presentation.dto.res.ProductResponseDto;
import com.supersonic.limitedstore.domain.product.repository.ProductRepository;
import com.supersonic.limitedstore.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private static final DateTimeFormatter RELEASE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ApiResponse<ProductResponseDto> createProduct(ProductRequestDto dto) {

        if (productRepository.existsByName(dto.getName())) {
            throw new CustomException(ErrorCode.PRODUCT_DUPLICATED);
        }

        LocalDateTime releaseAt;
        try {
            releaseAt = LocalDateTime.parse(dto.getReleaseAt(), RELEASE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_DATE_FORMAT);
        }



        Product product = Product.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .stock(dto.getStock())
            .releaseAt(releaseAt)
            .build();

        Product saved = productRepository.save(product);

        return ApiResponse.ok(
            ProductResponseDto.from(saved)
        );
    }

    public ApiResponse<List<ProductResponseDto>> getAllProducts() {
        List<Product> products = productRepository.findByIsDeletedFalse();

        List<ProductResponseDto> responseDtoList = products.stream()
            .map(ProductResponseDto::from)
            .collect(Collectors.toList());

        return ApiResponse.ok(responseDtoList);
    }

    public ApiResponse<ProductResponseDto> getProductById(UUID productId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId).orElseThrow(
            () -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)
        );

        return ApiResponse.ok(ProductResponseDto.from(product));
    }

    public ApiResponse<ProductResponseDto> updateProduct(UUID productId, ProductUpdateRequestDto dto) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId).orElseThrow(
            () -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        LocalDateTime releaseAt = null;
        if (dto.getReleaseAt() != null) {
            try {
                releaseAt = LocalDateTime.parse(dto.getReleaseAt(), RELEASE_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new CustomException(ErrorCode.INVALID_DATE_FORMAT);
            }
        }

            product.update(
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getStock(),
                releaseAt
            );

        productRepository.save(product);

        return ApiResponse.ok(ProductResponseDto.from(product));
    }

    public void deleteProduct(UUID productId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId).orElseThrow(
            () -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)
        );

        product.softDelete();
        productRepository.save(product);
    }

    public boolean exists(UUID productId) {
        return productRepository.existsById(productId);
    }

}
