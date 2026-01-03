package com.supersonic.limitedstore.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supersonic.limitedstore.common.dto.ApiResponse;
import com.supersonic.limitedstore.common.exception.CustomException;
import com.supersonic.limitedstore.common.exception.ErrorCode;
import com.supersonic.limitedstore.domain.product.presentation.controller.ProductController;
import com.supersonic.limitedstore.domain.product.presentation.dto.req.ProductRequestDto;
import com.supersonic.limitedstore.domain.product.presentation.dto.req.ProductUpdateRequestDto;
import com.supersonic.limitedstore.domain.product.presentation.dto.res.ProductResponseDto;
import com.supersonic.limitedstore.domain.product.service.ProductService;
import com.supersonic.limitedstore.security.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 상품_생성_성공() throws Exception {
        // given
        ProductRequestDto dto = ProductRequestDto.builder()
            .name("테스트 상품")
            .description("설명입니다")
            .price(10000)
            .stock(10)
            .releaseAt("2025-12-05 12:03:02")
            .build();

        ProductResponseDto response = ProductResponseDto.builder()
            .id(UUID.randomUUID())
            .name(dto.getName())
            .description(dto.getDescription())
            .price(dto.getPrice())
            .stock(dto.getStock())
            .releaseAt(LocalDateTime.now())
            .build();

        when(productService.createProduct(any())).thenReturn(ApiResponse.ok(response));

        // when & then
        mockMvc.perform(post("/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value(dto.getName()));
    }

    @Test
    void 상품_생성_실패_날짜형식_오류() throws Exception {
        ProductRequestDto dto = ProductRequestDto.builder()
            .name("테스트 상품")
            .price(10000)
            .stock(5)
            .releaseAt("잘못된 형식")
            .build();

        when(productService.createProduct(any()))
            .thenThrow(new CustomException(ErrorCode.INVALID_DATE_FORMAT));

        mockMvc.perform(post("/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void 상품_생성_실패_중복상품명() throws Exception {
        ProductRequestDto dto = ProductRequestDto.builder()
            .name("중복 상품")
            .price(10000)
            .stock(5)
            .releaseAt("2025-12-05 12:03:02")
            .build();

        when(productService.createProduct(any()))
            .thenThrow(new CustomException(ErrorCode.PRODUCT_DUPLICATED));

        mockMvc.perform(post("/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void 상품_단건조회_성공() throws Exception {
        // given
        UUID id = UUID.randomUUID();

        ProductResponseDto response = ProductResponseDto.builder()
            .id(id)
            .name("상품명")
            .price(5000)
            .stock(3)
            .releaseAt(LocalDateTime.now())
            .build();

        when(productService.getProductById(id)).thenReturn(ApiResponse.ok(response));

        // when & then
        mockMvc.perform(get("/v1/products/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    void 상품_단건조회_실패_NOT_FOUND() throws Exception {
        UUID id = UUID.randomUUID();

        when(productService.getProductById(id))
            .thenThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        mockMvc.perform(get("/v1/products/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void 상품_전체조회_성공() throws Exception {
        List<ProductResponseDto> list = List.of(
            ProductResponseDto.builder()
                .id(UUID.randomUUID())
                .name("상품1")
                .price(10000)
                .stock(5)
                .releaseAt(LocalDateTime.now())
                .build(),
            ProductResponseDto.builder()
                .id(UUID.randomUUID())
                .name("상품2")
                .price(20000)
                .stock(10)
                .releaseAt(LocalDateTime.now())
                .build()

        );

        when(productService.getAllProducts()).thenReturn(ApiResponse.ok(list));

        mockMvc.perform(get("/v1/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void 상품_업데이트_성공() throws Exception {
        //given

        UUID id = UUID.randomUUID();
        ProductUpdateRequestDto dto = ProductUpdateRequestDto.builder()
            .name("업데이트 상품")
            .price(20000)
            .stock(5)
            .build();

        ProductResponseDto response = ProductResponseDto.builder()
            .id(id)
            .name("업데이트 상품")
            .price(20000)
            .stock(5)
            .releaseAt(LocalDateTime.now())
            .build();

        when(productService.updateProduct(eq(id), any(ProductUpdateRequestDto.class)))
            .thenReturn(ApiResponse.ok(response));

        //when & then
        mockMvc.perform(patch("/v1/products/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value(dto.getName()))
            .andExpect(jsonPath("$.data.price").value(dto.getPrice()));
    }

    @Test
    void 상품_업데이트_실패_NOT_FOUND() throws Exception {
        UUID id = UUID.randomUUID();

        ProductUpdateRequestDto dto = ProductUpdateRequestDto.builder()
            .name("업데이트 상품")
            .price(20000)
            .stock(5)
            .build();

        when(productService.updateProduct(eq(id), any()))
            .thenThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        mockMvc.perform(patch("/v1/products/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
    }

    @Test
    void 상품_삭제_성공() throws Exception {
        // given
        UUID id = UUID.randomUUID();

        doNothing().when(productService).deleteProduct(id);

        // when & then
        mockMvc.perform(delete("/v1/products/{id}", id))
            .andExpect(status().isOk());

        verify(productService, times(1)).deleteProduct(id);
    }

    @Test
    void 상품_삭제_실패_NOT_FOUND() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND))
            .when(productService).deleteProduct(id);

        mockMvc.perform(delete("/v1/products/{id}", id))
            .andExpect(status().isNotFound());
    }
}
