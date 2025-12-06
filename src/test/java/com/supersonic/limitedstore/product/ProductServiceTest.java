package com.supersonic.limitedstore.product;

import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.common.exception.CustomException;
import com.supersonic.limitedstore.common.exception.ErrorCode;
import com.supersonic.limitedstore.domain.product.entity.Product;
import com.supersonic.limitedstore.domain.product.presentation.dto.req.ProductRequestDto;
import com.supersonic.limitedstore.domain.product.presentation.dto.req.ProductUpdateRequestDto;
import com.supersonic.limitedstore.domain.product.presentation.dto.res.ProductResponseDto;
import com.supersonic.limitedstore.domain.product.repository.ProductRepository;
import com.supersonic.limitedstore.domain.product.service.ProductService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void 상품_생성_성공() {
        // given
        ProductRequestDto dto = ProductRequestDto.builder()
            .name("테스트 상품")
            .description("설명")
            .price(10000)
            .stock(10)
            .releaseAt("2025-12-05 12:03:02")
            .build();

        LocalDateTime parsedReleaseAt = LocalDateTime.parse(dto.getReleaseAt(), formatter);

        when(productRepository.existsByName(dto.getName())).thenReturn(false);

        Product product = Product.builder()
            .id(UUID.randomUUID())
            .name(dto.getName())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .stock(dto.getStock())
            .releaseAt(parsedReleaseAt)
            .build();

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // when
        ApiResponse<ProductResponseDto> result = productService.createProduct(dto);

        // then
        assertThat(result.getData().getName()).isEqualTo(dto.getName());
        verify(productRepository, times(1)).save(any());
    }

    @Test
    void 상품_생성_중복상품명_에러() {
        // given
        ProductRequestDto dto = ProductRequestDto.builder()
            .name("중복상품")
            .price(1000)
            .stock(5)
            .releaseAt("2025-12-05 12:00:00")
            .build();

        when(productRepository.existsByName(dto.getName())).thenReturn(true);

        // when & then
        assertThrows(CustomException.class, () -> productService.createProduct(dto));
    }

    @Test
    void 상품_생성_날짜형식오류() {
        // given
        ProductRequestDto dto = ProductRequestDto.builder()
            .name("상품")
            .price(1000)
            .stock(5)
            .releaseAt("잘못된형식")
            .build();

        // when & then
        assertThrows(CustomException.class, () -> productService.createProduct(dto));
    }

    // ---------------------------
    // 상품 단일 조회 테스트
    // ---------------------------

    @Test
    void 상품_조회_성공() {
        // given
        UUID id = UUID.randomUUID();

        Product product = Product.builder()
            .id(id)
            .name("테스트")
            .price(1000)
            .stock(5)
            .build();

        when(productRepository.findByIdAndIsDeletedFalse(id))
            .thenReturn(Optional.of(product));

        // when
        ApiResponse<ProductResponseDto> result = productService.getProductById(id);

        // then
        assertThat(result.getData().getId()).isEqualTo(id);
    }

    @Test
    void 상품_조회_실패_삭제되었거나없음() {
        // given
        UUID id = UUID.randomUUID();

        when(productRepository.findByIdAndIsDeletedFalse(id))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> productService.getProductById(id));
    }

    // ---------------------------
    // 상품 전체 조회 테스트
    // ---------------------------

    @Test
    void 상품_전체조회_성공() {
        // given
        List<Product> list = List.of(
            Product.builder().id(UUID.randomUUID()).name("A").price(1000).stock(1).build(),
            Product.builder().id(UUID.randomUUID()).name("B").price(2000).stock(2).build()
        );

        when(productRepository.findByIsDeletedFalse())
            .thenReturn(list);

        // when
        ApiResponse<List<ProductResponseDto>> result = productService.getAllProducts();

        // then
        assertThat(result.getData().size()).isEqualTo(2);
    }

    // ---------------------------
    // 상품 수정 테스트
    // ---------------------------

    @Test
    void 상품_업데이트_성공() {
        // given
        UUID id = UUID.randomUUID();

        Product product = Product.builder()
            .id(id)
            .name("old")
            .price(1000)
            .stock(10)
            .build();

        ProductUpdateRequestDto dto = ProductUpdateRequestDto.builder()
            .name("newName")
            .price(2000)
            .build();

        when(productRepository.findByIdAndIsDeletedFalse(id))
            .thenReturn(Optional.of(product));

        when(productRepository.save(any())).thenReturn(product);

        // when
        ApiResponse<ProductResponseDto> result = productService.updateProduct(id, dto);

        // then
        assertThat(result.getData().getName()).isEqualTo("newName");
        assertThat(result.getData().getPrice()).isEqualTo(2000);
        verify(productRepository, times(1)).save(any());
    }

    // ---------------------------
    // 상품 삭제 테스트
    // ---------------------------

    @Test
    void 상품_삭제_성공() {
        // given
        UUID id = UUID.randomUUID();

        Product product = Product.builder()
            .id(id)
            .name("삭제대상")
            .price(500)
            .stock(1)
            .build();

        when(productRepository.findByIdAndIsDeletedFalse(id))
            .thenReturn(Optional.of(product));

        // when
        productService.deleteProduct(id);

        // then
        assertThat(product.isDeleted()).isTrue();
        verify(productRepository, times(1)).save(any());
    }
}
